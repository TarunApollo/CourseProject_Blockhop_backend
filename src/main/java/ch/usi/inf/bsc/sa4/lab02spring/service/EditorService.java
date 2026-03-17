package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import java.util.Map;
import java.util.NoSuchElementException;



@Service
public class EditorService {

    private final LevelRepository levelRepository;

    /// Constructs a new EditorService with the given dependency.
    /// @param levelRepository the repository for accessing level data
    @Autowired
    public EditorService(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    /// Edits a tile in the world layer of a level.
    ///
    /// Tile operations:
    /// - If gid > 0 and the position is empty, a tile is added (put operation)
    /// - If gid > 0 and the position already contains a tile, that tile is replaced (put operation)
    /// - If gid == 0, the tile at that position is removed (remove operation); if no tile exists, this is a no-op
    ///
    /// Security enforcement:
    /// - Only the level creator can edit (returns 401 UNAUTHORIZED otherwise)
    /// - Only unpublished levels can be edited (throws LevelPublishedException otherwise)
    /// - Coordinates must be within level bounds: 0 ≤ x < width, 0 ≤ y < height (returns 400 BAD_REQUEST otherwise)
    ///
    /// /// @spec.requires userId, levelId, and dto are not null.
    /// @spec.requires dto contains a non-null position and a valid gid.
    /// @spec.modifies the world layer of the level identified by levelId in the repository.
    /// @spec.effects if gid > 0, adds or replaces the ground object at the target position.
    ///               If gid == 0, removes the ground object at the target position if one exists.
    ///               Saves the updated level to the repository.
    /// @param userId the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto contains the target position and gid
    /// @return the updated level
    /// @throws NoSuchElementException if no level with the given id exists
    /// @throws SecurityException if the user is not the creator of the level
    /// @throws LevelPublishedException if the level is already published
    /// @throws IllegalArgumentException if the position is null or the coordinates
    ///         are out of bounds (x must be in [0, 256), y must be in [0, 14))
    public Level editWorldLayerTile(String userId, String levelId, EditorLevelDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("Level was not found!"));

        // Security check: only creator can edit
        if (!userId.equals(level.getCreator())) {
             throw new SecurityException("Not the creator");
        }

        // Security check: only unpublished levels can be edited
        if (level.isPublished()) {
            throw new LevelPublishedException("Level is already published!");
        }
        Position targetPosition = dto.position();
        if (targetPosition == null){
            throw new IllegalArgumentException("Position required!");
        }
        if (targetPosition.x() < 0 || targetPosition.x() >= 256 || targetPosition.y() < 0 || targetPosition.y() >= 14) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        // Tile operation: gid == 0 means remove, gid > 0 means add/replace
        Map<Position, GroundObject> worldLayer = level.getWorldLayer();
        if (dto.gid() == 0) {
            worldLayer.remove(targetPosition);
        } else {
            worldLayer.put(targetPosition, new GroundObject(dto.gid()));
        }
        return levelRepository.save(level);
    }
}