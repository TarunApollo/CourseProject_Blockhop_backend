package ch.usi.inf.bsc.sa4.lab02spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

///
/// Entry point for the Lab 02 Spring Boot application.
///
@SpringBootApplication
public class Lab02SpringApplication {

  /// Starts the Spring Boot application.
  /// @param args command-line arguments passed to the application
  ///
  public static void main(String[] args) {
    SpringApplication.run(Lab02SpringApplication.class, args);
  }

}
