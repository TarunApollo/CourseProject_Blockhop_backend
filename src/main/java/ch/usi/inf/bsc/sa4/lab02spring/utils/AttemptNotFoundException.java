package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/// Exception thrown when an attempt cannot be found.
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "The requested attempt does not exist")
public class AttemptNotFoundException extends RuntimeException {

    /// serial version uid for sonarqube remove this rule
    public static final long serialVersionUID = 4328743;

    /// Constructs an exception for a missing attempt.
    public AttemptNotFoundException() {
        super("Attempt not found");
    }
}
