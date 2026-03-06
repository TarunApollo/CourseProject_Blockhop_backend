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
    private String passwordHash;

    @PersistenceCreator
    public User(String id, String name, String passwordHash) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    public User(String name, String password) {
        this.name = name;
        // sanity checks e.g. empty, regex, ...
        this.passwordHash = PasswordHashingUtils.hashPassword(password);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public User changePassword(String oldPassword, String newPassword) {
        boolean isMatching = PasswordHashingUtils.passwordMatches(oldPassword, this.getPasswordHash());
        if (!isMatching) {
            throw new WrongPasswordException("password doesn't match");
        }

        this.passwordHash = PasswordHashingUtils.hashPassword(newPassword);
        return this;
    }
}
