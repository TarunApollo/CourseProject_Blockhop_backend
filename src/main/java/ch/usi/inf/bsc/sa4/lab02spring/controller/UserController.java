package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/// The controller for users.
///
@RestController
@RequestMapping("/users")
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

    /// Constructs a new UserController with the given dependencies.
    /// @param userService the service for accessing user data
    /// @param levelService the service for managing level operations
    /// @param attemptService the service for querying attempt-related statistics
    @Autowired
    public UserController(final UserService userService, final LevelService levelService, final AttemptService attemptService) {
        this.userService = userService;
        this.levelService = levelService;
        this.attemptService = attemptService;
    }

    /// @return a list of all existing users as UserDTOs
    @GetMapping
    public List<UserDTO> getUsers() {
        final var users = this.userService.getAllUsers();
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
    /// @param partialName the string to search for in user names (request parameter "query")
    /// @return a list of matching users as UserDTOs
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam("query") final String partialName) {
        return userService.searchUsers(partialName).stream().map(UserDTO::new).toList();
    }

    /// Returns the profile information for the authenticated user.
    /// @spec.requires authentication is not null.
    /// @param authentication token containing information about the logged-in user
    /// @return a 200 OK response containing the user's profile information
    ///         (name, played levels count, completed levels count, and list of created levels),
    ///         or a 404 Not Found response if the user does not exist
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(final Authentication authentication) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);

        return ResponseEntity.of(this.userService.getById(userId)
                .map(user -> new UserProfileDTO(
                        user,
                        this.attemptService.getPlayedLevelsCount(user),
                        this.attemptService.getCompletedLevelsCount(user),
                        this.levelService.getCreatedLevelsByUser(user))));
    }

    /// Authenticates a user using SwitchEduId Login.
    ///
    /// @param authentication token containing information about logged user
    /// @return a 200 OK with the newly created user dto, otherwise return the existing user dto information
    @GetMapping(path = "/me")
    public ResponseEntity<UserDTO> index(final Authentication authentication) {
        // Reuse the shared auth helper so /me and /profile resolve the current user id the same way.
        final String eduId = AuthUtils.getUserIdFromAuth(authentication);
        final String fullName = AuthUtils.getUserNameFromAuth(authentication);

        final Optional<User> optUser = this.userService.getById(eduId);
        return optUser.map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElseGet(() -> ResponseEntity.ok(new UserDTO(this.userService.createUser(new CreateUserDTO(eduId, fullName)))));
    }
}
