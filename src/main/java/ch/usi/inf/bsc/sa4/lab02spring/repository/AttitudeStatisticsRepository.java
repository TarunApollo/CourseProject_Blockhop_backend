package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelAttitudeType;

/// Custom statistics queries for attitude data.
public interface AttitudeStatisticsRepository {

    /// Counts the likes or dislikes the given level has.
    /// @param level the level to count statistics for
    /// @param levelAttitude the attitude to filter by
    /// @return the number of likes or dislikes for a specific level
    long countByLevelAttitude(Level level, LevelAttitudeType levelAttitude);

}