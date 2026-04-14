package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


import java.time.ZonedDateTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptRepository extends MongoRepository<Attempt, String>,AttemptStatisticsRepository {
    List<Attempt> findByUser(User user);
    List<Attempt> findByUserAndCompletedTrue(User user);

    /// Returns all attempts of the given user for the given level.
    List<Attempt> findByUserAndLevel(User user, Level level);

    /// Returns the attempt with the given id if it belongs to the given user.
    Optional<Attempt> findByIdAndUser(String id, User user);
    long countByLevel(Level level);
    long countByLevelAndCompletedTrue(Level level);
    long countByLevelAndTimestampAfter(Level level, ZonedDateTime after);
}
