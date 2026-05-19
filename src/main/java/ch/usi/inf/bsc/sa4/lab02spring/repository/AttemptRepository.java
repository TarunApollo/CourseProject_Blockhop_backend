package ch.usi.inf.bsc.sa4.lab02spring.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

/// Repository for storing and querying `Attempt` documents.
///
/// In addition to standard MongoDB repository operations, this repository also
/// exposes custom attempt statistics queries.
@Repository
public interface AttemptRepository extends MongoRepository<Attempt, String>, AttemptStatisticsRepository {

    /// Returns all attempts created by the given user.
    /// @param user the user whose attempts should be returned
    /// @return all attempts belonging to the user
    List<Attempt> findByUser(User user);

    /// Returns all completed attempts created by the given user.
    /// @param user the user whose completed attempts should be returned
    /// @return all completed attempts belonging to the user
    List<Attempt> findByUserAndCompletedTrue(User user);

    /// Returns all attempts of the given user for the given level.
    List<Attempt> findByUserAndLevel(User user, Level level);

    /// Returns the attempt with the given id if it belongs to the given user.
    Optional<Attempt> findByIdAndUser(String id, User user);

    /// Deletes all attempts recorded for the given level.
    /// @param level the level whose attempts should be removed
    void deleteByLevel(Level level);

    /// Counts all attempts recorded for the given level.
    /// @param level the level to count attempts for
    /// @return the number of attempts for the level
    long countByLevel(Level level);

    /// Counts completed attempts recorded for the given level.
    /// @param level the level to count completed attempts for
    /// @return the number of completed attempts for the level
    long countByLevelAndCompletedTrue(Level level);

    /// Counts attempts recorded for the given level after the provided timestamp.
    /// @param level the level to count attempts for
    /// @param after the lower time bound
    /// @return the number of attempts after the given timestamp
    long countByLevelAndTimestampAfter(Level level, ZonedDateTime after);
}
