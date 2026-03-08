package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;

public record LevelDTO(
    String id,
    String title,
    String description,
    String creatorId,
    boolean published
) {

    public LevelDTO(Level level) {
        this(
            level.getId(),
            level.getTitle(),
            level.getDescription(),
            level.getCreatorId(),
            level.isPublished()
        );
    }
}