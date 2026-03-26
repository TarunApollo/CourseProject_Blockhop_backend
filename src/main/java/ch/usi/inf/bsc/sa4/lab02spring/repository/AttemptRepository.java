package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface AttemptRepository extends MongoRepository<Attempt, String>,AttemptStatisticsRepository {
    long countByLevel(Level level);
    long countByLevelAndCompletedTrue(Level level);
    long countByLevelAndTimestampAfter(Level level, ZonedDateTime after);
}
