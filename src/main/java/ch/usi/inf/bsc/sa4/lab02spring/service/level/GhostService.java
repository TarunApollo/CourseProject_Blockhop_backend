package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.GhostDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/// Resolves the ghost replay (fastest replayable attempt) for a level.
///
/// The ghost shown is global: it is the fastest completed attempt on the
/// level across all players, not the current player's personal best. The
/// current player must, however, have completed the level at least once
/// themselves before any ghost is returned to them.
///
/// Only replay-verified (`LEGIT`) attempts are eligible both for unlocking
/// ghost access and for the global ghost shown on the level.
@Service
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring-managed singleton; injected collaborators are intentionally shared")
public class GhostService {

    /// Loads the level whose ghost is being requested.
    private final LevelRepository levelRepository;

    /// Looks up ghost-candidate attempts and the current player's completion.
    private final AttemptRepository attemptRepository;

    /// Creates a ghost service with its collaborators.
    ///
    /// @param levelRepository   loads the requested level
    /// @param attemptRepository runs the ghost-candidate and completion queries
    @Autowired
    public GhostService(
            final LevelRepository levelRepository,
            final AttemptRepository attemptRepository) {
        this.levelRepository = levelRepository;
        this.attemptRepository = attemptRepository;
    }

    /// Returns the ghost replay for the given level if one is available to the
    /// current player.
    ///
    /// The result is empty when the current player has not yet completed the
    /// level themselves, or when no completed attempt with a non-empty input
    /// log exists for the level.
    ///
    /// @spec.requires levelId and currentUser are not null.
    /// @param levelId     the id of the published level whose ghost is requested
    /// @param currentUser the authenticated player making the request
    /// @return the ghost DTO when available, otherwise empty
    /// @throws LevelNotFoundException if the level does not exist or is not
    ///         published (the same 404 is returned in both cases so that
    ///         unpublished levels are not enumerable through this endpoint)
    public Optional<GhostDTO> getGhostForLevel(final String levelId, final User currentUser) {
        final Level level = this.levelRepository.findById(levelId)
                .orElseThrow(LevelNotFoundException::new);
        if (!level.isPublished()) {
            throw new LevelNotFoundException();
        }
        if (!this.attemptRepository.existsByUserAndLevelAndCompletedTrueAndAntiCheatStatus(
                currentUser,
                level,
                AttemptVerificationStatus.LEGIT)) {
            return Optional.empty();
        }

        final ObjectId levelObjectId = new ObjectId(level.getId());
        final Optional<Attempt> best = this.attemptRepository.findFastestVerifiedGhostCandidate(
                levelObjectId,
                AttemptVerificationStatus.LEGIT);

        return best.map(attempt -> new GhostDTO(
                attempt.getId(),
                attempt.getInputLog(),
                attempt.getTimeTaken().toMillis(),
                attempt.getUser().getName()));
    }
}
