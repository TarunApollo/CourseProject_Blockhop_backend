package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The current user is not allowed to do this operation")
public class ForbiddenUserException extends RuntimeException {
    public ForbiddenUserException(final String message) { super(message); }
}
