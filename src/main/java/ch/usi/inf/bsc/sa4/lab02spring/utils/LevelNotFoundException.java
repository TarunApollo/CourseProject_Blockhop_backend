package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

///
/// Signals that a requested level could not be found.
///
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested level does not exist")
public class LevelNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    ///
    /// Creates a new exception with the default message.
    ///
    public LevelNotFoundException() {
        super("Level not found");
    }
}
