package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.LevelFavorite;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelFavoriteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.List;

/// Service handling creation, removal, and querying of favorite levels.
@Service
public class LevelFavoriteService {
    /// Repository handling favorite persistence.
    private final LevelFavoriteRepository levelFavoriteRepository;

    /// Constructs a new LevelFavoriteService with the given dependency.
    ///
    /// @param levelFavoriteRepository the repository for accessing favorite data
    @Autowired
    public LevelFavoriteService(final LevelFavoriteRepository levelFavoriteRepository) {
        this.levelFavoriteRepository = levelFavoriteRepository;
    }

    /// Marks the given level as favorite for the given user.
    ///
    /// @spec.requires user and level are not null.
    /// @spec.effects creates a new LevelFavorite for the user-level pair and saves
    ///               it to the repository; if a favorite already exists for this
    ///               pair, no operation is performed (idempotent).
    /// @param user  the user adding the favorite
    /// @param level the level being favorited
    public void addFavorite(final User user, final Level level) {
        level.ensurePublished();
        if (this.levelFavoriteRepository.existsByUserAndLevel(user, level)) {
            return;
        }
        final LevelFavorite favorite = new LevelFavorite(user, level, Instant.now());
        this.levelFavoriteRepository.save(favorite);
    }

    /// Removes the favorite of the level with the given id for the given user.
    /// Idempotent: deleting a non-existent favorite is a no-op. Works even if the
    /// underlying level has been deleted.
    ///
    /// @spec.requires user and levelId are not null.
    /// @spec.effects deletes the LevelFavorite for the given user and levelId from
    ///               the repository; if no such favorite exists, no operation is
    ///               performed.
    /// @param user    the user removing the favorite
    /// @param levelId the id of the level being unfavorited
    public void removeFavorite(final User user, final String levelId) {
        this.levelFavoriteRepository.deleteByUserIdAndLevelId(user.getId(), levelId);
    }

    /// Returns all favorites belonging to the given user.
    ///
    /// @spec.requires user is not null.
    /// @param user the user whose favorites to retrieve
    /// @return the user's favorites
    public List<LevelFavorite> getFavoritesByUser(final User user) {
        return this.levelFavoriteRepository.findByUser(user);
    }
}
