package ch.usi.inf.bsc.sa4.lab02spring.controller;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ObjectLayerResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectPropertiesDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.WorldLayerResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

@RestController
@RequestMapping("/editor")
public class EditorController {
    private final EditorService editorService;

    /// Constructs a new EditorController with the given dependency.
    /// @param editorService the service for handling level editing operations
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
    /// @spec.requires authentication, levelId, and dto are not null.
    /// @spec.modifies the world layer of the level identified by levelId in the repository.
    /// @spec.effects delegates to EditorService to add, replace, or remove a tile
    ///               at the target position in the world layer, then saves the level.
    /// @param authentication authentication token for the current user
    /// @param levelId id of the level to edit
    /// @param dto contains the target position and gid to apply
    /// @return 200 OK with the updated world layer,
    ///         400 BAD_REQUEST if position is out of bounds or gid is invalid,
    ///         403 FORBIDDEN if user is not the level creator,
    ///         404 NOT_FOUND if level doesn't exist
    @PutMapping("/{levelId}/world-layer")
    public ResponseEntity<WorldLayerResponseDTO> editWorldLayerTile(
            Authentication authentication,
            @PathVariable String levelId,
            @RequestBody EditorLevelDTO dto) {
        String userId = getUserIdFromAuth(authentication);
        try {
            Level updated = editorService.editWorldLayerTile(userId, levelId, dto);
            return ResponseEntity.ok(new WorldLayerResponseDTO(updated.getId(), updated.getWorldLayer()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenUserException | LevelPublishedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{levelId}/world-layer/batch")
    public ResponseEntity<WorldLayerResponseDTO> updateWorldLayer(
            Authentication authentication,
            @PathVariable String levelId,
            @RequestBody UpdateWorldLayerDTO dto) {
        String userId = getUserIdFromAuth(authentication);
        try {
            Level updated = editorService.updateWorldLayer(userId, levelId, dto);
            return ResponseEntity.ok(new WorldLayerResponseDTO(updated.getId(), updated.getWorldLayer()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenUserException | LevelPublishedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{levelId}/object-layer")
    public ResponseEntity<ObjectLayerResponseDTO> editObjectLayer(
            Authentication authentication,
            @PathVariable String levelId,
            @RequestBody EditorLevelDTO dto) {
        String userId = getUserIdFromAuth(authentication);
        try {
            Level updated = editorService.editObjectLayerTile(userId, levelId, dto);
            return ResponseEntity.ok(new ObjectLayerResponseDTO(updated.getId(), updated.getObjectLayer()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenUserException | LevelPublishedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    //TODO: LLM generated docs - needs checking
    /// Updates the properties of an existing object in the object layer.
    ///
    /// Uses polymorphic DTOs based on "type" field:
    /// - Box: {"type": "box", "position": {...}, "content": "bronze_coin"}
    /// - Coin: {"type": "coin", "position": {...}, "coinType": "gold"}
    /// - Snail: {"type": "snail", "position": {...}, "isHiding": true}
    ///
    /// Security checks are delegated to the EditorService, which enforces:
    /// - Only the level creator can edit
    /// - Only unpublished levels can be edited
    ///
    /// @param authentication authentication token for the current user
    /// @param levelId id of the level containing the object
    /// @param dto polymorphic DTO with position and type-specific properties
    /// @return 200 OK with the updated object layer,
    ///         403 FORBIDDEN if user is not the level creator or level is published,
    ///         404 NOT_FOUND if level or object doesn't exist
    @PatchMapping("/{levelId}/object-layer/properties")
    public ResponseEntity<ObjectLayerResponseDTO> updateObjectProperties(
            Authentication authentication,
            @PathVariable String levelId,
            @RequestBody UpdateObjectPropertiesDTO dto) {
        String userId = getUserIdFromAuth(authentication);
        try {
            Level updated = editorService.updateObjectProperties(userId, levelId, dto);
            return ResponseEntity.ok(new ObjectLayerResponseDTO(updated.getId(), updated.getObjectLayer()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenUserException | LevelPublishedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
