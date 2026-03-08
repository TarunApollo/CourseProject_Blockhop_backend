package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/// The controller for users.
///
@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
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

  /// Authenticates a user using SwitchEduId Login.
  ///
  /// @param authentication token containing information about logged user
  ///
  /// @return a 200 OK with the newly created user dto, otherwise return the existing user dto information
  @GetMapping(path = "/me")
  @SuppressWarnings("NullAway")
  public ResponseEntity<UserDTO> index(Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.badRequest().build();
    }
    Object principal = authentication.getPrincipal();
    Map<String, Object> attributes = Map.of();
    if (authentication.getPrincipal() instanceof Jwt jwt) {
      attributes = jwt.getClaims();
    } else if (principal instanceof OAuth2User oAuth2User) {
      attributes = oAuth2User.getAttributes();
    }
    String fullName = (String) attributes.get("name");
    String eduId = (String) attributes.get("sub");
    assert eduId != null;

    Optional<User> optUser = this.userService.getById(eduId);
    return optUser.map(
            user -> ResponseEntity.ok(new UserDTO(user)))
            .orElseGet(() -> ResponseEntity.ok(new UserDTO(this.userService.createUser(new CreateUserDTO(eduId, fullName)))));
  }
}
