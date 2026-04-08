package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptRepository extends MongoRepository<Attempt, String> {
    List<Attempt> findByUser(User user);
    List<Attempt> findByUserAndCompletedTrue(User user);
    List<Attempt> findByUserAndLevel(User user, Level level);
    Optional<Attempt> findByIdAndUser(String id, User user);
}
