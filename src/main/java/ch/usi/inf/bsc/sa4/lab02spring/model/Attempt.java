package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.ZonedDateTime;

@Document(collection = "attempts")
public class Attempt {
    @Id
    String id;
    @DBRef
    User user;
    ZonedDateTime timestamp;
    @DBRef
    Level level;
    boolean completed;
    Duration timeTaken;


    public Attempt(User user, ZonedDateTime timestamp, Level level, boolean completed, Duration timeTaken) {
        this.user = user;
        this.timestamp = timestamp;
        this.level = level;
        this.completed = completed;
        this.timeTaken = timeTaken;
    }

    @PersistenceCreator
    public Attempt(String id, User user, ZonedDateTime timestamp, Level level, boolean completed, Duration timeTaken) {
        this.id = id;
        this.user = user;
        this.timestamp = timestamp;
        this.level = level;
        this.completed = completed;
        this.timeTaken = timeTaken;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
