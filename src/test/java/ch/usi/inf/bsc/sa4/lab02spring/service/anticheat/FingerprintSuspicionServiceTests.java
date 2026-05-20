package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat.AntiCheatSuspicionUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

/// Tests for [FingerprintSuspicionService].
@SpringBootTest
@DisplayName("The Fingerprint Suspicion Service")
class FingerprintSuspicionServiceTests {

    /// Level id for the test.
    private static final String LEVEL_ID = "64b64c9f6f4b2c0012345678";

    /// Attempt id that should be ignored in checks.
    private static final String ATTEMPT_ID = "64b64c9f6f4b2c0012345679";

    /// User id for the test.
    private static final String USER_ID = "user-1";

    /// Fingerprint used by the tests.
    private static final InputLogFingerprint FINGERPRINT = new InputLogFingerprint(
            "exact-hash",
            "jitter-hash",
            10,
            List.of("bucket-a"),
            120,
            50);

    /// Service being tested.
    @Autowired
    private FingerprintSuspicionService fingerprintSuspicionService;

    /// Mocked attempt service.
    @MockitoBean
    private AttemptService attemptService;

    /// Test user.
    private User testUser;

    /// Test level.
    private Level testLevel;

    /// Duplicate attempt returned by mocks.
    private Attempt duplicateAttempt;

    /// Creates fresh test data before each test.
    @BeforeEach
    void setUp() {
        this.testUser = new User(USER_ID, "Mario");
        this.testLevel = new Level("Anticheat", "desc", testUser);
        ReflectionTestUtils.setField(this.testLevel, "id", LEVEL_ID);
        this.duplicateAttempt = new Attempt(
                testUser,
                ZonedDateTime.parse("2026-05-19T00:00:00Z"),
                testLevel,
                true,
                Duration.ofSeconds(12));
    }

    /// Tests for fingerprint classification.
    @Nested
    @DisplayName("when classifying a fingerprint")
    class Classification {

        /// Checks that the service sends all data to the classifier.
        @Test
        @DisplayName("returns the status produced by the fingerprint classifier")
        void returnsClassifierStatus() {
            final Optional<Attempt> exactDuplicate = Optional.of(duplicateAttempt);
            final Optional<Attempt> fuzzyDuplicate = Optional.empty();
            final Optional<Attempt> jitterDuplicate = Optional.empty();

            Mockito.when(attemptService.findExactFingerprintDuplicate(testLevel, ATTEMPT_ID, FINGERPRINT))
                    .thenReturn(exactDuplicate);
            Mockito.when(attemptService.findFuzzyFingerprintDuplicate(testLevel, ATTEMPT_ID, FINGERPRINT))
                    .thenReturn(fuzzyDuplicate);
            Mockito.when(attemptService.findJitterFingerprintDuplicate(testLevel, ATTEMPT_ID, FINGERPRINT))
                    .thenReturn(jitterDuplicate);
            Mockito.when(attemptService.countAttemptsByLevelUserStatusAfter(
                    Mockito.eq(testLevel),
                    Mockito.eq(testUser),
                    Mockito.eq(AttemptVerificationStatus.SUSPICIOUS),
                    Mockito.any(ZonedDateTime.class),
                    Mockito.eq(ATTEMPT_ID))).thenReturn(2L);
            Mockito.when(attemptService.countAttemptsByLevelUserStatusAfter(
                    Mockito.eq(testLevel),
                    Mockito.eq(testUser),
                    Mockito.eq(AttemptVerificationStatus.CHEATED),
                    Mockito.any(ZonedDateTime.class),
                    Mockito.eq(ATTEMPT_ID))).thenReturn(1L);

            try (MockedStatic<AntiCheatSuspicionUtils> mockedStatic =
                    Mockito.mockStatic(AntiCheatSuspicionUtils.class)) {
                mockedStatic.when(() -> AntiCheatSuspicionUtils.classifyFingerprintSuspicion(
                        FINGERPRINT,
                        exactDuplicate,
                        fuzzyDuplicate,
                        jitterDuplicate,
                        2L,
                        1L)).thenReturn(AttemptVerificationStatus.CHEATED);

                final AttemptVerificationStatus result = fingerprintSuspicionService.classify(
                        testLevel,
                        testUser,
                        ATTEMPT_ID,
                        FINGERPRINT);

                Assertions.assertEquals(AttemptVerificationStatus.CHEATED, result);
                mockedStatic.verify(() -> AntiCheatSuspicionUtils.classifyFingerprintSuspicion(
                        FINGERPRINT,
                        exactDuplicate,
                        fuzzyDuplicate,
                        jitterDuplicate,
                        2L,
                        1L));
            }
        }

        /// Checks that history uses the last seven days.
        @Test
        @DisplayName("uses a seven day history window for previous suspicious attempts")
        void usesSevenDayHistoryWindow() {
            final ZonedDateTime beforeCall = ZonedDateTime.now().minusDays(7).minusSeconds(1);
            Mockito.when(attemptService.findExactFingerprintDuplicate(testLevel, ATTEMPT_ID, FINGERPRINT))
                    .thenReturn(Optional.empty());
            Mockito.when(attemptService.findFuzzyFingerprintDuplicate(testLevel, ATTEMPT_ID, FINGERPRINT))
                    .thenReturn(Optional.empty());
            Mockito.when(attemptService.findJitterFingerprintDuplicate(testLevel, ATTEMPT_ID, FINGERPRINT))
                    .thenReturn(Optional.empty());
            Mockito.when(attemptService.countAttemptsByLevelUserStatusAfter(
                    Mockito.eq(testLevel),
                    Mockito.eq(testUser),
                    Mockito.eq(AttemptVerificationStatus.SUSPICIOUS),
                    Mockito.any(ZonedDateTime.class),
                    Mockito.eq(ATTEMPT_ID))).thenReturn(0L);
            Mockito.when(attemptService.countAttemptsByLevelUserStatusAfter(
                    Mockito.eq(testLevel),
                    Mockito.eq(testUser),
                    Mockito.eq(AttemptVerificationStatus.CHEATED),
                    Mockito.any(ZonedDateTime.class),
                    Mockito.eq(ATTEMPT_ID))).thenReturn(0L);

            try (MockedStatic<AntiCheatSuspicionUtils> mockedStatic =
                    Mockito.mockStatic(AntiCheatSuspicionUtils.class)) {
                mockedStatic.when(() -> AntiCheatSuspicionUtils.classifyFingerprintSuspicion(
                        FINGERPRINT,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        0L,
                        0L)).thenReturn(AttemptVerificationStatus.LEGIT);

                fingerprintSuspicionService.classify(testLevel, testUser, ATTEMPT_ID, FINGERPRINT);
            }

            final ArgumentCaptor<ZonedDateTime> windowCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
            Mockito.verify(attemptService).countAttemptsByLevelUserStatusAfter(
                    Mockito.eq(testLevel),
                    Mockito.eq(testUser),
                    Mockito.eq(AttemptVerificationStatus.SUSPICIOUS),
                    windowCaptor.capture(),
                    Mockito.eq(ATTEMPT_ID));
            final ZonedDateTime afterCall = ZonedDateTime.now().minusDays(7).plusSeconds(1);
            Assertions.assertFalse(windowCaptor.getValue().isBefore(beforeCall));
            Assertions.assertFalse(windowCaptor.getValue().isAfter(afterCall));
        }
    }
}
