package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.SetLevelAttitudeDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelAttitudeService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// REST endpoints for level CRUD operations (create, clone, update, delete).
@RestController
@RequestMapping("/levels")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed singleton; injected services are intentionally shared")
public class LevelController {

    /// Handles core CRUD operations on levels.
    private final LevelService levelService;
    /// Resolves authenticated users for controller actions.
    private final UserService userService;
    /// Handles operations related to level attitudes (likes and dislikes).
    private final LevelAttitudeService levelAttitudeService;

    /// Constructs a new LevelController.
    /// 
    /// @param levelService         the service for managing core level operations
    /// @param userService          the service for accessing user data
    /// @param levelAttitudeService the service for managing level attitudes
    @Autowired
    public LevelController(
            final LevelService levelService,
            final UserService userService,
            final LevelAttitudeService levelAttitudeService) {
        this.levelService = levelService;
        this.userService = userService;
        this.levelAttitudeService = levelAttitudeService;
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
    public ResponseEntity<LevelDTO> createLevel(
            final Authentication authentication,
            @RequestBody final CreateLevelDTO createLevelDTO) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        return ResponseEntity.ok(new LevelDTO(this.levelService.createLevel(createLevelDTO, userId)));
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
    public ResponseEntity<LevelDTO> cloneLevel(
            final Authentication authentication,
            @RequestBody final CloneLevelDTO cloneLevelDTO) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return this.levelService.cloneLevel(cloneLevelDTO, user)
                .map(LevelDTO::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
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
    /// @param dto            the DTO containing the optional new values
    /// @return a 200 OK response containing the updated level
    /// @throws UserNotFoundException  if the authenticated user does not exist
    /// @throws LevelNotFoundException if the target level does not exist
    /// @throws ForbiddenUserException if the authenticated user does not own
    ///                                the level
    @PutMapping("/{levelId}/properties")
    public ResponseEntity<LevelDTO> updateLevel(
            final Authentication authentication,
            @PathVariable final String levelId,
            @RequestBody final UpdateLevelDTO dto) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final User creator = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(new LevelDTO(this.levelService.updateLevelProperties(creator, levelId, dto)));
    }

    /// Deletes a level owned by the authenticated user.
    /// 
    /// @param authentication the current authenticated user
    /// @param levelId        the ID of the level to delete
    /// @return 204 No Content when the level is deleted successfully
    /// @throws LevelNotFoundException if the level does not exist
    /// @throws ForbiddenUserException if the user is not the owner of the level
    @DeleteMapping("/{levelId}")
    public ResponseEntity<Void> deleteLevel(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        this.levelService.deleteLevel(userId, levelId);
        return ResponseEntity.noContent().build();
    }

    /// Sets or updates the authenticated user's attitude (like/dislike) towards a
    /// level. If the user already has an attitude for the level, it will be updated.
    /// Otherwise, a new attitude record will be created.
    /// 
    /// @param authentication the current authenticated user
    /// @param levelId        the ID of the level to set attitude for
    /// @param dto            the DTO containing the attitude (like or dislike)
    /// @return 200 OK when the attitude is set successfully, with the updated
    ///         like/dislike counts
    /// @throws UserNotFoundException  if the authenticated user does not exist
    /// @throws LevelNotFoundException if the level does not exist
    @PutMapping("/{levelId}/attitude")
    public ResponseEntity<Void> updateLevelAttitude(
            final Authentication authentication,
            @PathVariable final String levelId,
            @RequestBody final SetLevelAttitudeDTO dto) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        this.levelAttitudeService.setAttitude(userId, levelId, dto.attitude());
        return ResponseEntity.ok().build();
    }

    /// Deletes the authenticated user's attitude (like/dislike) towards a level if
    /// it exists.
    /// 
    /// @param authentication the current authenticated user
    /// @param levelId        the ID of the level to delete attitude for
    /// @return 204 No Content when the attitude is deleted successfully or if no
    ///         attitude existed
    /// @throws UserNotFoundException  if the authenticated user does not exist
    /// @throws LevelNotFoundException if the level does not exist
    @DeleteMapping("/{levelId}/attitude")
    public ResponseEntity<Void> deleteLevelAttitude(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        this.levelAttitudeService.deleteAttitude(userId, levelId);
        return ResponseEntity.noContent().build();
    }
}
