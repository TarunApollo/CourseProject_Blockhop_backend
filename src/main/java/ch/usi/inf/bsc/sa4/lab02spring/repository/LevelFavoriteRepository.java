package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelFavorite;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/// Repository for storing and querying `LevelFavorite` documents.
@Repository
public interface LevelFavoriteRepository extends MongoRepository<LevelFavorite, String> {

    /// Returns all favorites belonging to the given user.
    /// @param user the user whose favorites should be returned
    /// @return all favorites belonging to the user
    List<LevelFavorite> findByUser(User user);

    /// Checks whether the given user has already favorited the given level.
    /// @param user  the user
    /// @param level the level
    /// @return true if a favorite already exists for this user-level pair
    boolean existsByUserAndLevel(User user, Level level);

    /// Removes the favorite (if any) for the given user and level id.
    /// Querying by levelId (not the Level entity) lets users clean
    /// up favorites whose underlying level was deleted from the DB.
    /// @param user    the user
    /// @param levelId the id of the level
    @Query(value = "{ 'user': ?0, 'level.$id': ?1 }", delete = true)
    void deleteByUserAndLevelId(User user, String levelId);

    /// Removes all favorites pointing to the level with the given id.
    /// Used when a level becomes unavailable to keep favorite lists
    /// restricted to published levels.
    /// @param levelId the id of the level
    @Query(value = "{ 'level.$id': ?0 }", delete = true)
    void deleteByLevelId(String levelId);
}
