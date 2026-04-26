package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

///
/// Signals that an object type is not recognized by the application.
///
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "This object is unknown")
public class UnknownObjectTypeException extends RuntimeException {

    ///
    /// Creates a new exception with the provided message.
    ///
    public UnknownObjectTypeException(){
        super("This object is unknown");
    }
}

