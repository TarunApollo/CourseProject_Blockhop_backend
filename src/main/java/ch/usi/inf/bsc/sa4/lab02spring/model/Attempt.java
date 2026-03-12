package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

@Document(collection = "attempts")
public record Attempt(
        @Id
        String id,
        @DBRef
        User user,
        ZonedDateTime timestamp,
        @DBRef
        Level level,
        boolean completed,
        Duration timeTaken)
{}