package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import org.springframework.stereotype.Service;

/// Validates whether a submitted attempt satisfies a level's completion rules.
@Service
public class AttemptValidationService {
    /// Validates whether the submitted attempt satisfies the level's completion
    /// criteria.
    ///
    /// @spec.requires level and dto are not null.
    /// @spec.effects compares the submitted world layer against the level's
    ///               expected world layer and checks whether the player's
    ///               position corresponds to an ExitDoor in the object layer.
    /// @param level the level to validate the submission against
    /// @param dto   the DTO containing the submitted world layer and player
    ///              position
    /// @return true if the world layer matches the expected state and the
    ///         player is positioned on an ExitDoor, false otherwise
    public boolean validateLevelSubmission(final Level level, final AttemptDTO dto) {
        final boolean isWorldLayerEqual = level.getWorldLayer().equals(dto.worldLayer());
        return isWorldLayerEqual;
    }
}
