package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.OAuth2UserUtils;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.LevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelFavoriteService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/// The controller for users.
///
@RestController
@RequestMapping("/users")
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring-managed singleton; injected services are intentionally shared")
public class UserController {

    ///
    /// Service used to access and manage users.
    ///
    private final UserService userService;

    ///
    /// Service used to retrieve level data for user profiles.
    ///
    private final LevelService levelService;

    ///
    /// Service used to retrieve attempt statistics for user profiles.
    ///
    private final AttemptService attemptService;

    ///
    /// Service used to manage and retrieve favorite levels.
    ///
    private final LevelFavoriteService levelFavoriteService;

    /// Constructs a new UserController with the given dependencies.
    /// @param userService the service for accessing user data
    /// @param levelService the service for managing level operations
    /// @param attemptService the service for querying attempt-related statistics
    /// @param levelFavoriteService the service for managing favorite levels
    @Autowired
    public UserController(
            final UserService userService,
            final LevelService levelService,
            final AttemptService attemptService,
            final LevelFavoriteService levelFavoriteService) {
        this.userService = userService;
        this.levelService = levelService;
        this.attemptService = attemptService;
        this.levelFavoriteService = levelFavoriteService;
    }

    /// @return a list of all existing users as UserDTOs
    @GetMapping
    public List<UserDTO> getUsers() {
        final List<User> users = this.userService.getAllUsers();
        return users.stream().map(UserDTO::new).toList();
    }

    /// Returns the user with the given id.
    /// @spec.requires id is not null.
    /// @param id the user's unique identifier (path variable)
    /// @return a 200 OK response containing the user as a UserDTO if found,
    ///         or a 404 Not Found response otherwise
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable final String id) {
        return ResponseEntity.of(this.userService.getById(id).map(UserDTO::new));
    }

    /// Searches for users whose name matches the given query string.
    /// @spec.requires partialName is not null.
    /// @param partialName the string to search for in user names
    ///        (request parameter "query")
    /// @return a list of matching users as UserDTOs
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam("query") final String partialName) {
        return userService.searchUsers(partialName).stream().map(UserDTO::new).toList();
    }

    /// Returns the profile information for the authenticated user.
    /// @spec.requires oauth2User is not null.
    /// @param oauth2User the authenticated OAuth2 user
    /// @return a 200 OK response containing the user's profile information
    ///         (name, played levels count, completed levels count, and list of created levels),
    ///         or a 404 Not Found response if the user does not exist
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(
            @AuthenticationPrincipal final OAuth2User oauth2User) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, "sub");
        return ResponseEntity.of(this.userService.getById(userId)
                .map(profileUser -> new UserProfileDTO(
                        profileUser,
                        this.attemptService.getPlayedLevelsCount(profileUser),
                        this.attemptService.getCompletedLevelsCount(profileUser),
                        this.levelService.getCreatedLevelsByUser(profileUser))));
    }

    /// Authenticates a user using SwitchEduId Login.
    ///
    /// @param oauth2User the authenticated OAuth2 user
    /// @return a 200 OK with the newly created user dto,
    ///         or the existing user dto information if the user
    ///         already exists
    @GetMapping(path = "/me")
    public ResponseEntity<UserDTO> index(
            @AuthenticationPrincipal final OAuth2User oauth2User) {
        final String eduId = OAuth2UserUtils.getRequiredAttribute(oauth2User, "sub");
        final String fullName = OAuth2UserUtils.getRequiredAttribute(oauth2User, "name");
        final Optional<User> optUser = this.userService.getById(eduId);
        return optUser.map(existingUser -> ResponseEntity.ok(new UserDTO(existingUser)))
                .orElseGet(() -> ResponseEntity.ok(new UserDTO(this.userService.createUser(new CreateUserDTO(eduId, fullName)))));
    }

    /// Returns the levels favorited by the authenticated user.
    ///
    /// @spec.requires oAuth2User is not null.
    /// @param oAuth2User the authenticated OAuth2 user
    /// @return a 200 OK response containing the user's favorited levels as DTOs
    /// @throws UserNotFoundException if no user with the authenticated id exists
    @GetMapping("/me/favorites")
    public ResponseEntity<List<LevelDTO>> getFavorites(@AuthenticationPrincipal final OAuth2User oAuth2User) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(oAuth2User, "sub");
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        final List<LevelDTO> favorites = this.levelFavoriteService.getFavoritesByUser(user).stream()
                .map(favorite -> new LevelDTO(favorite.getLevel()))
                .toList();
        return ResponseEntity.ok(favorites);
    }
}
