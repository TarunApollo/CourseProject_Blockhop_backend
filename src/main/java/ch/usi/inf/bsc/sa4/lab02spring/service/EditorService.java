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
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

/// Service for editing level layers and object properties.
@Service
public class EditorService {

    /// Repository used to load and persist levels.
    private final LevelRepository levelRepository;

    /// Service used to inspect tile set metadata.
    private final TileSetService tileSetService;

    /// Factory used to create game objects from tile ids.
    private final GameObjectFactory gameObjectFactory;

    /// Service used for level-related validation and state updates.
    private final LevelService levelService;

    /// Creates an editor service with all required collaborators.
    /// @param levelRepository repository for loading and saving levels
    /// @param tileSetService service for tile metadata checks
    /// @param gameObjectFactory factory for creating game objects
    /// @param levelService service for level validation/update logic
    @Autowired
    public EditorService(final LevelRepository levelRepository, final TileSetService tileSetService,
            final GameObjectFactory gameObjectFactory, final LevelService levelService) {
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
        this.gameObjectFactory = gameObjectFactory;
        this.levelService = levelService;
    }

    /// Replaces the entire world layer of a level.
    /// The request contains all tiles that should exist in the layer.
    /// Tiles not present in the request are implicitly deleted.
    ///
    /// @param userId  the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto     contains the list of tiles (position + gid) representing the new world layer state
    /// @return the updated level
    /// @throws LevelNotFoundException   if level not found
    /// @throws ForbiddenUserException   if not level owner
    /// @throws LevelPublishedException  if level is published
    /// @throws IllegalArgumentException if any position is out of bounds or any gid is invalid
    public Level replaceWorldLayer(final String userId, final String levelId, final UpdateWorldLayerDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        Map<Position, GroundObject> newWorldLayer = new LinkedHashMap<>();
        for (EditorLevelDTO tile : dto.tiles()) {
            level.ensureWithinBounds(tile.position());
            int gid = tile.gid();
            new TileObjectId(gid, tileSetService::isGroundGID);
            newWorldLayer.put(tile.position(), new GroundObject(gid));
        }

        level.setWorldLayer(newWorldLayer);

        this.levelService.invalidateLevelPublishEligible(level, userId);
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
    /// @throws IllegalArgumentException if any position is out of bounds, gid invalid, or placement rules violated
    public Level replaceObjectLayer(final String userId, final String levelId, final UpdateObjectLayerDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        Map<Position, GameObject> newObjectLayer = new LinkedHashMap<>();
        Set<Position> positionsInNewLayer = new HashSet<>();

        for (EditorLevelDTO object : dto.objects()) {
            level.ensureWithinBounds(object.position());

            int gid = object.gid();
            new TileObjectId(gid, tileSetService::isObjectGID);

            if (positionsInNewLayer.contains(object.position())) {
                throw new IllegalArgumentException("Duplicate position in object layer: " + object.position());
            }
            positionsInNewLayer.add(object.position());

            GameObject gameObject = gameObjectFactory.createGameObject(gid, object.position(), object.content());
            newObjectLayer.put(object.position(), gameObject);
        }

        level.ensureValidObjectLayer(newObjectLayer);
        level.setObjectLayer(newObjectLayer);

        this.levelService.invalidateLevelPublishEligible(level, userId);
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
        Level level = levelRepository.findById(levelId)
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

        this.levelService.invalidateLevelPublishEligible(level, userId);
        return levelRepository.save(level);
    }
}
