package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "This object is unknown")
public class UnknownObjectTypeException extends RuntimeException {
    public UnknownObjectTypeException(){
        super("This object is unknown");
    }
}

