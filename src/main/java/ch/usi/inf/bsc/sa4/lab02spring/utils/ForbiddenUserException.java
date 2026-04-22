package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

///
/// Signals that the current user is not authorized to perform the requested operation.
///
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The current user is not allowed to do this operation")
public class ForbiddenUserException extends RuntimeException {

    ///
    /// Creates a new exception with the provided message.
    /// @param message the detail message describing why the user is forbidden
    ///
    public ForbiddenUserException(final String message) { super(message); }
}
