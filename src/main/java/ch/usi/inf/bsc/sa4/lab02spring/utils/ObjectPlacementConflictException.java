package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Cannot put object in this place")
public class ObjectPlacementConflictException extends RuntimeException {
    public ObjectPlacementConflictException(){
        super("Cannot put object here");
    }
}

