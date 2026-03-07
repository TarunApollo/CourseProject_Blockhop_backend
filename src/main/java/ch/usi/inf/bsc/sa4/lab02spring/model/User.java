package ch.usi.inf.bsc.sa4.lab02spring.model;

import ch.usi.inf.bsc.sa4.lab02spring.utils.PasswordHashingUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@SuppressWarnings("NullAway.Init")
public class User {
    @Id
    private String id;
    private final String name;

    @PersistenceCreator
    public User(String id, String name, String passwordHash) {
        this.id = id;
        this.name = name;
    }

    public User(String name, String password) {
        this.name = name;
        // sanity checks e.g. empty, regex, ...
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
