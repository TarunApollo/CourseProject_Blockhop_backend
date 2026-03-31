package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Level is not published and cannot be played")
public class LevelNotPlayableException extends RuntimeException {
    public LevelNotPlayableException() {
        super("Level is not published");
    }
}
