package ch.usi.inf.bsc.sa4.lab02spring.utils;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User was not found");
    }
}
