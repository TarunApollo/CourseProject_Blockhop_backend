package ch.usi.inf.bsc.sa4.lab02spring.controller;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import ch.usi.inf.bsc.sa4.lab02spring.service.EditorService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/levels")
public class LevelController {
  private final LevelService levelService;

  @Autowired
  public LevelController(LevelService levelService, EditorService editorService) {
    this.levelService = levelService;
  }

  @PostMapping()
  public ResponseEntity<LevelDTO> createLevel(Authentication authentication, @RequestBody CreateLevelDTO createLevelDTO) {
    String userId = getUserIdFromAuth(authentication);
    if (userId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return ResponseEntity.ok(new LevelDTO(this.levelService.createLevel(createLevelDTO, userId)));
  }

  @GetMapping()
  public List<LevelDTO> getLevels() {
    var levels = this.levelService.getAllLevels();
    return levels.stream().map(LevelDTO::new).toList();
  }

  @PostMapping("/clone")
  public ResponseEntity<LevelDTO> cloneLevel(Authentication authentication, @RequestBody CloneLevelDTO cloneLevelDTO) {
    String userId = getUserIdFromAuth(authentication);
    if (userId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return this.levelService.cloneLevel(cloneLevelDTO, userId)
            .map(LevelDTO::new)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
  }

  private String getUserIdFromAuth(Authentication authentication) {
    if (authentication == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    } else {
      Object principal = authentication.getPrincipal();
      if (principal instanceof Jwt jwt) {
        return jwt.getClaimAsString("sub");
      } else if (principal instanceof OAuth2User oauth2User) {
        if (oauth2User.getAttribute("sub") == null) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return oauth2User.getAttribute("sub");
      }
      throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }
  }
}
