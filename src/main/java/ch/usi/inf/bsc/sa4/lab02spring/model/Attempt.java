package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

@Document(collection = "attempts")
public record Attempt(User user, ZonedDateTime timestamp, Level level, boolean completed) {
}
