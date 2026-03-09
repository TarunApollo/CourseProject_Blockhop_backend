package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Can't modify an already published level")
public class LevelPublishedException extends RuntimeException {
    public LevelPublishedException(String message){
        super(message);
    }
}
