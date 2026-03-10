package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private final String id; // switchEduId
    private final String name;
    @DBRef
    private final Set<Level> levelsPlayed = new HashSet<>();
    @DBRef
    private final Set<Level> levelsCompleted = new HashSet<>();

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @PersistenceCreator
    public User(String id, String name, Set<Level> levelsPlayed, Set<Level> levelsCompleted) {
        this.id = id;
        this.name = name;
        this.levelsPlayed.addAll(levelsPlayed);
        this.levelsCompleted.addAll(levelsCompleted);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Level> getLevelsPlayed() {
        return levelsPlayed;
    }

    public Set<Level> getLevelsCompleted() {
        return levelsCompleted;
    }

    public void addPlayedLevel(Level level) {
        this.levelsPlayed.add(level);
    }

    public void addCompletedLevel(Level level) {
        this.levelsCompleted.add(level);
    }
}