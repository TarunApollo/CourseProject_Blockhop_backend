package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;

/// Custom statistics queries for level attitude data.
public interface AttitudeStatisticsRepository {

    /// Counts likes recorded for the given level.
    /// @param level the level whose likes should be counted
    /// @return number of like attitudes for the given level
    long countLikesByLevel(Level level);

    /// Counts dislikes recorded for the given level.
    /// @param level the level whose dislikes should be counted
    /// @return number of dislike attitudes for the given level
    long countDislikesByLevel(Level level);
}