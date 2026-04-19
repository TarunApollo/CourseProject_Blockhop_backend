package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import java.util.List;

public record UserProfileDTO(
        String name,
        long playedLevelsCount,
        long completedLevelsCount,
        List<CreatedLevelProfileDTO> createdLevels // List is better than Set because it preserves order for the frontend
) {
    /// Constructs a UserProfileDTO from the given User entity and statistics.
    /// @param user the user whose profile to represent
    /// @param playedLevelsCount the number of distinct levels the user has played
    /// @param completedLevelsCount the number of distinct levels the user has completed
    /// @param createdLevels the list of levels created by the user as LevelDTOs
    public UserProfileDTO(User user, long playedLevelsCount, long completedLevelsCount, List<CreatedLevelProfileDTO> createdLevels) {
        this(
                user.getName(),
                playedLevelsCount,
                completedLevelsCount,
                createdLevels
        );
    }
}