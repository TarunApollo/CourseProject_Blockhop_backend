package ch.usi.inf.bsc.sa4.lab02spring.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions thrown by REST controllers and maps them to HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles invalid arguments supplied to controller methods.
     *
     * @param e the thrown exception
     * @return a {@code 400 Bad Request} response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().build();
    }

    /**
     * Handles invalid state encountered during request processing.
     *
     * @param e the thrown exception
     * @return a {@code 400 Bad Request} response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Void> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().build();
    }
    
}
