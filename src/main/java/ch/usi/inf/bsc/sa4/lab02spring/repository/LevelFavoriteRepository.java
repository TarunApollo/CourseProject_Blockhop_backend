package ch.usi.inf.bsc.sa4.lab02spring.repository;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelFavorite;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;
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

    /// Removes the favorite (if any) for the given user-level pair.
    /// @param user  the user
    /// @param level the level
    void deleteByUserAndLevel(User user, Level level);
}