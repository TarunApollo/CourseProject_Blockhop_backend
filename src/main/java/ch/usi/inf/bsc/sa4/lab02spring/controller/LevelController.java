package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelSummaryDto;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAggregationService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPlayService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPublishService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/levels")
@SuppressWarnings("PMD.ExcessiveImports")
public class LevelController {
    /// Handles level creation, publication, and play-related endpoints.
    private final LevelService levelService;
    /// Produces sorted summaries for published levels.
    private final LevelAggregationService levelAggregationService;
    /// Manages publish and unpublish transitions.
    private final LevelPublishService levelPublishService;
    /// Handles playable map retrieval and attempt submission.
    private final LevelPlayService levelPlayService;
    /// Resolves authenticated users for controller actions.
    private final UserService userService;

    /// Constructs a new LevelController with the given dependencies.
    /// @param levelService the service for managing core level operations
    /// @param levelAggregationService the service for published level summaries
    /// @param levelPublishService the service for publish and unpublish actions
    /// @param levelPlayService the service for play flow and submissions
    /// @param userService the service for accessing user data
    @Autowired
    public LevelController(
            final LevelService levelService,
            final LevelAggregationService levelAggregationService,
            final LevelPublishService levelPublishService,
            final LevelPlayService levelPlayService,
            final UserService userService) {
        this.levelService = levelService;
        this.levelAggregationService = levelAggregationService;
        this.levelPublishService = levelPublishService;
        this.levelPlayService = levelPlayService;
        this.userService = userService;
    }

  /// Creates a new empty level and returns a level DTO.
  /// @spec.requires authentication and createLevelDTO are not null.
  /// @spec.effects saves a new level to the repository with the authenticated user as creator.
  /// @param authentication abstract token for authentication
    /// @param createLevelDTO the DTO containing the necessary information to create a new level
    /// @return a 200 OK response containing the created level as a LevelDTO
    /// @throws UserNotFoundException if the authenticated user does not exist
    @PostMapping()
    public ResponseEntity<LevelDTO> createLevel(final Authentication authentication, @RequestBody final CreateLevelDTO createLevelDTO) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
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
            @RequestParam final PublishedLevelSortBy sortBy,
            @RequestParam(defaultValue = "ALL_TIME") final DateRangePreset period) {
        return this.levelAggregationService.getPublishedLevels(sortBy, period);
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
    public ResponseEntity<LevelDTO> cloneLevel(final Authentication authentication, @RequestBody final CloneLevelDTO cloneLevelDTO) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
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
    /// @return a 200 OK response containing the updated level
    /// @throws UserNotFoundException if the authenticated user does not exist
    /// @throws ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException if the
    ///         target level does not exist
    /// @throws ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException if the
    ///         authenticated user does not own the target level
    /// @throws ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException if the
    ///         target level is already published
    @PutMapping("/{levelId}/properties")
    public ResponseEntity<LevelDTO> updateLevel(final Authentication authentication, @PathVariable final String levelId, @RequestBody final UpdateLevelDTO dto) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final User creator = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(new LevelDTO(this.levelService.updateLevelProperties(creator, levelId, dto)));
    }

    // TODO: add jsdoc...
    @DeleteMapping("/{levelId}")
    public ResponseEntity<Void> deleteLevel(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        this.levelService.deleteLevel(userId, levelId);
        return ResponseEntity.noContent().build();
    }

    /// Unpublishes a level owned by the authenticated user.
    /// @param authentication the current authenticated user
    /// @param levelId the ID of the level to unpublish
    /// @return 204 No Content when the level is unpublished successfully
    /// @throws LevelNotFoundException if the level does not exist
    /// @throws ForbiddenUserException if the authenticated user is not the owner of the level
    @PutMapping("/{levelId}/unpublish")
    public ResponseEntity<Void> unpublishLevel(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        this.levelPublishService.unpublishLevel(userId, levelId);
        return ResponseEntity.noContent().build();
    }

    /// Publishes the specified level.
    ///
    /// @spec.requires authentication and levelId are not null.
    /// @spec.effects forwards the authenticated user and target level id to the
    ///               level service, which publishes the level.
    /// @param authentication the current authenticated user
    /// @param levelId the id of the target level
    /// @return a 204 No Content response when the level is published successfully
    @PutMapping(path = "/{levelId}/publish")
    public ResponseEntity<Void> publishLevel(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        this.levelPublishService.publish(userId, levelId);
        return ResponseEntity.noContent().build();
    }

    /// Submits an attempt for the specified level on behalf of the authenticated
    /// user.
    ///
    /// @spec.requires authentication, levelId, and dto are not null.
    /// @spec.effects validates the attempt against the level state; if the level
    ///               is unpublished and owned by the authenticated user and the
    ///               attempt is successful, marks the level as publish eligible;
    ///               records the attempt through the attempt service.
    /// @param authentication abstract token for authentication
    /// @param levelId        the id of the level to submit an attempt for
    /// @param dto            the DTO containing the attempt details
    /// @return a 200 OK response containing the result of the attempt, or a 403
    ///         Forbidden response if the submission fails
    /// @throws UserNotFoundException if no user with the authenticated id exists
    /// @throws LevelNotFoundException if no level with the given levelId exists
    /// @throws ForbiddenLevelActionException
    ///         if the level is unpublished and the authenticated user is not
    ///         its creator
    /// @throws ForbiddenUserException if the authenticated user is not the
    ///         owner of the level when marking it as publish eligible
    @PostMapping("/{levelId}/submit")
    public ResponseEntity<String> submitAttempt(
            final Authentication authentication,
            @PathVariable final String levelId,
            @RequestBody final AttemptDTO dto) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        return ResponseEntity.ok(this.levelPlayService.handleLevelSubmission(levelId, userId, dto));
    }


    /// Returns a Tiled/Phaser-compatible map for the requested playable level.
    ///
    /// @spec.requires authentication and levelId are not null.
    /// @spec.effects resolves the authenticated user and delegates to the level
    ///               service to validate access and export the requested level
    ///               as a frontend-consumable map structure.
    /// @param authentication abstract token for authentication
    /// @param levelId the id of the level to load for play
    /// @return a 200 OK response containing a Tiled/Phaser-compatible map payload
    /// @throws UserNotFoundException if the authenticated user does not exist
    @GetMapping("/play/{levelId}/map")
    public ResponseEntity<Map<String,Object>> getMap(
        final Authentication authentication,
        @PathVariable final String levelId)
        {
            final String userId = AuthUtils.getUserIdFromAuth(authentication);
            final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
            return ResponseEntity.ok(this.levelPlayService.getPlayableMap(user, levelId));
        }
}
