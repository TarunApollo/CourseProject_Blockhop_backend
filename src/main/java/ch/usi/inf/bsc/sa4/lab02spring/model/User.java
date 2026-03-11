package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.Collections;
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

    /// Creates a new User with no levels played or completed.
    /// @spec.requires id and name are not null.
    /// @spec.effects creates a User with the given id and name,
    ///               with levelsPlayed and levelsCompleted initialized as empty sets.
    /// @param id the unique identifier of the user (switchEduId).
    /// @param name the display name of the user.
    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /// Creates a new User with possibly some levels played or completed.
    /// @spec.requires id, name, levelsPlayed, levelsCompleted are not null
    /// @spec.effects creates a User with the given id, name, levelsPlayed, and levelsCompleted.
    /// @param id the unique identifier of the user (switchEduId)
    /// @param name the game name of the user
    /// @param levelsPlayed the set of games played by the user
    /// @param levelsCompleted the set of levels completed by the user
    @PersistenceCreator
    public User(String id, String name, Set<Level> levelsPlayed, Set<Level> levelsCompleted) {
        this.id = id;
        this.name = name;
        this.levelsPlayed.addAll(levelsPlayed);
        this.levelsCompleted.addAll(levelsCompleted);
    }

    ///@return the unique identifier of the user
    public String getId() {
        return id;
    }

    ///@return the gmae name of the user
    public String getName() {
        return name;
    }

    ///@return the set of levels played by the user
    public Set<Level> getLevelsPlayed() {
        return Collections.unmodifiableSet(levelsPlayed);
    }

    ///@return the set of levels completed by the user
    public Set<Level> getLevelsCompleted() {
        return Collections.unmodifiableSet(levelsCompleted);
    }

    /// Adds a level to the set of levels played by the user
    /// @spec.requires level is not null
    /// @spec.modifies this.
    /// @spec.effects adds the given level to the set of levels played by this user.
    /// @param level the level to be added to the set levelsPlayed
    public void addPlayedLevel(Level level) {
        this.levelsPlayed.add(level);
    }

    /// Adds a level to the set of levels completed by the user.
    /// @spec.requires level is not null.
    /// @spec.modifies this.
    /// @spec.effects adds the given level to the set of levels completed by this user.
    /// @param level the level to be added to the set levelsCompleted
    public void addCompletedLevel(Level level) {
        this.levelsCompleted.add(level);
    }
}