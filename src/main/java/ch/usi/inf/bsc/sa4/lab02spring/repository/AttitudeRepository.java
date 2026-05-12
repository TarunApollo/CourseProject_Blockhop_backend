package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitude;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/// Repository for managing level attitudes (likes/dislikes) in the database.
@Repository
public interface AttitudeRepository extends MongoRepository<LevelAttitude, String>, AttitudeStatisticsRepository {
    
    /// Returns all user attitudes of a specific level.
    /// So far, counting likes/dislikes outside of DB.
    /// @param level the level to search by
    /// @return all attitudes of the level
    List<LevelAttitude> findByLevel(Level level);

    /// Returns all level attitudes of a specific user.
    /// @param user the user to search by
    /// @return all attitudes of the user
    List<LevelAttitude> findByUser(User user);

    /// Find a single attitude for a given level and user.
    /// Since each user can have at most one attitude per level, 
    /// return an Optional.
    /// @param level the level
    /// @param user the user
    /// @return optional attitude
    Optional<LevelAttitude> findByLevelAndUser(Level level, User user);

    /// Deletes the attitude of a user for a specific level, 
    /// if it exists.
    /// @param level the level
    /// @param user the user
    void deleteByLevelAndUser(Level level, User user);

}
