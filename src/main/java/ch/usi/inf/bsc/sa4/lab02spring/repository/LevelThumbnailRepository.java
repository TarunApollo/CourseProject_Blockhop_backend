package ch.usi.inf.bsc.sa4.lab02spring.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.usi.inf.bsc.sa4.lab02spring.model.LevelThumbnail;

public interface LevelThumbnailRepository extends MongoRepository<LevelThumbnail, String> {
    Optional<LevelThumbnail> findByLevelId(String levelId);
    void deleteByLevelId(String levelId);
}
