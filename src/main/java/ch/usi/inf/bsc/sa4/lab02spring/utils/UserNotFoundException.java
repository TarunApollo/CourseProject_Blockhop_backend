package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

///
/// Signals that a requested user could not be found.
///
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested user does not exist")
public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    ///
    /// Creates a new exception with the provided message.
    ///
    public UserNotFoundException() {
        super("User was not found");
    }
}
