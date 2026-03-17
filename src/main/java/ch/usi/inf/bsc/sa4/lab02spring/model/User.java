package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private final String id; // switchEduId
    private final String name;


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

    ///@returns the unique identifier of the user
    public String getId() {
        return id;
    }

    ///@returns the name of the user
    public String getName() {
        return name;
    }
}