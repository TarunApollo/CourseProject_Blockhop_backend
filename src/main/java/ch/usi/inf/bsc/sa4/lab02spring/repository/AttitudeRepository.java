package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitude;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/// Repository for managing level attitudes (likes/dislikes) in the database.
@Repository
public interface AttitudeRepository extends MongoRepository<LevelAttitude, String> {

    /// Find a single attitude for a given level and user. Since each user can have
    /// at most one attitude per level, return an Optional.
    /// 
    /// @param level the level
    /// @param user  the user
    /// @return optional attitude
    Optional<LevelAttitude> findByLevelAndUser(Level level, User user);

    /// Deletes the attitude of a user for a specific level, if it exists.
    /// 
    /// @param level the level
    /// @param user  the user
    void deleteByLevelAndUser(Level level, User user);
}
