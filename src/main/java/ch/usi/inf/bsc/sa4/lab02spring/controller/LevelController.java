package ch.usi.inf.bsc.sa4.lab02spring.controller;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;


import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.*;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.PublishedLevelSortBy;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.converter.LayerToTiledMapConverter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/levels")
public class LevelController {
    private final LevelService levelService;
    private final UserService userService;
    private final TileSetService tileSetService;
    private final LayerToTiledMapConverter layerToTiledMapConverter;

  /// Constructs a new LevelController with the given dependencies.
  /// @param levelService the service for managing level operations
  /// @param userService the service for accessing user data
    @Autowired
    public LevelController(LevelService levelService, UserService userService, TileSetService tileSetService, LayerToTiledMapConverter layerToTiledMapConverter) {
        this.levelService = levelService;
        this.userService = userService;
        this.tileSetService = tileSetService;
        this.layerToTiledMapConverter = layerToTiledMapConverter;
    }

  /// Creates a new empty level and returns a level DTO.
  /// @spec.requires authentication and createLevelDTO are not null.
  /// @spec.effects saves a new level to the repository with the authenticated user as creator.
  /// @param authentication abstract token for authentication
  /// @param createLevelDTO the DTO containing the necessary information to create a new level
  /// @return a 200 OK response containing the created level as a LevelDTO
  /// @throws UserNotFoundException if the authenticated user does not exist
    @PostMapping()
    public ResponseEntity<LevelDTO> createLevel(Authentication authentication, @RequestBody CreateLevelDTO createLevelDTO) {
        String userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(new LevelDTO(this.levelService.createLevel(createLevelDTO, userId)));
    }

  /// Returns a list of all levels present in the collection.
  /// @return a list of all levels as LevelDTOs
    @GetMapping()
    public List<LevelDTO> getLevels() {
        var levels = this.levelService.getAllLevels();
        return levels.stream().map(LevelDTO::new).toList();
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

    /// Returns the thumbnail image for the given level.
    ///
    /// @spec.requires levelId is not null.
    /// @spec.effects fetches the stored thumbnail bytes for the target level and
    ///               returns them as an image/png response body.
    /// @param levelId the id of the level whose thumbnail is requested
    /// @return a 200 OK response containing the thumbnail image bytes
    /// @deprecated
    @GetMapping(value = "/{levelId}/thumbnail", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getThumbnail(@PathVariable String levelId) {
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(this.levelService.getThumbnailForLevel(levelId));
    }

    /// @deprecated
    @PutMapping(value = "/{levelId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateThumbnail(
            final Authentication authentication,
            @PathVariable final String levelId,
            @RequestParam("thumbnail") final MultipartFile thumbnail) {
        final String userId = getUserIdFromAuth(authentication);
        this.levelService.saveThumbnailForLevel(userId, levelId, thumbnail);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<LevelDTO> cloneLevel(Authentication authentication, @RequestBody CloneLevelDTO cloneLevelDTO) {
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
    /// @return a 200 OK response containing the updated level
    /// @throws UserNotFoundException if the authenticated user does not exist
    /// @throws ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException if the
    ///         target level does not exist
    /// @throws ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException if the
    ///         authenticated user does not own the target level
    /// @throws ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException if the
    ///         target level is already published
    @PutMapping("/{levelId}/properties")
    public ResponseEntity<Level> updateLevel(Authentication authentication, @PathVariable String levelId, @RequestBody UpdateLevelDTO dto) {
        String userId = getUserIdFromAuth(authentication);
        User creator = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(this.levelService.updateLevelProperties(creator, levelId, dto));
    }

    // TODO: add jsdoc...
    @DeleteMapping("/{levelId}")
    public ResponseEntity<Void> deleteLevel(
            Authentication authentication,
            @PathVariable String levelId) {
        String userId = getUserIdFromAuth(authentication);
        this.levelService.deleteLevel(userId, levelId);
        return ResponseEntity.noContent().build();
    }

    /// Unpublishes a level owned by the authenticated user.
    /// @param authentication the current authenticated user
    /// @param levelId the ID of the level to unpublish
    /// @return 200 OK with the updated level
    /// @throws LevelNotFoundException if the level does not exist
    /// @throws ForbiddenUserException if the authenticated user is not the owner of the level
    @PutMapping("/{levelId}/unpublish")
    public ResponseEntity<Level> unpublishLevel(
            Authentication authentication,
            @PathVariable String levelId) {
        String userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(this.levelService.unpublishLevel(userId, levelId));
    }

    /// Publishes the specified level.
    ///
    /// @spec.requires authentication and levelId are not null.
    /// @spec.effects forwards the authenticated user and target level id to the
    ///               level service, which publishes the level.
    /// @param authentication the current authenticated user
    /// @param levelId the id of the target level
    /// @return a 200 OK response containing the published level
    @PutMapping(path = "/{levelId}/publish")
    public ResponseEntity<Level> publishLevel(
            Authentication authentication,
            @PathVariable String levelId) {
        String userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(this.levelService.publish(userId, levelId));
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
        final String userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(this.levelService.submitAttempt(levelId, userId, dto));
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
            final String userId = getUserIdFromAuth(authentication);
            final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
            return ResponseEntity.ok(this.levelService.getPlayableMap(user,levelId));
        }
}
