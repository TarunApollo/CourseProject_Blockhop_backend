package ch.usi.inf.bsc.sa4.lab02spring.repository;

import java.util.List;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;

@Repository
public interface LevelRepository extends MongoRepository<Level, String> {
    // @return list of levels
    List<Level> findByCreatorId(String creatorId);

    // TODO: add other methods to query the database
}
