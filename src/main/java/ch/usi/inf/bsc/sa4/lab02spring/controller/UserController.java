package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.SwitchEduIdUser;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  ///
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

  @GetMapping(path = "/me")
  @SuppressWarnings("NullAway")
  public ResponseEntity<SwitchEduIdUser> index(OAuth2AuthenticationToken authentication) {
    String fullName = authentication.getPrincipal().getAttribute("name");
    String token = authentication.getPrincipal().getAttribute("sub");
    return ResponseEntity.ok(new SwitchEduIdUser(fullName));
  }
}
