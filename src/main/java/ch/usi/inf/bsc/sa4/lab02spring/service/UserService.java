package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/// A service class to support user management.
/// 
@Service
public class UserService {
  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /// Returns all existing users.
  /// 
  /// @return a list of all existing users.
  /// 
  public List<User> getAllUsers() {
    return this.userRepository.findAll();
  }

  /// Creates a new user (using SwitchEduId login information) and persists it in the DB.
  /// 
  /// @param dto the data to create a new user.
  /// @return the newly created user.
  /// @spec.requires <code>createUserDTO != null</code>
  /// 
  public User createUser(CreateUserDTO dto) {
    User newUser = new User(dto.id(), dto.fullName());
    return this.userRepository.save(newUser);
  }

  /// Looks for a user by its id.
  /// 
  /// @param userId a userId.
  /// @return an optional which contains the user with the given id if it exists,
  ///         otherwise an empty optional.
  ///
  public Optional<User> getById(String userId) {
    return userRepository.findById(userId);
  }

  /// @returns the number of levels played by the user
  public int getPlayedLevelsCount(User user) {
    return user.getLevelsPlayed().size();
  }

  /// @returns the number of levels completed by the user
  public int getCompletedLevelsCount(User user) {
    return user.getLevelsCompleted().size();
  }

  /// Searches for users whose name contains a given string.
  /// @spec.requires `partialName != null`
  /// @param partialName a partial name to search
  /// @return the list of users whose name contains `partialName`.
  public List<User> searchUsers(String partialName) {
    return userRepository.findByNameContaining(partialName);
  }

}
