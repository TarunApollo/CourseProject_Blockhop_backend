package ch.usi.inf.bsc.sa4.lab02spring.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.usi.inf.bsc.sa4.lab02spring.model.LevelThumbnail;

/// Repository for storing and querying `LevelThumbnail` documents.
public interface LevelThumbnailRepository extends MongoRepository<LevelThumbnail, String> {

    /// Returns the thumbnail associated with the given level id.
    /// @param levelId the level id to search for
    /// @return the thumbnail if it exists
    Optional<LevelThumbnail> findByLevelId(String levelId);

    /// Deletes the thumbnail associated with the given level id.
    /// @param levelId the level id whose thumbnail should be removed
    void deleteByLevelId(String levelId);
}
