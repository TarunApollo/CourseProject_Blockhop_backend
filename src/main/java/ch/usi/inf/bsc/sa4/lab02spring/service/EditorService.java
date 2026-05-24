package ch.usi.inf.bsc.sa4.lab02spring.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.BoxPropertyUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectPropertiesDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPublishService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

/// Applies editor updates to world and object layers.
@Service
@SuppressWarnings("PMD.UseConcurrentHashMap")
public class EditorService {

    /// Persists edited levels.
    private final LevelRepository levelRepository;
    /// Validates tile identifiers against the loaded tile catalog.
    private final TileCatalogService tileCatalogService;
    /// Builds domain objects for edited object-layer entries.
    private final GameObjectFactory gameObjectFactory;
    /// Resets publication state and public engagement after edits.
    private final LevelPublishService levelPublishService;

    /// Creates an editor service with its required collaborators.
    ///
    /// @param levelRepository     persists edited levels
    /// @param tileCatalogService  validates ground and object tile ids
    /// @param gameObjectFactory   creates game objects from editor payloads
    /// @param levelPublishService resets public publication state after edits
    @Autowired
    public EditorService(
            final LevelRepository levelRepository,
            final TileCatalogService tileCatalogService,
            final GameObjectFactory gameObjectFactory,
            final LevelPublishService levelPublishService) {
        this.levelRepository = levelRepository;
        this.tileCatalogService = tileCatalogService;
        this.gameObjectFactory = gameObjectFactory;
        this.levelPublishService = levelPublishService;
    }

    /// Replaces the entire world layer of a level. The request contains all tiles
    /// that should exist in the layer. Tiles not present in the request are
    /// implicitly deleted.
    ///
    /// @param userId  the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto     contains the list of tiles (position + tileId) representing
    ///                the new world layer state
    /// @return the updated level
    /// @throws LevelNotFoundException   if level not found
    /// @throws ForbiddenUserException   if not level owner
    /// @throws LevelPublishedException  if level is published
    /// @throws IllegalArgumentException if any position is out of bounds or any
    ///                                  tileId is invalid
    public Level replaceWorldLayer(final String userId, final String levelId, final UpdateWorldLayerDTO dto) {
        final Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        final Map<Position, GroundObject> newLayer = new LinkedHashMap<>(dto.tiles().size());
        final Set<Position> seenPositions = new HashSet<>(dto.tiles().size());

        for (final EditorLevelDTO entry : dto.tiles()) {
            level.ensureWithinBounds(entry.position());
            if (!seenPositions.add(entry.position())) {
                throw new IllegalArgumentException("Duplicate position in layer: " + entry.position());
            }
            if (entry.tileId() == null || entry.tileId().isBlank() || !tileCatalogService.isWorldTile(entry.tileId())) {
                throw new IllegalArgumentException("Invalid tileId: " + entry.tileId());
            }
            newLayer.put(entry.position(), new GroundObject(entry.tileId()));
        }

        level.setWorldLayer(Collections.unmodifiableMap(newLayer));
        this.levelPublishService.resetLevelAfterEdit(level, userId);
        return levelRepository.save(level);
    }

    /// Replaces the entire object layer of a level. The request contains all
    /// objects that should exist in the layer. Objects not present in the request
    /// are implicitly deleted.
    ///
    /// @param userId  the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto     contains the list of objects with position, tileId, and
    ///                optional content
    /// @return the updated level
    /// @throws LevelNotFoundException   if level not found
    /// @throws ForbiddenUserException   if not level owner
    /// @throws LevelPublishedException  if level is published
    /// @throws IllegalArgumentException if any position is out of bounds, tileId
    ///                                  invalid, or placement rules are violated
    public Level replaceObjectLayer(final String userId, final String levelId, final UpdateObjectLayerDTO dto) {
        final Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        final Map<Position, GameObject> newLayer = new LinkedHashMap<>(dto.objects().size());
        final Set<Position> seenPositions = new HashSet<>(dto.objects().size());

        for (final EditorLevelDTO entry : dto.objects()) {
            level.ensureWithinBounds(entry.position());
            if (!seenPositions.add(entry.position())) {
                throw new IllegalArgumentException("Duplicate position in layer: " + entry.position());
            }
            if (entry.tileId() == null || entry.tileId().isBlank() || !tileCatalogService.isObjectTile(entry.tileId())) {
                throw new IllegalArgumentException("Invalid tileId: " + entry.tileId());
            }
            newLayer.put(entry.position(), gameObjectFactory.createGameObject(entry.tileId(), entry.position(), entry.content()));
        }

        level.ensureValidObjectLayer(newLayer);
        level.setObjectLayer(Collections.unmodifiableMap(newLayer));
        this.levelPublishService.resetLevelAfterEdit(level, userId);
        return levelRepository.save(level);
    }

    /// Updates the properties of an existing object in the object layer.
    /// 
    /// @param userId  the authenticated user's ID
    /// @param levelId the level containing the object
    /// @param dto     contains the position and properties to update
    /// @return the updated level
    /// @throws LevelNotFoundException   if level or object not found
    /// @throws ForbiddenUserException   if not level owner
    /// @throws LevelPublishedException  if level is published
    /// @throws IllegalArgumentException if property doesn't match object type
    public Level updateObjectProperties(final String userId, final String levelId,
            final UpdateObjectPropertiesDTO dto) {
        final Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        if (dto instanceof BoxPropertyUpdateDTO boxUpdate) {
            level.updateBoxContent(dto.position(), boxUpdate.content());
        } else {
            throw new IllegalArgumentException(
                    "Unsupported object type for property update: " + dto.getClass().getSimpleName());
        }

        this.levelPublishService.resetLevelAfterEdit(level, userId);
        return levelRepository.save(level);
    }
}
