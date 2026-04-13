package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

public interface AttemptStatisticsRepository {
    long countDistinctPlayedLevelsByUser(User user);
    long countDistinctCompletedLevelsByUser(User user);
}

