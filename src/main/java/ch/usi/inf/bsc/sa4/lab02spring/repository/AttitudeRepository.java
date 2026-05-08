package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitude;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// the interface can later extend AttitudeStatistics
@Repository
public interface AttitudeRepository extends MongoRepository<LevelAttitude, String> {
    
    /// Return all likes and dislikes of a specific level
    /// So far, counting likes/dislikes outside of DB
    /// @param level the level to search by
    List<LevelAttitude> findByLevel(Level level);

    /// Returns all rated levels by a specific user
    /// @param user the user to search by
    List<LevelAttitude> findByUser(User user);


}
