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

  /// Creates a new user and persists it in the DB.
  /// 
  /// @param createUserDTO the data to create a new user.
  /// @return the newly created user.
  /// @spec.requires <code>createUserDTO != null</code>
  /// 
  public User createUser(CreateUserDTO createUserDTO) {
    User newUser = new User(createUserDTO.name(), createUserDTO.password());
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

  /// Changes the user's password.
  /// 
  /// @param userId      a user id.
  /// @param oldPassword the old password.
  /// @param newPassword the new password.
  /// @return an optional with the updated user if the password has been changed
  ///         successfully. An empty option if the user does not exist.
  /// 
  public Optional<User> changePassword(String userId, String oldPassword, String newPassword) {
    Optional<User> optUser = this.getById(userId);
    return optUser.map(user -> {
      User updatedUser = user.changePassword(oldPassword, newPassword);
      return this.userRepository.save(updatedUser);
    });
  }

  /// Searches for users whose name contains a given string.
  /// 
  /// @param partialName a partial name to search
  /// @return the list of users whose name contains `partialName`.
  /// @spec.requires `partialName != null`
  /// 
  public List<User> searchUsers(String partialName) {
    return userRepository.findByNameContaining(partialName);
  }

}
