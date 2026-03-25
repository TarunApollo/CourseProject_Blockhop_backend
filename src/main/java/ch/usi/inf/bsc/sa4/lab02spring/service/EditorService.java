package ch.usi.inf.bsc.sa4.lab02spring.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.BoxPropertyUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CoinPropertyUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.SnailPropertyUpdateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectPropertiesDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileObjectId;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;

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

        TileObjectId gid = dto.gid() == 0
        ? TileObjectId.remove()
        : new TileObjectId(dto.gid(), tileSetService::isGroundGID);
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

        Map<Position, TileObjectId> tiles = new HashMap<>();
        for(EditorLevelDTO object : dto.tiles()){
            TileObjectId gid = new TileObjectId(object.gid(), tileSetService::isGroundGID);
            tiles.put(object.position(), gid);
        }

        level.updateWorldLayerBatch(tiles);

        return levelRepository.save(level);
    }

    /// Edits a single tile in the object layer of a level.
    /// @param userId the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto contains the target position, gid, and optional properties
    /// @return the updated level
    /// @throws NoSuchElementException if level not found
    /// @throws ForbiddenUserException if not level owner
    /// @throws LevelPublishedException if level is published
    /// @throws IllegalArgumentException if position out of bounds, gid invalid, or placement rules violated
    public Level editObjectLayerTile(String userId, String levelId, EditorLevelDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("Level was not found!"));

        level.ensureOwnedBy(userId);
        level.ensureModifiable();
        level.ensureWithinBounds(dto.position());

        TileObjectId tileId = new TileObjectId(dto.gid(), tileSetService::isObjectGID);

        if (tileId.isRemoval()) {
            level.removeObjectLayer(dto.position());
        } else {
            if (level.getWorldLayer().containsKey(dto.position())) {
                throw new IllegalArgumentException("Cannot place object on ground tile");
            }
            if (level.getObjectLayer().containsKey(dto.position())) {
                throw new IllegalArgumentException("Tile already has an object");
            }
            level.putObjectLayer(dto.position(), 
                gameObjectFactory.createGameObject(tileId.value(), dto.position(), dto.getProperties()));
        }
        return levelRepository.save(level);
    }

    /// Updates the properties of an existing object in the object layer.
    /// @param userId the authenticated user's ID
    /// @param levelId the level containing the object
    /// @param dto contains the position and properties to update
    /// @return the updated level
    /// @throws NoSuchElementException if level or object not found
    /// @throws ForbiddenUserException if not level owner
    /// @throws LevelPublishedException if level is published
    /// @throws IllegalArgumentException if property doesn't match object type
    public Level updateObjectProperties(String userId, String levelId, UpdateObjectPropertiesDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("Level not found"));

        level.ensureOwnedBy(userId);
        level.ensureModifiable();

        switch (dto) {
            case BoxPropertyUpdateDTO boxUpdate -> 
                level.updateBoxContent(dto.position(), boxUpdate.content());
            case CoinPropertyUpdateDTO coinUpdate -> {
                int newGid = tileSetService.getGidForTileType(coinUpdate.coinType().tileType());
                level.updateCoinType(dto.position(), newGid, coinUpdate.coinType().coinValue());
            }
            case SnailPropertyUpdateDTO snailUpdate -> 
                level.updateSnailHiding(dto.position(), snailUpdate.isHiding());
        }

        return levelRepository.save(level);
    }
}
