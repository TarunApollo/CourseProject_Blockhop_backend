package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.UNAUTHORIZED, reason="Wrong old password")
public class WrongPasswordException extends RuntimeException {
  public WrongPasswordException(String message) {
    super(message);
  }
}
