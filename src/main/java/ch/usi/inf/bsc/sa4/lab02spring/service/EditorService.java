package ch.usi.inf.bsc.sa4.lab02spring.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObjectTileId;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

@Service
public class EditorService {

    private final LevelRepository levelRepository;
    private final TileSetService tileSetService;
    private final GameObjectFactory gameObjectFactory;

    @Autowired
    public EditorService(LevelRepository levelRepository, TileSetService tileSetService, GameObjectFactory gameObjectFactory) {
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
        this.gameObjectFactory = gameObjectFactory;
    }

    /// Edits a single tile in the world layer of a level.
    ///
    /// Tile operations:
    /// - If gid > 0 and the position is empty, a tile is added (put operation)
    /// - If gid > 0 and the position already contains a tile, that tile is replaced (put operation)
    /// - If gid == 0, the tile at that position is removed (remove operation); if no tile exists, this is a no-op
    ///
    /// Security enforcement:
    /// - Only the level creator can edit (throws ForbiddenUserException resulting in 403 FORBIDDEN)
    /// - Only unpublished levels can be edited (throws LevelPublishedException resulting in 403 FORBIDDEN)
    /// - Coordinates must be within level bounds: 0 ≤ x < width, 0 ≤ y < height (returns 400 BAD_REQUEST otherwise)
    ///
    /// @spec.requires dto contains a non-null position and a valid gid.
    /// @spec.modifies the world layer of the level identified by levelId in the repository.
    /// @spec.effects if gid > 0, adds or replaces the ground object at the target position.
    ///               If gid == 0, removes the ground object at the target position if one exists.
    ///               Saves the updated level to the repository.
    /// @param userId the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto contains the target position and gid
    /// @return the updated level
    /// @throws NoSuchElementException if level not found
    /// @throws ForbiddenUserException if not level owner
    /// @throws LevelPublishedException if level is published
    /// @throws IllegalArgumentException if position out of bounds or gid invalid
    public Level editWorldLayerTile(String userId, String levelId, EditorLevelDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("Level was not found!"));

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        GameObjectTileId gid = dto.gid() == 0
        ? GameObjectTileId.remove()
        : new GameObjectTileId(dto.gid(), tileSetService::isGroundGID);
        level.updateWorldLayerTile(dto.position(), gid);

        return levelRepository.save(level);
    }

    /// Batch updates multiple tiles in the world layer of a level.
    /// @param userId the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto contains the list of positions and gids to apply
    /// @return the updated level
    /// @throws NoSuchElementException if level not found
    /// @throws ForbiddenUserException if not level owner
    /// @throws LevelPublishedException if level is published
    /// @throws IllegalArgumentException if any position is out of bounds or any gid is invalid
    public Level updateWorldLayer(String userId, String levelId, UpdateWorldLayerDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("Level not found"));

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        Map<Position, GameObjectTileId> tiles = new HashMap<>();
        for(EditorLevelDTO object : dto.tiles()){
            GameObjectTileId gid = new GameObjectTileId(object.gid(), tileSetService::isGroundGID);
            tiles.put(object.position(), gid);
        }

        level.updateWorldLayerBatch(tiles);

        return levelRepository.save(level);
    }
    public Level editObjectLayerTile(String userId, String levelId, EditorLevelDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("Level was not found!"));

        level.ensureOwnedBy(userId);
        level.ensureModifiable();
        level.ensureWithinBounds(dto.position());

        GameObjectTileId tileId = new GameObjectTileId(dto.gid(), tileSetService::isObjectGID);

        if (tileId.isRemoval()) {
            level.removeObjectLayer(dto.position());
        } else {
            if (tileSetService.isGroundGID(dto.gid())) {
                throw new IllegalArgumentException("Ground tiles cannot be placed in object layer");
            }
            if (level.getWorldLayer().containsKey(dto.position())) {
                throw new IllegalArgumentException("Cannot place object on ground tile");
            }
            if (level.getObjectLayer().containsKey(dto.position())) {
                throw new IllegalArgumentException("Tile already has an object");
            }
            level.putObjectLayer(dto.position(), gameObjectFactory.createGameObject(dto.gid(), dto.position()));
        }
        return levelRepository.save(level);
    }
}