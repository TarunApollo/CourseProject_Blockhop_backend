package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.ZonedDateTime;

/// Represents a user's attempt at playing a level.
/// An attempt stores who played, when it happened, which level was played,
/// whether the level was completed, and how long the attempt took.
@SuppressWarnings("NullAway.Init")
@Document(collection = "attempts")
public class Attempt {

    /// Database identifier of the attempt.
    @Id
    /* package */ String id;

    /// Reference to the user who made this attempt.
    @DBRef
    /* package */ User user;

    /// Timestamp when the attempt was created.
    /* package */ ZonedDateTime timestamp;

    /// Reference to the level associated with this attempt.
    @DBRef
    /* package */ Level level;

    /// Whether the attempt completed the level successfully.
    /* package */ boolean completed;

    /// Time spent on the attempt.
    /* package */ Duration timeTaken;

    /// Creates a new attempt without an explicit id.
    ///
    /// @param user the user who made the attempt
    /// @param timestamp the creation timestamp
    /// @param level the level that was played
    /// @param completed whether the level was completed
    /// @param timeTaken the duration of the attempt
    public Attempt(final User user, final ZonedDateTime timestamp, final Level level, final boolean completed, final Duration timeTaken) {
        this.user = user;
        this.timestamp = timestamp;
        this.level = level;
        this.completed = completed;
        this.timeTaken = timeTaken;
    }

    /// Creates a persisted attempt with an explicit id.
    ///
    /// @param id the attempt id
    /// @param user the user who made the attempt
    /// @param timestamp the creation timestamp
    /// @param level the level that was played
    /// @param completed whether the level was completed
    /// @param timeTaken the duration of the attempt
    @PersistenceCreator
    public Attempt(final String id, final User user, final ZonedDateTime timestamp, final Level level, final boolean completed, final Duration timeTaken) {
        this.id = id;
        this.user = user;
        this.timestamp = timestamp;
        this.level = level;
        this.completed = completed;
        this.timeTaken = timeTaken;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public Duration getTimeTaken() {
        return timeTaken;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(final boolean completed) {
        this.completed = completed;
    }
}
