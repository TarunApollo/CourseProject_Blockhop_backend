package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/editor")
public class EditorController {
    private final EditorService editorService;

    @Autowired
    public EditorController(EditorService editorService) {
        this.editorService = editorService;
    }

    /// Edits a tile in the world layer of an unpublished level.
    ///
    /// Behavior:
    /// - If gid > 0 and the position is empty, a tile is added
    /// - If gid > 0 and the position already contains a tile, that tile is replaced
    /// - If gid == 0, the tile at that position is removed; if no tile exists, the operation is a no-op
    ///
    /// Security checks are delegated to the EditorService, which enforces:
    /// - Only the level creator can edit
    /// - Only unpublished levels can be edited
    /// - Coordinates must remain within level bounds (0 to width-1, 0 to height-1)
    ///
    /// @param authentication authentication token for the current user
    /// @param levelId id of the level to edit
    /// @param dto contains the target position and gid to apply
    /// @return 200 OK with the updated level, 401 if unauthorized, 403 if the level is published,
    ///         404 if level not found, 400 if coordinates are out of bounds
    @PutMapping("/{levelId}/world-layer")
    public ResponseEntity<Level> editLevel(Authentication authentication, @PathVariable String levelId, @RequestBody EditorLevelDTO dto) {
        String userId = getUserIdFromAuth(authentication);
        try {
            return ResponseEntity.ok(editorService.editWorldLayerTile(userId, levelId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
