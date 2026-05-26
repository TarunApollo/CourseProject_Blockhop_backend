package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/// Represents a user's attempt at playing a level. An attempt stores who
/// played, when it happened, which level was played, whether the level was
/// completed, and how long the attempt took.
@SuppressWarnings({"NullAway.Init", "PMD.DataClass"})
@SuppressFBWarnings(
        value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" },
        justification = "Mongo-managed entity; references stored as-is for persistence")
@Document(collection = "attempts")
@CompoundIndex(
        name = "attempt_level_exact_fingerprint_metadata",
        def = "{'level': 1, 'fingerprint.exactHash': 1, 'fingerprint.inputFrameCount': 1, 'fingerprint.inputChangeCount': 1}")
@CompoundIndex(
        name = "attempt_level_fuzzy_fingerprint_metadata",
        def = "{'level': 1, 'fingerprint.changeBucketHashes': 1, 'fingerprint.inputFrameCount': 1, 'fingerprint.inputChangeCount': 1}")
@CompoundIndex(
        name = "attempt_level_jitter_fingerprint_metadata",
        def = "{'level': 1, 'fingerprint.jitterInputHash': 1, 'fingerprint.jitterInputChangeCount': 1}")
@CompoundIndex(
        name = "attempt_level_completed_timetaken_ghost",
        def = "{'level': 1, 'completed': 1, 'timeTaken': 1}")
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

    /// Fingerprint of the input log used for anti-cheat duplicate detection.
    /* package */ InputLogFingerprint fingerprint;

    /// Input frames recorded during this attempt, used for ghost replay.
    @JsonIgnore
    private List<InputFrameDTO> inputLog;

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
    /// @param antiCheatStatus stored anti-cheat verification status
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

    /// Returns the database id of this attempt.
    ///
    /// @return attempt id
    public String getId() {
        return id;
    }

    /// Returns the user who made this attempt.
    ///
    /// @return attempt owner
    public User getUser() {
        return user;
    }

    /// Returns when this attempt was created.
    ///
    /// @return attempt timestamp
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    /// Returns the level played by this attempt.
    ///
    /// @return attempted level
    public Level getLevel() {
        return level;
    }

    /// Returns the duration recorded for this attempt.
    ///
    /// @return attempt duration
    public Duration getTimeTaken() {
        return timeTaken;
    }

    /// Reports whether this attempt completed the level.
    ///
    /// @return true when the level was completed
    public boolean isCompleted() {
        return completed;
    }

    /// Updates whether this attempt completed the level.
    ///
    /// @param completed true when the level was completed
    public void setCompleted(final boolean completed) {
        this.completed = completed;
    }

    /// Returns the anti-cheat verification status for this attempt.
    ///
    /// @return anti-cheat status
    public AttemptVerificationStatus getAntiCheatStatus() {
        return antiCheatStatus;
    }

    /// Updates the anti-cheat verification status for this attempt.
    ///
    /// @param antiCheatStatus status to store
    public void setAntiCheatStatus(final AttemptVerificationStatus antiCheatStatus) {
        this.antiCheatStatus = antiCheatStatus;
    }

    /// Stores the input-log fingerprint used for duplicate detection.
    ///
    /// @param fingerprint fingerprint to store
    public void setFingerprint(final InputLogFingerprint fingerprint){
        this.fingerprint = fingerprint;
    }

    /// Returns the stored input-log fingerprint.
    ///
    /// @return input-log fingerprint
    public InputLogFingerprint getFingerprint() {
        return fingerprint;
    }

    /// Returns the input frames recorded for ghost replay.
    ///
    /// @return input log, or null if not set
    public List<InputFrameDTO> getInputLog() {
        return inputLog;
    }

    /// Stores the input frames for ghost replay.
    ///
    /// @param inputLog frames to store
    public void setInputLog(final List<InputFrameDTO> inputLog) {
        this.inputLog = inputLog;
    }
}
