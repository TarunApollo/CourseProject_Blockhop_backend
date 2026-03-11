package ch.usi.inf.bsc.sa4.lab02spring.controller;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;

import java.util.List;

@RestController
@RequestMapping("/levels")
public class LevelController {
  private final LevelService levelService;
  private final UserService userService;
  @Autowired
  public LevelController(LevelService levelService, UserService userService) {
    this.levelService = levelService;
    this.userService = userService;
  }
  ///
  /// Creates a new empty level and returns a level dto
  /// must be authorised and it must have a completeLevelDTO to return a level
  /// @param authentication abstract token for authentication (either jwt or oauth2)
  /// @param createLevelDTO dto containing the necessary information to create a brand-new level
  /// @return a 200 if OK otherwise a 401 if the user is not authenticated
  /// 
  @PostMapping()
  public ResponseEntity<LevelDTO> createLevel(Authentication authentication, @RequestBody CreateLevelDTO createLevelDTO) {
    String userId = getUserIdFromAuth(authentication);
    return ResponseEntity.ok(new LevelDTO(this.levelService.createLevel(createLevelDTO, userId)));
  }
  ///
  /// Return a list of the levels present in the collection
  /// a parameterless method that returns all the available levels
  /// @return list of levels
  /// 
  @GetMapping()
  public List<LevelDTO> getLevels() {
    var levels = this.levelService.getAllLevels();
    return levels.stream().map(LevelDTO::new).toList();
  }
  ///
  /// Copies the given level if present and if the user is the creator of such level
  /// @param authentication abstract token for authentication (either jwt or oauth2)
  /// @param cloneLevelDTO dto containing the necessary information to clone the level
  /// @return a 200 if user is authenticated and levels exists a 404 if level doesn't exist a 401 if user not authenticated
  /// 
  @PostMapping("/clone")
  public ResponseEntity<LevelDTO> cloneLevel(Authentication authentication, @RequestBody CloneLevelDTO cloneLevelDTO) {
    String userId = getUserIdFromAuth(authentication);
    var user = this.userService.getById(userId).get();
    return this.levelService.cloneLevel(cloneLevelDTO, user)
            .map(LevelDTO::new)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }
  ///
  /// updates the level if present 
  /// @param authentication abstract token for authentication
  /// @param levelId id of the level being changed
  /// @param dto dtat tranfer object holding the data to be updated.
  /// @return a 200 if user is authenticated and levels exists a 404 if level doesn't exist a 401 if user not authenticated
  /// 
  
//  @PutMapping("/{levelId}/properties")
//  public ResponseEntity<Level> updateLevel(Authentication authentication, @PathVariable String levelId, @RequestBody UpdateLevelDTO dto) {
//    String userId = getUserIdFromAuth(authentication);
//    return ResponseEntity.ok(this.levelService.updateLevelProperties(userId,levelId, dto));
//  }
}
