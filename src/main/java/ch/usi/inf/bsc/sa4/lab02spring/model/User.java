package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
@SuppressWarnings("NullAway.Init")
public class User {
    @Id
    private final String id; // switchEduId
    private final String name;
    private final Set<Level> completedLevels = new HashSet<>();
    private final Set<Level> playedLevels = new HashSet<>();

    @PersistenceCreator
    public User(String id, String name, Set<Level> completedLevels, Set<Level> playedLevels) {
        this.id = id;
        this.name = name;
        this.completedLevels.addAll(completedLevels);
        this.playedLevels.addAll(playedLevels);
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Level> getCompletedLevels() { return completedLevels; }

    public Set<Level> getPlayedLevels() { return playedLevels; }
}
