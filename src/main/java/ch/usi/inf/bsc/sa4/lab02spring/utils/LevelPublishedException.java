package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

///
/// Signals that an operation was attempted on a level that has already been published.
///
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Can't modify an already published level")
public class LevelPublishedException extends RuntimeException {

    ///
    /// Creates a new exception with the provided message.
    /// @param message the detail message describing the publication conflict
    ///
    public LevelPublishedException(final String message){
        super(message);
    }
}
