package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AttemptNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

/// Unit tests for [AttemptService].
@SpringBootTest
@DisplayName("The Attempt Service")
class AttemptServiceTests {

    /// Valid MongoDB id used for the level fixture.
    private static final String LEVEL_ID = "64b64c9f6f4b2c0012345678";

    /// Valid MongoDB id used for the attempt fixture.
    private static final String ATTEMPT_ID = "64b64c9f6f4b2c0012345679";

    /// Valid MongoDB id used for a wrong level fixture.
    private static final String OTHER_LEVEL_ID = "64b64c9f6f4b2c0012345680";

    /// Fixed timestamp used for attempt fixtures.
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.parse("2026-05-20T00:00:00Z");

    /// Fixed lower bound used for recent attempt count queries.
    private static final ZonedDateTime WINDOW_START = ZonedDateTime.parse("2026-05-13T00:00:00Z");

    /// Default attempt DTO for testing attempts.
    private static final AttemptDTO ATTEMPT_DTO = new AttemptDTO(
            Map.of(), new Position(0, 0),
            TIMESTAMP, Duration.ofSeconds(15), false);

    /// Fingerprint fixture used by duplicate lookup tests.
    private static final InputLogFingerprint FINGERPRINT = new InputLogFingerprint(
            "exact-hash",
            "jitter-hash",
            7,
            List.of("bucket-a", "bucket-b"),
            100,
            20);

    /// The service under test.
    @Autowired
    private AttemptService attemptService;

    /// Mocked repository to isolate tests.
    @MockitoBean
    private AttemptRepository attemptRepository;

    /// The test user entity.
    private User testUser;

    /// The test level entity.
    private Level testLevel;

    /// The expected Attempt entity after mapping.
    private Attempt expectedAttempt;

    /// Existing attempt returned by repository stubs.
    private Attempt testAttempt;

    /// User that does not own the attempt fixture.
    private User otherUser;

    /// Sets up test data before each test.
    @BeforeEach
    void setup() {
        this.testUser = new User("user-1", "Mario");
        this.otherUser = new User("user-2", "Luigi");
        this.testLevel = new Level("Test Level", "desc", testUser);
        ReflectionTestUtils.setField(this.testLevel, "id", LEVEL_ID);

        this.expectedAttempt = new Attempt(
                this.testUser,
                ATTEMPT_DTO.timestamp(),
                this.testLevel,
                ATTEMPT_DTO.completed(),
                ATTEMPT_DTO.timeTaken());
        this.testAttempt = new Attempt(
                ATTEMPT_ID,
                this.testUser,
                TIMESTAMP,
                this.testLevel,
                true,
                Duration.ofSeconds(20),
                AttemptVerificationStatus.NOT_VERIFIED);
    }

    /// Tests related to retrieving statistics.
    @Nested
    @DisplayName("when retrieving statistics")
    class Stats {

        /// Verifies delegation to the repository for counting played levels.
        @Test
        @DisplayName("should count played levels correctly")
        void testGetPlayedLevelsCount() {
            Mockito.when(attemptRepository.countDistinctPlayedLevelsByUser(testUser)).thenReturn(5L);

            final long count = attemptService.getPlayedLevelsCount(testUser);
            Assertions.assertEquals(5L, count);
        }

        /// Verifies delegation to the repository for counting completed levels.
        @Test
        @DisplayName("should count completed levels correctly")
        void testGetCompletedLevelsCount() {
            Mockito.when(attemptRepository.countDistinctCompletedLevelsByUser(testUser)).thenReturn(3L);

            final long count = attemptService.getCompletedLevelsCount(testUser);
            Assertions.assertEquals(3L, count);
        }
    }

    /// Tests related to submitting attempts.
    @Nested
    @DisplayName("when submitting an attempt")
    class Submission {

        /// Verifies that the service correctly maps and saves a new attempt.
        @Test
        @DisplayName("should save a new attempt with correct fields from DTO")
        void testSubmitAttemptSaves() {
            attemptService.submitAttempt(testUser, testLevel, ATTEMPT_DTO);

            Mockito.verify(attemptRepository).save(Mockito.refEq(expectedAttempt));
        }
    }

    /// Tests related to retrieving attempts by id.
    @Nested
    @DisplayName("when retrieving an attempt")
    class Retrieval {

        /// Verifies that the service returns the exact attempt loaded by id.
        @Test
        @DisplayName("returns the attempt when the id exists")
        void returnsAttemptById() {
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(testAttempt));

            final Attempt result = attemptService.getAttemptById(ATTEMPT_ID);

            Assertions.assertSame(testAttempt, result);
        }

        /// Verifies that a missing id is surfaced as an attempt-specific exception.
        @Test
        @DisplayName("throws AttemptNotFoundException when the id does not exist")
        void throwsWhenAttemptIsMissing() {
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(AttemptNotFoundException.class,
                    () -> attemptService.getAttemptById(ATTEMPT_ID));
        }
    }

    /// Tests related to updating stored anti-cheat data.
    @Nested
    @DisplayName("when updating anti-cheat data")
    class AntiCheatUpdates {

        /// Verifies that a valid owner and level mutate the status and save it.
        @Test
        @DisplayName("updates the anti-cheat status and saves the attempt")
        void updatesAntiCheatStatus() {
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(testAttempt));

            attemptService.updateAntiCheatStatus(
                    ATTEMPT_ID,
                    testUser,
                    LEVEL_ID,
                    AttemptVerificationStatus.SUSPICIOUS);

            Assertions.assertEquals(AttemptVerificationStatus.SUSPICIOUS, testAttempt.getAntiCheatStatus());
            Mockito.verify(attemptRepository).save(testAttempt);
        }

        /// Verifies that another user cannot mutate an attempt they do not own.
        @Test
        @DisplayName("throws ForbiddenUserException and prevents save for a non-owner")
        void rejectsAntiCheatStatusForWrongUser() {
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(testAttempt));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> attemptService.updateAntiCheatStatus(
                            ATTEMPT_ID,
                            otherUser,
                            LEVEL_ID,
                            AttemptVerificationStatus.CHEATED));
            Mockito.verify(attemptRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a mismatched level id blocks status mutation and persistence.
        @Test
        @DisplayName("throws IllegalArgumentException and prevents save for the wrong level")
        void rejectsAntiCheatStatusForWrongLevel() {
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(testAttempt));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> attemptService.updateAntiCheatStatus(
                            ATTEMPT_ID,
                            testUser,
                            OTHER_LEVEL_ID,
                            AttemptVerificationStatus.CHEATED));
            Mockito.verify(attemptRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a valid owner and level store the replay input log.
        @Test
        @DisplayName("updates the input log and saves the attempt")
        void updatesInputLog() {
            final List<InputFrameDTO> inputLog = List.of(
                    new InputFrameDTO(0, false, true, false, true),
                    new InputFrameDTO(1, false, true, true, true));
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(testAttempt));

            attemptService.updateInputLog(ATTEMPT_ID, testUser, LEVEL_ID, inputLog);

            Assertions.assertSame(inputLog, testAttempt.getInputLog());
            Mockito.verify(attemptRepository).save(testAttempt);
        }

        /// Verifies that another user cannot mutate replay input data.
        @Test
        @DisplayName("throws ForbiddenUserException and prevents input log save for a non-owner")
        void rejectsInputLogForWrongUser() {
            final List<InputFrameDTO> inputLog = List.of(new InputFrameDTO(0, false, true, false, true));
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(testAttempt));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> attemptService.updateInputLog(ATTEMPT_ID, otherUser, LEVEL_ID, inputLog));
            Mockito.verify(attemptRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that replay input data cannot be written for the wrong level.
        @Test
        @DisplayName("throws IllegalArgumentException and prevents input log save for the wrong level")
        void rejectsInputLogForWrongLevel() {
            final List<InputFrameDTO> inputLog = List.of(new InputFrameDTO(0, false, true, false, true));
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(testAttempt));

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> attemptService.updateInputLog(ATTEMPT_ID, testUser, OTHER_LEVEL_ID, inputLog));
            Mockito.verify(attemptRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that a valid owner and level mutate the fingerprint and save it.
        @Test
        @DisplayName("updates the fingerprint and saves the attempt")
        void updatesFingerprint() {
            Mockito.when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(testAttempt));

            attemptService.updateFingerprint(ATTEMPT_ID, testUser, LEVEL_ID, FINGERPRINT);

            Assertions.assertSame(FINGERPRINT, testAttempt.getFingerprint());
            Mockito.verify(attemptRepository).save(testAttempt);
        }
    }

    /// Tests related to duplicate fingerprint lookups.
    @Nested
    @DisplayName("when finding fingerprint duplicates")
    class FingerprintDuplicates {

        /// Verifies that recent status counts are delegated with parsed Mongo ids.
        @Test
        @DisplayName("counts recent attempts with the expected repository arguments")
        void countsRecentAttemptsByStatus() {
            Mockito.when(attemptRepository.countByLevelUserStatusAndTimestampAfterExcludingAttempt(
                    new ObjectId(LEVEL_ID),
                    testUser.getId(),
                    AttemptVerificationStatus.CHEATED,
                    WINDOW_START,
                    new ObjectId(ATTEMPT_ID))).thenReturn(4L);

            final long result = attemptService.countAttemptsByLevelUserStatusAfter(
                    testLevel,
                    testUser,
                    AttemptVerificationStatus.CHEATED,
                    WINDOW_START,
                    ATTEMPT_ID);

            Assertions.assertEquals(4L, result);
        }

        /// Verifies that exact duplicate lookup returns the first repository match.
        @Test
        @DisplayName("returns the first exact fingerprint duplicate")
        void returnsExactDuplicate() {
            Mockito.when(attemptRepository.findExactFingerprintDuplicate(
                    new ObjectId(LEVEL_ID),
                    FINGERPRINT.exactHash(),
                    new ObjectId(ATTEMPT_ID),
                    FINGERPRINT.inputFrameCount(),
                    FINGERPRINT.inputChangeCount())).thenReturn(List.of(testAttempt));

            final Optional<Attempt> result = attemptService.findExactFingerprintDuplicate(
                    testLevel,
                    ATTEMPT_ID,
                    FINGERPRINT);

            Assertions.assertTrue(result.isPresent());
            Assertions.assertSame(testAttempt, result.orElseThrow());
        }

        /// Verifies that exact lookup avoids the repository when the hash is empty.
        @Test
        @DisplayName("returns empty for an empty exact fingerprint without querying")
        void exactDuplicateSkipsEmptyHash() {
            final Optional<Attempt> result = attemptService.findExactFingerprintDuplicate(
                    testLevel,
                    ATTEMPT_ID,
                    InputLogFingerprint.empty());

            Assertions.assertTrue(result.isEmpty());
            Mockito.verifyNoInteractions(attemptRepository);
        }

        /// Verifies that fuzzy duplicate lookup computes the expected metadata range.
        @Test
        @DisplayName("returns the first fuzzy duplicate in the computed metadata range")
        void returnsFuzzyDuplicate() {
            Mockito.when(attemptRepository.findFuzzyFingerprintDuplicateInMetadataRange(
                    new ObjectId(LEVEL_ID),
                    FINGERPRINT.changeBucketHashes(),
                    new ObjectId(ATTEMPT_ID),
                    0,
                    200,
                    10,
                    30)).thenReturn(List.of(testAttempt));

            final Optional<Attempt> result = attemptService.findFuzzyFingerprintDuplicate(
                    testLevel,
                    ATTEMPT_ID,
                    FINGERPRINT);

            Assertions.assertTrue(result.isPresent());
            Assertions.assertSame(testAttempt, result.orElseThrow());
        }

        /// Verifies that fuzzy lookup avoids the repository when no bucket hashes exist.
        @Test
        @DisplayName("returns empty for empty bucket hashes without querying")
        void fuzzyDuplicateSkipsEmptyBuckets() {
            final Optional<Attempt> result = attemptService.findFuzzyFingerprintDuplicate(
                    testLevel,
                    ATTEMPT_ID,
                    InputLogFingerprint.empty());

            Assertions.assertTrue(result.isEmpty());
            Mockito.verifyNoInteractions(attemptRepository);
        }

        /// Verifies that jitter duplicate lookup returns the first repository match.
        @Test
        @DisplayName("returns the first jitter fingerprint duplicate")
        void returnsJitterDuplicate() {
            Mockito.when(attemptRepository.findJitterFingerprintDuplicate(
                    new ObjectId(LEVEL_ID),
                    FINGERPRINT.jitterInputHash(),
                    new ObjectId(ATTEMPT_ID),
                    FINGERPRINT.jitterInputChangeCount())).thenReturn(List.of(testAttempt));

            final Optional<Attempt> result = attemptService.findJitterFingerprintDuplicate(
                    testLevel,
                    ATTEMPT_ID,
                    FINGERPRINT);

            Assertions.assertTrue(result.isPresent());
            Assertions.assertSame(testAttempt, result.orElseThrow());
        }
    }
}
