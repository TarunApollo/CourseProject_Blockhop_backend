package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "This action is not allowed on this level.")
public class ForbiddenLevelActionException extends RuntimeException {
    public ForbiddenLevelActionException(final String message) { super(message); }
}
