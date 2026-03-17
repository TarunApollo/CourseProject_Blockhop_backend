package ch.usi.inf.bsc.sa4.lab02spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;

import java.util.NoSuchElementException;


@Service
public class EditorService {

    private final LevelRepository levelRepository;
    private final TileSetService tileSetService;
    @Autowired
    public EditorService(LevelRepository levelRepository, TileSetService tileSetService ) {
        this.levelRepository = levelRepository;
        this.tileSetService = tileSetService;
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
    /// @param userId the authenticated user's ID
    /// @param levelId the level to edit
    /// @param dto contains the target position and gid
    /// @return the updated level
    /// @throws ResponseStatusException if level not found (404), unauthorized (401), or coordinates out of bounds (400)
    /// @throws LevelPublishedException if the level is already published
    public Level editWorldLayerTile(String userId, String levelId, EditorLevelDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("Level was not found!"));

        // Security check: only creator can edit
        level.ensureOwnedBy(userId); // Throws ForbiddenUserException if not owner
        // Security check: only unpublished levels can be edited
        level.ensureModifiable();    

        // Tile operation: gid == 0 means remove, gid > 0 means add/replace
        if (dto.gid() == 0) {
            level.removeGroundObject(dto.position());
        } else {
            if (!tileSetService.isGroundGID(dto.gid())) {
                throw new IllegalArgumentException("Not a ground tile");
            }
            level.putWorldLayer(dto.position(), new GroundObject(dto.gid()));
        }
        return levelRepository.save(level);
    }
}