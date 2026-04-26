package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/// Represents a user in the system.
/// A user has a stable identifier and a display name.
@Document(collection = "users")
public class User {
    /// Unique identifier of the user.
    @Id
    private final String id; // switchEduId
    /// Display name of the user.
    private final String name;


    /// Creates a new User with no levels played or completed.
    /// @spec.requires id and name are not null.
    /// @spec.effects creates a User with the given id and name,
    ///               with levelsPlayed and levelsCompleted initialized as empty sets.
    /// @param id the unique identifier of the user (switchEduId).
    /// @param name the display name of the user.
    public User(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    /// @return the unique identifier of the user
    public String getId() {
        return id;
    }

    /// @return the name of the user
    public String getName() {
        return name;
    }
}