package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/// Repository for storing and querying users.
@Repository
public interface UserRepository extends MongoRepository<User, String> {

  /// Finds all users whose name contains the given text.
  /// @param name the substring to search for
  /// @return matching users
  List<User> findByNameContaining(String name);
}
