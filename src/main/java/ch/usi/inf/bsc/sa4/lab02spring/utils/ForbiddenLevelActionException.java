package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

///
/// Signals that the requested operation is not allowed for the current level state.
///
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "This action is not allowed on this level.")
public class ForbiddenLevelActionException extends RuntimeException {
    ///
    /// Creates a new exception with the provided message.
    /// @param message the detail message describing why the action is forbidden
    ///
    public ForbiddenLevelActionException(final String message) { super(message); }
}
