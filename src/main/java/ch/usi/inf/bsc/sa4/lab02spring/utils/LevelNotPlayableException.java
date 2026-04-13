package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user tries to play a level that is not currently playable.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Level is not published and cannot be played")
public class LevelNotPlayableException extends RuntimeException {
    /// Creates a new exception for an unpublished and unplayable level.
    public LevelNotPlayableException() {
        super("Level is not published");
    }
}
