package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import java.util.List;

public record UserProfileDTO(
    String name,
    int playedLevelsCount,
    int completedLevelsCount,
    List<LevelDTO> createdLevels // List is better than Set because it preserves order for the frontend
) {
    public UserProfileDTO(User user, int playedLevelsCount, int completedLevelsCount, List<LevelDTO> createdLevels) {
        this(
            user.getName(),
            playedLevelsCount,
            completedLevelsCount,
            createdLevels
        );
    }
}