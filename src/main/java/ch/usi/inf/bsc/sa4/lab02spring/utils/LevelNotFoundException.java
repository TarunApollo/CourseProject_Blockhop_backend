package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested level does not exist")
public class LevelNotFoundException extends RuntimeException {
    public LevelNotFoundException() {
        super("Level not found");
    }
}
