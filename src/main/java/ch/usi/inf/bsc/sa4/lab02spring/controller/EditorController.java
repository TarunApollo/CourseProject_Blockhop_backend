package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.EditorLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;

@RestController
@RequestMapping("/editor")
public class EditorController {
    private final LevelRepository levelRepository;
    private final LevelService levelService;
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
  /// Allows creator to edit unpublished level
  /// @param authentication abstract token for authentication
  /// @param levelId id of the level being changed
  /// @param dto data tranfer object holding the data to be updated.
  /// @return a 200 if user is authenticated,is the creator, the level is unpublished and levels exists. Otherwise a 404 if level doesn't exist and a 401 if user not authenticated, the level is published, or is not the creator
  /// 
  

    @PutMapping("/{levelId}")
  public ResponseEntity<Level> editLevel(Authentication authentication, @PathVariable String levelId, @RequestBody EditorLevelDTO dto){
    String userId = getUserIdFromAuth(authentication);
    String creatorId = this.levelService.creator(levelId);
    Level level = this.levelService.getLevelbyId(levelId);
    Boolean isPublished = this.levelService.published(level);
    //
    // also, you dont have to worry about any of this logic here, 
    // just replicate whatever I did in for the level controller 
    //
    // && should be ||, but you can remove this as im checking this already
    if(creatorId.equals(userId) == false && !isPublished){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } 
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // will be different but i will update it with the editorService methods

}
}
