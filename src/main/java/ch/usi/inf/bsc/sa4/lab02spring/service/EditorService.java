package ch.usi.inf.bsc.sa4.lab02spring.service;

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
import ch.usi.inf.bsc.sa4.lab02spring.model.TileObjectId;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPublishService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

/// Applies editor updates to world and object layers.
@Service
public class EditorService {

    /// Persists edited levels.
    private final LevelRepository levelRepository;
    /// Validates tile identifiers against the loaded tileset.
    private final TileSetService tileSetService;
    /// Builds domain objects for edited object-layer entries.
    private final GameObjectFactory gameObjectFactory;
    /// Recomputes publish eligibility after edits.
    private final LevelPublishService levelPublishService;

    /// Creates an editor service with its required collaborators.
    ///
    /// @param levelRepository persists edited levels
    /// @param tileSetService validates ground and object gids
    /// @param gameObjectFactory creates game objects from editor payloads
    /// @param levelPublishService updates publish eligibility after edits
    @Autowired
    public EditorService(
            final LevelRepository levelRepository,
            final TileSetService tileSetService,
            final GameObjectFactory gameObjectFactory,
            final LevelPublishService levelPublishService) {
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
        this.gameObjectFactory = gameObjectFactory;
        this.levelPublishService = levelPublishService;
    }

    /// Replaces the entire world layer of a level.
    /// The request contains all tiles that should exist in the layer.
    /// Tiles not present in the request are implicitly deleted.
    ///
    /// @param userId  the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto     contains the list of tiles (position + gid)
    ///                representing the new world layer state
    /// @return the updated level
    /// @throws LevelNotFoundException   if level not found
    /// @throws ForbiddenUserException   if not level owner
    /// @throws LevelPublishedException  if level is published
    /// @throws IllegalArgumentException if any position is out of bounds or any gid is invalid
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public Level replaceWorldLayer(final String userId, final String levelId, final UpdateWorldLayerDTO dto) {
        final Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        // Built dynamically via put(); Map.of() is immutable so cannot be used here.
        final Map<Position, GroundObject> newWorldLayer = new LinkedHashMap<>(dto.tiles().size());
        for (final EditorLevelDTO tile : dto.tiles()) {
            level.ensureWithinBounds(tile.position());
            final int gid = tile.gid();
            new TileObjectId(gid, tileSetService::isGroundGID);
            newWorldLayer.put(tile.position(), new GroundObject(gid));
        }

        level.setWorldLayer(newWorldLayer);

        this.levelPublishService.invalidateLevelPublishEligible(level, userId);
        return levelRepository.save(level);
    }

    /// Replaces the entire object layer of a level.
    /// The request contains all objects that should exist in the layer.
    /// Objects not present in the request are implicitly deleted.
    ///
    /// @param userId the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto contains the list of objects with position, gid, and optional content
    /// @return the updated level
    /// @throws LevelNotFoundException if level not found
    /// @throws ForbiddenUserException if not level owner
    /// @throws LevelPublishedException if level is published
    /// @throws IllegalArgumentException if any position is out of bounds, gid
    ///         invalid, or placement rules are violated
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public Level replaceObjectLayer(final String userId, final String levelId, final UpdateObjectLayerDTO dto) {
        final Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        // Built dynamically via put(); Map.of() is immutable so cannot be used here.
        final Map<Position, GameObject> newObjectLayer = new LinkedHashMap<>(dto.objects().size());
        final Set<Position> positionsInNewLayer = new HashSet<>(dto.objects().size());

        for (final EditorLevelDTO object : dto.objects()) {
            level.ensureWithinBounds(object.position());

            final int gid = object.gid();
            new TileObjectId(gid, tileSetService::isObjectGID);

            if (positionsInNewLayer.contains(object.position())) {
                throw new IllegalArgumentException("Duplicate position in object layer: " + object.position());
            }
            positionsInNewLayer.add(object.position());

            final GameObject gameObject = gameObjectFactory.createGameObject(gid, object.position(), object.content());
            newObjectLayer.put(object.position(), gameObject);
        }

        level.ensureValidObjectLayer(newObjectLayer);
        level.setObjectLayer(newObjectLayer);

        this.levelPublishService.invalidateLevelPublishEligible(level, userId);
        return levelRepository.save(level);
    }

    /// Updates the properties of an existing object in the object layer.
    /// @param userId the authenticated user's ID
    /// @param levelId the level containing the object
    /// @param dto contains the position and properties to update
    /// @return the updated level
    /// @throws LevelNotFoundException if level or object not found
    /// @throws ForbiddenUserException if not level owner
    /// @throws LevelPublishedException if level is published
    /// @throws IllegalArgumentException if property doesn't match object type
    public Level updateObjectProperties(final String userId, final String levelId, final UpdateObjectPropertiesDTO dto) {
        final Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);


        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        switch (dto) {
            case BoxPropertyUpdateDTO boxUpdate ->
                level.updateBoxContent(dto.position(), boxUpdate.content());
            default ->
                throw new IllegalArgumentException(
                    "Unsupported object type for property update: " + dto.getClass().getSimpleName());
        }

        this.levelPublishService.invalidateLevelPublishEligible(level, userId);
        return levelRepository.save(level);
    }
}
