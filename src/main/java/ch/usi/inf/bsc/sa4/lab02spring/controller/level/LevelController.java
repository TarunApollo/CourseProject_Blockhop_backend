package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.OAuth2UserUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    /// Constructs a new LevelController.
    /// 
    /// @param levelService the service for managing core level operations
    /// @param userService  the service for accessing user data
    @Autowired
    public LevelController(
            final LevelService levelService,
            final UserService userService) {
        this.levelService = levelService;
        this.userService = userService;
    }

    /// Creates a new empty level and returns a level DTO.
    /// 
    /// @spec.requires authentication and createLevelDTO are not null.
    /// @spec.effects saves a new level to the repository with the authenticated user
    ///               as creator.
    /// @param user the authenticated OAuth2 user
    /// @param createLevelDTO the DTO containing the necessary information to create
    ///                       a new level
    /// @return a 200 OK response containing the created level as a LevelDTO
    /// @throws UserNotFoundException if the authenticated user does not exist
    @PostMapping()
    public ResponseEntity<LevelDTO> createLevel(
            @AuthenticationPrincipal final OAuth2User user,
            @RequestBody final CreateLevelDTO createLevelDTO) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(user, "sub");
        return ResponseEntity.ok(new LevelDTO(this.levelService.createLevel(createLevelDTO, userId)));
    }

    /// Clones the given level if it exists and the authenticated user is its
    /// creator.
    ///
    /// @spec.requires authentication and cloneLevelDTO are not null.
    /// @spec.effects saves a clone of the level to the repository with the user as
    ///               the new creator.
    /// @param oauth2User the authenticated OAuth2 user
    /// @param cloneLevelDTO  the DTO containing the id of the level to clone
    /// @return a 200 OK response containing the cloned level as a LevelDTO, or a 403
    ///         Forbidden response if the level does not exist or does not belong to
    ///         the user
    /// @throws UserNotFoundException if the authenticated user does not exist
    @PostMapping("/clone")
    public ResponseEntity<LevelDTO> cloneLevel(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @RequestBody final CloneLevelDTO cloneLevelDTO) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, "sub");
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
    /// @param oauth2User the authenticated OAuth2 user
    /// @param levelId        the id of the level to update
    /// @param dto            the DTO containing the optional new values
    /// @return a 200 OK response containing the updated level
    /// @throws UserNotFoundException  if the authenticated user does not exist
    /// @throws LevelNotFoundException if the target level does not exist
    /// @throws ForbiddenUserException if the authenticated user does not own
    ///                                the level
    @PutMapping("/{levelId}/properties")
    public ResponseEntity<LevelDTO> updateLevel(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @PathVariable final String levelId,
            @RequestBody final UpdateLevelDTO dto) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, "sub");
        final User creator = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(new LevelDTO(this.levelService.updateLevelProperties(creator, levelId, dto)));
    }

    /// Deletes a level owned by the authenticated user.
    /// 
    /// @param oauth2User the authenticated OAuth2 user
    /// @param levelId        the ID of the level to delete
    /// @return 204 No Content when the level is deleted successfully
    /// @throws LevelNotFoundException if the level does not exist
    /// @throws ForbiddenUserException if the user is not the owner of the level
    @DeleteMapping("/{levelId}")
    public ResponseEntity<Void> deleteLevel(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @PathVariable final String levelId) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, "sub");
        this.levelService.deleteLevel(userId, levelId);
        return ResponseEntity.noContent().build();
    }
}
