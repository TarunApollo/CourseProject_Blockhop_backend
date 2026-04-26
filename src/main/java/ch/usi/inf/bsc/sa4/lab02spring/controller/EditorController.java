package ch.usi.inf.bsc.sa4.lab02spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import java.lang.IllegalArgumentException;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ObjectLayerResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateObjectPropertiesDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateWorldLayerDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.WorldLayerResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;

/// REST controller for editing level data in the editor.
@RestController
@RequestMapping("/editor")
public class EditorController {

    /// Service used to perform editor operations.
    ///
    private final EditorService editorService;

    /// Creates a new editor controller.
    /// 
    /// @param editorService service used to apply editor updates
    ///
    @Autowired
    public EditorController(final EditorService editorService) {
        this.editorService = editorService;
    }

    /// Replaces the entire world layer of an unpublished level. The request body
    /// contains all tiles that should exist in the world layer. Tiles not present in
    /// the request are implicitly deleted (removed from the layer).
    ///
    /// Security checks:
    /// - Only the level creator can edit
    /// - Only unpublished levels can be edited
    /// - All positions must be within level bounds
    /// - All GIDs must be valid ground tile GIDs
    ///
    /// @param authentication authentication token for the current user
    /// @param levelId        id of the level to edit
    /// @param dto            contains the list of tiles (position + gid)
    ///                       representing the new world layer state
    /// @return 200 OK with the updated world layer
    /// @throws LevelNotFoundException   if level not found
    /// @throws ForbiddenUserException   if not level owner
    /// @throws IllegalArgumentException if any position is out of bounds or any gid
    ///                                  is invalid
    @PutMapping("/{levelId}/world-layer")
    public ResponseEntity<WorldLayerResponseDTO> replaceWorldLayer(
            final Authentication authentication,
            @PathVariable final String levelId,
            @RequestBody final UpdateWorldLayerDTO dto) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final Level updated = editorService.replaceWorldLayer(userId, levelId, dto);
        return ResponseEntity.ok(new WorldLayerResponseDTO(updated.getId(), updated.getWorldLayer()));
    }

    /// Replaces the entire object layer of an unpublished level. The request body
    /// contains all objects that should exist in the object layer. Objects not
    /// present in the request are implicitly deleted (removed from the layer).
    ///
    /// Security checks:
    /// - Only the level creator can edit
    /// - Only unpublished levels can be edited
    /// - All positions must be within level bounds
    /// - All GIDs must be valid object GIDs
    /// - No duplicate positions allowed in the request
    ///
    /// JSON example: { "objects": [ {"position": {"x": 1, "y": 2}, "gid": 42,
    /// "content": {"type": "Item_Coin_Gold"}}, {"position": {"x": 3, "y": 4}, "gid":
    /// 42}, {"position": {"x": 5, "y": 6}, "gid": 50} ] }
    ///
    /// Note: "content" is optional per object. Only boxes use it.
    ///
    /// @param authentication authentication token for the current user
    /// @param levelId        id of the level to edit
    /// @param dto            list of objects with position, gid, and optional
    ///                       content
    /// @return 200 OK with the updated object layer
    /// @throws LevelNotFoundException   if not found
    /// @throws ForbiddenUserException   if not owner
    /// @throws LevelPublishedException  if published
    /// @throws IllegalArgumentException if invalid position, gid, or
    ///                                  duplicate positions
    @PutMapping("/{levelId}/object-layer")
    public ResponseEntity<ObjectLayerResponseDTO> replaceObjectLayer(
            final Authentication authentication,
            @PathVariable final String levelId,
            @RequestBody final UpdateObjectLayerDTO dto) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final Level updated = editorService.replaceObjectLayer(userId, levelId, dto);
        return ResponseEntity.ok(new ObjectLayerResponseDTO(updated.getId(), updated.getObjectLayer()));
    }

    /// Updates the properties of an existing object in the object layer.
    ///
    /// JSON examples:
    /// - Update box content: {"type": "box", "position": {"x": 1, "y": 2},
    ///   "content": {"type": "Item_Coin_Bronze"}}
    /// - Empty box content: {"type": "box", "position": {"x": 1, "y": 2}, "content":
    ///   {}}
    ///
    /// @param authentication authentication token for the current user
    /// @param levelId        id of the level containing the object
    /// @param dto            polymorphic DTO with position and type-specific
    ///                       properties
    /// @return 200 OK with the updated object layer
    /// @throws LevelNotFoundException   if level not found
    /// @throws ForbiddenUserException   if not level owner
    /// @throws LevelPublishedException  if level is published
    /// @throws IllegalArgumentException if property doesn't match object type
    @PatchMapping("/{levelId}/object-layer/properties")
    public ResponseEntity<ObjectLayerResponseDTO> updateObjectProperties(
            final Authentication authentication,
            @PathVariable final String levelId,
            @RequestBody final UpdateObjectPropertiesDTO dto) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final Level updated = editorService.updateObjectProperties(userId, levelId, dto);
        return ResponseEntity.ok(new ObjectLayerResponseDTO(updated.getId(), updated.getObjectLayer()));
    }
}
