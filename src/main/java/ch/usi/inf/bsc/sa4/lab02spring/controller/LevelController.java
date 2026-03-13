package ch.usi.inf.bsc.sa4.lab02spring.controller;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.levelTesting.UpdateLevelObjectDTO;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    /// must be authorized, and it must have a completeLevelDTO to return a level
    ///
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
    ///
    /// @return list of levels
    ///
    @GetMapping()
    public List<LevelDTO> getLevels() {
        var levels = this.levelService.getAllLevels();
        return levels.stream().map(LevelDTO::new).toList();
    }

    ///
    /// Copies the given level if present and if the user is the creator of such level
    ///
    /// @param authentication abstract token for authentication (either jwt or oauth2)
    /// @param cloneLevelDTO  dto containing the necessary information to clone the level
    /// @return a 200 if user is authenticated and levels exists a 404 if level doesn't exist a 401 if user not authenticated
    ///
    @PostMapping("/clone")
    public ResponseEntity<LevelDTO> cloneLevel(Authentication authentication, @RequestBody CloneLevelDTO cloneLevelDTO) {
        String userId = getUserIdFromAuth(authentication);
        User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return this.levelService.cloneLevel(cloneLevelDTO, user)
                .map(LevelDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    ///
    /// updates the level if present
    ///
    /// @param authentication abstract token for authentication
    /// @param levelId        id of the level being changed
    /// @param dto            data transfer object holding the data to be updated.
    /// @return a 200 if user is authenticated and levels exists a 404 if level doesn't exist a 401 if user not authenticated
    ///
    @PutMapping("/{levelId}/properties")
    public ResponseEntity<Level> updateLevel(Authentication authentication, @PathVariable String levelId, @RequestBody UpdateLevelDTO dto) {
        String userId = getUserIdFromAuth(authentication);
        User creator = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        try {
            return ResponseEntity.ok(this.levelService.updateLevelProperties(creator, levelId, dto));
        } catch (ForbiddenUserException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (LevelPublishedException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Method for testing adding an object into the objectLayer of a given level
//    @PutMapping("{levelId}/object")
//    public ResponseEntity<LevelDTO> updateLevelObject(Authentication authentication, @PathVariable String levelId, @RequestBody UpdateLevelObjectDTO dto){
//        String userId = getUserIdFromAuth(authentication);
//        User creator = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
//        try {
//            Level level = this.levelService.updateLevelObject(creator, levelId, dto);
//            return ResponseEntity.ok(new LevelDTO(level));
//        } catch (ForbiddenUserException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        } catch (LevelPublishedException e){
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//    }
}


