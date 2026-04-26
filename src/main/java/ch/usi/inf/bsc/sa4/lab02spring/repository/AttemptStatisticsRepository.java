package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

/// Custom statistics queries for attempt data.
public interface AttemptStatisticsRepository {

    /// Counts the distinct levels the given user has played.
    /// @param user the user whose played levels should be counted
    /// @return the number of distinct played levels
    long countDistinctPlayedLevelsByUser(User user);

    /// Counts the distinct levels the given user has completed.
    /// @param user the user whose completed levels should be counted
    /// @return the number of distinct completed levels
    long countDistinctCompletedLevelsByUser(User user);
}

