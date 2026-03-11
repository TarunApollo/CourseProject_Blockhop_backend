package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

public record LevelDTO(
    String id,
    String title,
    String description,
    User creator,
    boolean published
) {

    public LevelDTO(Level level) {
        this(
            level.getId(),
            level.getTitle(),
            level.getDescription(),
            level.getCreator(),
            level.isPublished()
        );
    }
}