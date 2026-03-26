package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.*;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
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

    /// Constructs a new LevelController with the given dependencies.
    /// 
    /// @param levelService the service for managing level operations
    /// @param userService  the service for accessing user data
    @Autowired
    public LevelController(LevelService levelService, UserService userService) {
        this.levelService = levelService;
        this.userService = userService;
    }

    /// Creates a new empty level and returns a level DTO.
    /// 
    /// @spec.requires authentication and createLevelDTO are not null.
    /// @spec.effects saves a new level to the repository with the authenticated user
    ///               as creator.
    /// @param authentication abstract token for authentication
    /// @param createLevelDTO the DTO containing the necessary information to create
    ///                       a new level
    /// @return a 200 OK response containing the created level as a LevelDTO
    /// @throws UserNotFoundException if the authenticated user does not exist
    @PostMapping()
    public ResponseEntity<LevelDTO> createLevel(Authentication authentication,
            @RequestBody CreateLevelDTO createLevelDTO) {
        String userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(new LevelDTO(this.levelService.createLevel(createLevelDTO, userId)));
    }

    /// Returns all published levels as summaries, sorted by the given criteria.
    /// 
    /// @param sortBy sorting strategy (required): CLEAR_RATE or POPULARITY
    /// @param period time range for popularity calculation (optional, default
    ///               ALL_TIME): ALL_TIME, TODAY, LAST_7_DAYS, LAST_30_DAYS,
    ///               LAST_365_DAYS. Only relevant when sortBy is POPULARITY; ignored
    ///               for CLEAR_RATE.
    /// @return a list of published levels sorted by the specified criteria
    @GetMapping("/published")
    public List<LevelSummaryDto> getPublishedLevels(
            @RequestParam PublishedLevelSortBy sortBy,
            @RequestParam(defaultValue = "ALL_TIME") DateRangePreset period) {
        return this.levelService.getPublishedLevels(sortBy, period);
    }

    /// Clones the given level if it exists and the authenticated user is its
    /// creator.
    /// 
    /// @spec.requires authentication and cloneLevelDTO are not null.
    /// @spec.effects saves a clone of the level to the repository with the user as
    ///               the new creator.
    /// @param authentication abstract token for authentication
    /// @param cloneLevelDTO  the DTO containing the id of the level to clone
    /// @return a 200 OK response containing the cloned level as a LevelDTO, or a 403
    ///         Forbidden response if the level does not exist or does not belong to
    ///         the user
    /// @throws UserNotFoundException if the authenticated user does not exist
    @PostMapping("/clone")
    public ResponseEntity<LevelDTO> cloneLevel(Authentication authentication,
            @RequestBody CloneLevelDTO cloneLevelDTO) {
        String userId = getUserIdFromAuth(authentication);
        User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return this.levelService.cloneLevel(cloneLevelDTO, user)
                .map(LevelDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    /// Updates the properties of an existing unpublished level owned by the
    /// authenticated user.
    /// 
    /// @spec.requires authentication, levelId, and dto are not null.
    /// @spec.modifies the level identified by levelId in the repository.
    /// @spec.effects updates the title, description, and/or clear condition of the
    ///               level
    /// @param authentication abstract token for authentication
    /// @param levelId        the id of the level to update
    /// @param dto            the DTO containing the optional new values for title,
    ///                       description, and clear condition
    /// @return a 200 OK response containing the updated level, a 403 Forbidden
    ///         response if the level does not belong to the authenticated user, or a
    ///         403 Forbidden response if the level is already published
    /// @throws UserNotFoundException if the authenticated user does not exist
    @PutMapping("/{levelId}/properties")
    public ResponseEntity<Level> updateLevel(Authentication authentication, @PathVariable String levelId,
            @RequestBody UpdateLevelDTO dto) {
        String userId = getUserIdFromAuth(authentication);
        User creator = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(this.levelService.updateLevelProperties(creator, levelId, dto));
    }
}
