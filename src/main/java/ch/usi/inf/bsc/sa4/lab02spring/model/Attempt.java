package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.ZonedDateTime;

/// Represents a user's attempt at playing a level. An attempt stores who
/// played, when it happened, which level was played, whether the level was
/// completed, and how long the attempt took.
@SuppressWarnings("NullAway.Init")
@Document(collection = "attempts")
@CompoundIndex(
        name = "attempt_level_exact_fingerprint_metadata",
        def = "{'level': 1, 'fingerprint.exactHash': 1, 'fingerprint.inputFrameCount': 1, 'fingerprint.inputChangeCount': 1}")
@CompoundIndex(
        name = "attempt_level_fuzzy_fingerprint_metadata",
        def = "{'level': 1, 'fingerprint.changeBucketHashes': 1, 'fingerprint.inputFrameCount': 1, 'fingerprint.inputChangeCount': 1}")
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

    /// Result of replay based anti cheat verification for this attempt.
    /* package */ AttemptVerificationStatus antiCheatStatus;

    InputLogFingerprint fingerprint;

    /// Creates a new attempt. An id is auto-generated.
    ///
    /// @param user      the user who made the attempt
    /// @param timestamp the creation timestamp
    /// @param level     the level that was played
    /// @param completed whether the level was completed
    /// @param timeTaken the duration of the attempt
    public Attempt(final User user, final ZonedDateTime timestamp, final Level level, final boolean completed,
            final Duration timeTaken) {
        this.user = user;
        this.timestamp = timestamp;
        this.level = level;
        this.completed = completed;
        this.timeTaken = timeTaken;
        this.antiCheatStatus = AttemptVerificationStatus.NOT_VERIFIED;
        this.fingerprint = InputLogFingerprint.empty();
    }

    /// Creates a persisted attempt with an explicit id.
    ///
    /// @param id        the attempt id
    /// @param user      the user who made the attempt
    /// @param timestamp the creation timestamp
    /// @param level     the level that was played
    /// @param completed whether the level was completed
    /// @param timeTaken the duration of the attempt
    @PersistenceCreator
    public Attempt(final String id, final User user, final ZonedDateTime timestamp, final Level level,
            final boolean completed, final Duration timeTaken,
            final AttemptVerificationStatus antiCheatStatus) {
        this.id = id;
        this.user = user;
        this.timestamp = timestamp;
        this.level = level;
        this.completed = completed;
        this.timeTaken = timeTaken;
        this.antiCheatStatus = antiCheatStatus;
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

    public AttemptVerificationStatus getAntiCheatStatus() {
        return antiCheatStatus;
    }

    public void setAntiCheatStatus(final AttemptVerificationStatus antiCheatStatus) {
        this.antiCheatStatus = antiCheatStatus;
    }

    public void setFingerprint(final InputLogFingerprint fingerprint){
        this.fingerprint = fingerprint;
    }

    public InputLogFingerprint getFingerprint() {
        return fingerprint;
    }
}
