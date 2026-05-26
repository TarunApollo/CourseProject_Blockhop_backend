package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AttemptNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/// Service handling creation and querying of player attempts.
@Service
public class AttemptService {
    /// Relative frame-count tolerance used for fuzzy fingerprint matching.
    private static final double FRAME_TOLERANCE = 1.0;

    /// Relative input-change tolerance used for fuzzy fingerprint matching.
    private static final double CHANGE_TOLERANCE = 0.5;

    /// Minimum frame-count tolerance applied to fuzzy fingerprint matching.
    private static final int MIN_FRAME = 5;

    /// Minimum input-change tolerance applied to fuzzy fingerprint matching.
    private static final int MIN_COUNT = 2;

    /// Repository handling attempt persistence.
    private final AttemptRepository attemptRepository;

    /// Constructs a new AttemptService with the given dependency.
    ///
    /// @param attemptRepository the repository for accessing attempt data
    @Autowired
    public AttemptService(final AttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    /// Returns the number of distinct levels the given user has played.
    /// 
    /// @spec.requires user is not null.
    /// @param user the user whose played levels to count
    /// @return the number of distinct levels the user has at least one attempt on
    public long getPlayedLevelsCount(final User user) {
        return this.attemptRepository.countDistinctPlayedLevelsByUser(user);
    }

    /// Returns the number of distinct levels the given user has completed.
    /// 
    /// @param user the user whose completed levels to count
    /// @return the number of distinct levels the user has completed at least once
    public long getCompletedLevelsCount(final User user) {
        return this.attemptRepository.countDistinctCompletedLevelsByUser(user);
    }

    /// Counts recent attempts for a level, user, and anti-cheat status.
    /// 
    /// @param level             the level whose attempts are counted
    /// @param user              the user whose attempts are counted
    /// @param status            the anti-cheat status to match
    /// @param after             the lower timestamp bound
    /// @param excludedAttemptId the attempt id excluded from the count
    /// @return the number of matching attempts after the timestamp
    public long countAttemptsByLevelUserStatusAfter(final Level level,
            final User user,
            final AttemptVerificationStatus status,
            final ZonedDateTime after,
            final String excludedAttemptId) {
        return this.attemptRepository.countByLevelUserStatusAndTimestampAfterExcludingAttempt(
                getValidObjectId(level.getId()),
                user.getId(),
                status,
                after,
                getValidObjectId(excludedAttemptId));
    }

    /// Records a new attempt for the given user on the given level.
    ///
    /// @spec.requires user, level, and dto are not null.
    /// @spec.effects creates a new Attempt from the provided data and saves it to
    ///               the repository; if completed is true, the attempt is marked as
    ///               completed before saving.
    /// @param user  the user who performed the attempt
    /// @param level the level the attempt was made on
    /// @param dto   the DTO containing the attempt timestamp and time taken
    ///              condition
    /// @return the saved attempt
    public Attempt submitAttempt(final User user, final Level level, final AttemptDTO dto) {
        if (dto.timeTaken() == null) {
            throw new IllegalArgumentException("Attempt timeTaken cannot be null");
        }
        final Attempt attempt = new Attempt(
                user,
                dto.timestamp(),
                level,
                dto.completed(),
                dto.timeTaken());
        return this.attemptRepository.save(attempt);
    }

    /// Retrieves an attempt by id.
    /// 
    /// @param attemptId the id of the attempt to retrieve
    /// @return the matching attempt
    /// @throws AttemptNotFoundException if no attempt has the given id
    public Attempt getAttemptById(final String attemptId) {
        return this.attemptRepository.findById(attemptId)
                .orElseThrow(AttemptNotFoundException::new);
    }

    /// Updates the anti-cheat status of an attempt owned by the given user.
    /// 
    /// @param attemptId       the id of the attempt to update
    /// @param user            the owner expected for the attempt
    /// @param levelId         the level expected for the attempt
    /// @param antiCheatStatus the anti-cheat status to store
    public void updateAntiCheatStatus(final String attemptId,
            final User user,
            final String levelId,
            final AttemptVerificationStatus antiCheatStatus) {
        final Attempt attempt = getAttemptById(attemptId);
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new ForbiddenUserException("Attempt does not belong to this user");
        }
        if (!attempt.getLevel().getId().equals(levelId)) {
            throw new IllegalArgumentException("Attempt does not belong to this level");
        }
        attempt.setAntiCheatStatus(antiCheatStatus);
        this.attemptRepository.save(attempt);
    }

    /// Stores the computed input fingerprint for an attempt.
    ///
    /// @param attemptId   the id of the attempt to update
    /// @param user        the owner expected for the attempt
    /// @param levelId     the level expected for the attempt
    /// @param fingerprint the fingerprint to store on the attempt
    public void updateFingerprint(final String attemptId,
            final User user,
            final String levelId,
            final InputLogFingerprint fingerprint) {
        final Attempt attempt = getAttemptById(attemptId);
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new ForbiddenUserException("Attempt does not belong to this user");
        }
        if (!attempt.getLevel().getId().equals(levelId)) {
            throw new IllegalArgumentException("Attempt does not belong to this level");
        }
        attempt.setFingerprint(fingerprint);
        this.attemptRepository.save(attempt);
    }

    /// Stores the recorded input log for an attempt so it can later be replayed
    /// as a ghost.
    ///
    /// Loads the attempt fresh and saves it back; callers must not pre-load the
    /// attempt themselves, since other replay-submission updates may have
    /// persisted changes between loads.
    ///
    /// @param attemptId the id of the attempt to update
    /// @param user      the owner expected for the attempt
    /// @param levelId   the level expected for the attempt
    /// @param inputLog  the per-frame input log to store on the attempt
    public void updateInputLog(final String attemptId,
            final User user,
            final String levelId,
            final List<InputFrameDTO> inputLog) {
        final Attempt attempt = getAttemptById(attemptId);
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new ForbiddenUserException("Attempt does not belong to this user");
        }
        if (!attempt.getLevel().getId().equals(levelId)) {
            throw new IllegalArgumentException("Attempt does not belong to this level");
        }
        attempt.setInputLog(inputLog);
        this.attemptRepository.save(attempt);
    }

    /// Finds another attempt on the same level with the same exact fingerprint.
    /// 
    /// @param level            the level that contains the current attempt
    /// @param currentAttemptId the id of the attempt excluded from matching
    /// @param fingerprint      the fingerprint of the current attempt
    /// @return a duplicate attempt when one exists
    public Optional<Attempt> findExactFingerprintDuplicate(final Level level,
            final String currentAttemptId,
            final InputLogFingerprint fingerprint) {
        return fingerprint.exactHash().isEmpty()
                ? Optional.empty()
                : this.attemptRepository.findExactFingerprintDuplicate(
                        getValidObjectId(level.getId()),
                        fingerprint.exactHash(),
                        getValidObjectId(currentAttemptId),
                        fingerprint.inputFrameCount(),
                        fingerprint.inputChangeCount()).stream().findFirst();
    }

    /// Finds another attempt with an overlapping fuzzy fingerprint.
    /// 
    /// @param level            the level that contains the current attempt
    /// @param currentAttemptId the id of the attempt excluded from matching
    /// @param fingerprint      the fingerprint of the current attempt
    /// @return a fuzzy duplicate attempt when one exists
    public Optional<Attempt> findFuzzyFingerprintDuplicate(final Level level,
            final String currentAttemptId,
            final InputLogFingerprint fingerprint) {
        final MetadataRanges ranges = metadataRanges(fingerprint, FRAME_TOLERANCE, CHANGE_TOLERANCE);
        return fingerprint.changeBucketHashes().isEmpty()
                ? Optional.empty()
                : this.attemptRepository.findFuzzyFingerprintDuplicateInMetadataRange(
                        getValidObjectId(level.getId()),
                        fingerprint.changeBucketHashes(),
                        getValidObjectId(currentAttemptId),
                        ranges.minFrameCount(),
                        ranges.maxFrameCount(),
                        ranges.minInputChanges(),
                        ranges.maxInputChanges()).stream().findFirst();
    }

    /// Finds another attempt with the same jitter-normalized fingerprint.
    /// 
    /// @param level            the level that contains the current attempt
    /// @param currentAttemptId the id of the attempt excluded from matching
    /// @param fingerprint      the fingerprint of the current attempt
    /// @return a jitter duplicate attempt when one exists
    public Optional<Attempt> findJitterFingerprintDuplicate(final Level level,
            final String currentAttemptId,
            final InputLogFingerprint fingerprint) {
        return fingerprint.jitterInputHash().isEmpty()
                ? Optional.empty()
                : this.attemptRepository.findJitterFingerprintDuplicate(
                        getValidObjectId(level.getId()),
                        fingerprint.jitterInputHash(),
                        getValidObjectId(currentAttemptId),
                        fingerprint.jitterInputChangeCount()).stream().findFirst();
    }

    /// Computes metadata bounds used to limit fuzzy fingerprint duplicate queries.
    ///
    /// @param fingerprint    fingerprint whose metadata defines the range center
    /// @param frameTolRatio  relative tolerance for frame counts
    /// @param changeTolRatio relative tolerance for input change counts
    /// @return inclusive metadata ranges for fuzzy matching
    private static MetadataRanges metadataRanges(final InputLogFingerprint fingerprint,
            final double frameTolRatio,
            final double changeTolRatio) {
        final int frameTolerance = tolerance(
                fingerprint.inputFrameCount(),
                MIN_FRAME,
                frameTolRatio);
        final int changeTolerance = tolerance(
                fingerprint.inputChangeCount(),
                MIN_COUNT,
                changeTolRatio);

        return new MetadataRanges(
                Math.max(0, fingerprint.inputFrameCount() - frameTolerance),
                fingerprint.inputFrameCount() + frameTolerance,
                Math.max(0, fingerprint.inputChangeCount() - changeTolerance),
                fingerprint.inputChangeCount() + changeTolerance);
    }

    /// Computes the larger of the minimum tolerance and the ratio-based tolerance.
    ///
    /// @param value          metadata value used to compute the ratio-based
    ///                       tolerance
    /// @param minTolerance   lower bound for the tolerance
    /// @param toleranceRatio ratio applied to the metadata value
    /// @return the effective tolerance
    private static int tolerance(final int value,
            final int minTolerance,
            final double toleranceRatio) {
        return Math.max(minTolerance, (int) Math.ceil(value * toleranceRatio));
    }

    /// Parses and validates a MongoDB object id string.
    ///
    /// @param id string representation of the object id
    /// @return parsed ObjectId
    /// @throws IllegalArgumentException if the id is not a valid ObjectId
    private static ObjectId getValidObjectId(final String id) {
        if (!ObjectId.isValid(id)) {
            throw new IllegalArgumentException(id + " is not a valid ObjectId");
        }
        return new ObjectId(id);
    }

    /// Inclusive metadata bounds used by fuzzy fingerprint duplicate queries.
    ///
    /// @param minFrameCount   minimum accepted frame count
    /// @param maxFrameCount   maximum accepted frame count
    /// @param minInputChanges minimum accepted input-change count
    /// @param maxInputChanges maximum accepted input-change count
    private record MetadataRanges(
            int minFrameCount,
            int maxFrameCount,
            int minInputChanges,
            int maxInputChanges) {
    }
}
