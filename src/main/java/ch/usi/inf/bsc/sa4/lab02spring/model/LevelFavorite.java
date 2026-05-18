package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
/// Represents a user's favorite level. A favorite stores who marked the level
/// as favorite, which level was favorited, and when it happened.
@SuppressWarnings({ "NullAway.Init", "PMD.DataClass" })
@SuppressFBWarnings(value = { "EI_EXPOSE_REP",
        "EI_EXPOSE_REP2" }, justification = "Mongo-managed entity; references are exposed intentionally for persistence and DTO mapping")
@Document(collection = "favorites")
public class LevelFavorite {

    /// Database identifier of the favorite.
    @Id
    /* package */ String id;

    /// Reference to the user who favorited the level.
    @DBRef
    /* package */ User user;

    /// Reference to the favorited level.
    @DBRef
    /* package */ Level level;

    /// Timestamp when the favorite was created.
    /* package */ Instant timestamp;

    /// Creates a new favorite without an explicit id.
    ///
    /// @param user      the user who favorited the level
    /// @param level     the level that was favorited
    /// @param timestamp the creation timestamp
    public LevelFavorite(final User user, final Level level, final Instant timestamp) {
        this.user = user;
        this.level = level;
        this.timestamp = timestamp;
    }

    /// Creates a persisted favorite with an explicit id.
    ///
    /// @param id        the favorite id
    /// @param user      the user who favorited the level
    /// @param level     the level that was favorited
    /// @param timestamp the creation timestamp
    @PersistenceCreator
    public LevelFavorite(final String id, final User user, final Level level, final Instant timestamp) {
        this.id = id;
        this.user = user;
        this.level = level;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Level getLevel() {
        return level;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
