package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/// Represents a user's attitude towards a level. An attitude stores who
/// liked/disliked, which level was rated and whether a level is liked
/// or disliked.
@SuppressWarnings("NullAway.Init")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Mongo-managed entity; references stored as-is for persistence")
@Document(collection = "ratings")
public class LevelAttitude {

    /// Database identifier of the attitude.
    @Id
    /* package */ String id;

    /// Reference to the user who made this attitude.
    @DBRef
    /* package */ User user;

    /// Reference to the level associated with this attitude.
    @DBRef
    /* package */ Level level;

    /// The rating done by the user
    /* package */ LevelAttitudeType attitude;

    /// Creates a new level attitude entry without an explicit id.
    ///
    /// @param user     the user who made the attitude
    /// @param level    the level that was played
    /// @param attitude the attitude towards a level
    public LevelAttitude(final User user, final Level level, final LevelAttitudeType attitude) {
        this.user = user;
        this.level = level;
        this.attitude = attitude;
    }

    /// Creates a new level attitude entry with an explicit id.
    ///
    /// @param id       the attitude id
    /// @param user     the user who made the attitude
    /// @param level    the level that was played
    /// @param attitude the attitude towards a level
    @PersistenceCreator
    public LevelAttitude(final String id, final User user, final Level level, final LevelAttitudeType attitude) {
        this.id = id;
        this.user = user;
        this.level = level;
        this.attitude = attitude;
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

    public LevelAttitudeType getAttitude() {
        return attitude;
    }

    public void setAttitude(final LevelAttitudeType attitude) {
        this.attitude = attitude;
    }
}
