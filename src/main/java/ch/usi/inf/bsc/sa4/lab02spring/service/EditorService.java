package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundTileId;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@Service
public class EditorService {

    private final LevelRepository levelRepository;


    private final TileSetService tileSetService;
    /// Constructs a new EditorService with the given dependency.
    /// @param levelRepository the repository for accessing level data
    @Autowired
    public EditorService(LevelRepository levelRepository, TileSetService tileSetService ) {
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
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

        // Security check: only creator can edit (throws ForbiddenUserException if not owner)
        level.ensureOwnedBy(userId);

        // Construct validated GroundTileId value object (validation happens here in service layer)
        GroundTileId gid = dto.gid() == 0
                ? GroundTileId.remove()
                : new GroundTileId(dto.gid(), tileSetService::isGroundGID);

        // Delegate to entity with validated value object (entity stays pure)
        level.updateWorldLayerTile(dto.position(), gid);

        return levelRepository.save(level);
    }

    //TODO: write spec
    /// Batch updates multiple tiles in the world layer of a level
    /// @param userId the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto contains the map of positions to gids to apply
    /// @return the updated level
    /// @throws NoSuchElementException if level not found
    /// @throws ForbiddenUserException if not level owner
    /// @throws LevelPublishedException if level is published
    /// @throws IllegalArgumentException if any position is out of bounds or any gid is invalid
    public Level updateWorldLayer(String userId, String levelId, UpdateWorldLayerDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("Level not found"));

        // Security check: only creator can edit (throws ForbiddenUserException if not owner)
        level.ensureOwnedBy(userId);

        // Construct validated GroundTileId value objects (validation happens here in service layer)
//        Map<Position, GroundTileId> tiles = dto.tiles().stream()
//                .collect(Collectors.toMap(
//                        e -> e.position(),
//                        e -> e.gid() == 0
//                                ? GroundTileId.remove()
//                                : new GroundTileId(e.gid(), tileSetService::isGroundGID)
//                ));

        Map<Position, GroundTileId> tiles = new HashMap<>();
        for (EditorLevelDTO object : dto.tiles()) {
            GroundTileId gid = object.gid() == 0
                    ? GroundTileId.remove()
                    : new GroundTileId(object.gid(), tileSetService::isGroundGID);
            tiles.put(object.position(), gid);
        }

        level.updateWorldLayerBatch(tiles);

        return levelRepository.save(level);
    }
}