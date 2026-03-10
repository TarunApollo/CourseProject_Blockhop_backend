package ch.usi.inf.bsc.sa4.lab02spring.controller;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/// The controller for users.
///
@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final LevelService levelService;

  @Autowired
  public UserController(UserService userService, LevelService levelService) {
    this.userService = userService;
    this.levelService = levelService;
  }

  /// Returns the list of existing users.
  ///
  /// @return a list of existing users.
  ///
  @GetMapping
  public List<UserDTO> getUsers() {
    var users = this.userService.getAllUsers();
    return users.stream().map(UserDTO::new).toList();
  }

  /// Adds a new user.
  ///
  /// @param createUserDTO the information needed to create a user.
  /// @return a 200 OK with the newly created user DTO.
  /// @spec.modifies the list of users in the system.
  /// @spec.requires nothing
  /// @deprecated only use this for testing with postman. Switcheduid login should be used instead
  @PostMapping
  public UserDTO createUser(@RequestBody CreateUserDTO createUserDTO) {
    return new UserDTO(userService.createUser(createUserDTO));
  }

  /// Returns the user dto with the given id.
  /// 
  /// @param id a path variable containing the user's id.
  /// @return a 200 OK if the user exists, a 404 NOT FOUND otherwise.
  /// 
  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
    return ResponseEntity.of(this.userService.getById(id).map(UserDTO::new));
  }

  /// Searches for a user's name in the system
  /// 
  /// @param partialName a request param with the string to search in the user's
  ///                    name.
  /// @return a 200 OK with the list of user dtos matching the query.
  /// 
  @GetMapping("/search")
  public List<UserDTO> searchUsers(@RequestParam("query") String partialName) {
    return userService.searchUsers(partialName).stream().map(UserDTO::new).toList();
  }

  /// Returns the profile information for the authenticated user.
  ///
  /// @param authentication token containing information about the logged-in user
  /// @return a 200 OK with the user's profile information (name, played levels count,
  ///         completed levels count, and list of created levels), or 404 if user not found
  ///
  @GetMapping("/profile")
  public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
    String userId = getUserIdFromAuth(authentication);

    return ResponseEntity.of(this.userService.getById(userId)
            .map(user -> new UserProfileDTO(
                    user,
                    this.userService.getPlayedLevelsCount(user),
                    this.userService.getCompletedLevelsCount(user),
                    this.levelService.getCreatedLevelsByUser(userId))));
  }

  /// Authenticates a user using SwitchEduId Login.
  ///
  /// @param authentication token containing information about logged user
  ///
  /// @return a 200 OK with the newly created user dto, otherwise return the existing user dto information
  @GetMapping(path = "/me")
  public ResponseEntity<UserDTO> index(Authentication authentication) {
    // Reuse the shared auth helper so /me and /profile resolve the current user id the same way.
    String eduId = getUserIdFromAuth(authentication);
    String fullName = getUserNameFromAuth(authentication);

    Optional<User> optUser = this.userService.getById(eduId);
    return optUser.map(
            user -> ResponseEntity.ok(new UserDTO(user)))
            .orElseGet(() -> ResponseEntity.ok(new UserDTO(this.userService.createUser(new CreateUserDTO(eduId, fullName)))));
  }

  private String getUserNameFromAuth(Authentication authentication) {
    Object principal = authentication.getPrincipal();

    if (principal instanceof Jwt jwt) {
      String name = jwt.getClaimAsString("name");
      if (name != null) {
        return name;
      }
    } else if (principal instanceof OAuth2User oAuth2User) {
      String name = oAuth2User.getAttribute("name");
      if (name != null) {
        return name;
      }
    }

    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user name not available");
  }
}
