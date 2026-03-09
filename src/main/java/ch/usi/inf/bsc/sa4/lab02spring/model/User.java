package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Document(collection = "users")
@SuppressWarnings("NullAway.Init")
public class User {
    @Id
    private final String id; // switchEduId
    private final String name;

    @PersistenceCreator
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
}
