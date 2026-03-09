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
    private final Set<String> completedLevelsIds;
    private final Set<String> playedLevelsIds;

    @PersistenceCreator
    public User(String id, String name, Set<String> completedLevelsIds, Set<String> playedLevelsIds) {
        this.id = id;
        this.name = name;
        this.completedLevelsIds = completedLevelsIds == null ? new HashSet<>() : completedLevelsIds;
        this.playedLevelsIds = playedLevelsIds == null ? new HashSet<>() : playedLevelsIds;
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
        this.completedLevelsIds = new HashSet<>();
        this.playedLevelsIds = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getCompletedLevelsIds() { return completedLevelsIds; }

    public Set<String> getPlayedLevelsIds() { return playedLevelsIds; }
}
