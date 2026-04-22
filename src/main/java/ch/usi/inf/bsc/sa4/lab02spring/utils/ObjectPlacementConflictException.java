package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

///
/// Signals that an object cannot be placed because
/// the target position is already occupied
/// or otherwise invalid for placement.
///
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Cannot put object in this place")
public class ObjectPlacementConflictException extends RuntimeException {

    ///
    /// Creates a new exception with the provided message.
    ///
    public ObjectPlacementConflictException(){
        super("Cannot put object here");
    }
}

