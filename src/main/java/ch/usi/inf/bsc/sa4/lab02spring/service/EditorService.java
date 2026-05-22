package ch.usi.inf.bsc.sa4.lab02spring.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.BoxPropertyUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ILayerUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectPropertiesDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
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
        return applyLayerUpdate(userId, levelId, dto, (level, entry) -> {
            validateTileId(entry.tileId(), tileCatalogService::isWorldTile);
            return new GroundObject(entry.tileId());
        }, Level::setWorldLayer);
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
        return applyLayerUpdate(userId, levelId, dto, (level, entry) -> {
            validateTileId(entry.tileId(), tileCatalogService::isObjectTile);
            return gameObjectFactory.createGameObject(entry.tileId(), entry.position(), entry.content());
        }, (level, layer) -> {
            level.ensureValidObjectLayer(layer);
            level.setObjectLayer(layer);
        });
    }

    /// Common logic for replacing a level layer.
    private <T> Level applyLayerUpdate(
            final String userId,
            final String levelId,
            final ILayerUpdateDTO dto,
            final java.util.function.BiFunction<Level, EditorLevelDTO, T> factory,
            final java.util.function.BiConsumer<Level, Map<Position, T>> setter) {
        final Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        final Map<Position, T> newLayer = new LinkedHashMap<>(dto.entries().size());
        final Set<Position> seenPositions = new HashSet<>(dto.entries().size());

        for (final EditorLevelDTO entry : dto.entries()) {
            level.ensureWithinBounds(entry.position());
            if (!seenPositions.add(entry.position())) {
                throw new IllegalArgumentException("Duplicate position in layer: " + entry.position());
            }
            newLayer.put(entry.position(), factory.apply(level, entry));
        }

        setter.accept(level, newLayer);

        this.levelPublishService.resetLevelAfterEdit(level, userId);
        return levelRepository.save(level);
    }

    private static void validateTileId(final String tileId, final java.util.function.Predicate<String> predicate) {
        if (tileId == null || tileId.isBlank() || !predicate.test(tileId)) {
            throw new IllegalArgumentException("Invalid tileId: " + tileId);
        }
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
