package ch.usi.inf.bsc.sa4.lab02spring.repository;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttemptRepository extends MongoRepository<Attempt, String> {

}
