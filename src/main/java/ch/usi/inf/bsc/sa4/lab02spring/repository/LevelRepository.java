package ch.usi.inf.bsc.sa4.lab02spring.repository;

import java.util.List;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;

/// Repository for storing and querying `Level` documents.
@Repository
public interface LevelRepository extends MongoRepository<Level, String> {

    /// Returns all levels created by the given user.
    /// @param user the creator whose levels should be returned
    /// @return all levels created by the user
    List<Level> findByCreator(User user);

    /// Returns all published levels.
    /// @return all levels marked as published
    List<Level> findByPublishedTrue();
}
