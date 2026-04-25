package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateAttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for the AttemptService.
 * Goal: Test service logic in isolation using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("The Attempt Service (Unit)")
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "NullAway" })
/* default */ class AttemptServiceTests {

    /** The mocked attempt repository. */
    @Mock
    private AttemptRepository attemptRepository;

    /** The mocked level repository. */
    @Mock
    private LevelRepository levelRepository;

    /** The service under test. */
    @InjectMocks
    private AttemptService attemptService;

    /** The test user. */
    private User testUser;

    /** The test level. */
    private Level testLevel;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    /* default */ void setup() {
        this.testUser = new User("user-1", "Mario");
        this.testLevel = new Level("Test Level", "Desc", this.testUser);
    }

    /**
     * Tests for the createAttempt method.
     */
    @Nested
    @DisplayName("when creating an attempt")
    /* default */ class Creation {

        /**
         * Verifies that a valid attempt is saved and returned.
         */
        @Test
        @DisplayName("should save and return a non null attempt")
        /* default */ void testCreateAttemptNotNull() {
            final CreateAttemptDTO dto = new CreateAttemptDTO("level-1", true, Duration.ofSeconds(10));
            final Attempt savedAttempt = new Attempt("attempt-1", testUser, java.time.ZonedDateTime.now(), testLevel,
                    true, Duration.ofSeconds(10));

            Mockito.when(levelRepository.findById("level-1")).thenReturn(Optional.of(testLevel));
            Mockito.when(attemptRepository.save(Mockito.any(Attempt.class))).thenReturn(savedAttempt);

            final Attempt result = attemptService.createAttempt(testUser, dto);

            Assertions.assertNotNull(result);
        }

        /**
         * Verifies that the returned attempt has the correct ID.
         */
        @Test
        @DisplayName("should return the correct attempt ID")
        /* default */ void testCreateAttemptCorrectId() {
            final CreateAttemptDTO dto = new CreateAttemptDTO("level-1", true, Duration.ofSeconds(10));
            final Attempt savedAttempt = new Attempt("attempt-1", testUser, java.time.ZonedDateTime.now(), testLevel,
                    true, Duration.ofSeconds(10));

            Mockito.when(levelRepository.findById("level-1")).thenReturn(Optional.of(testLevel));
            Mockito.when(attemptRepository.save(Mockito.any(Attempt.class))).thenReturn(savedAttempt);

            final Attempt result = attemptService.createAttempt(testUser, dto);

            Assertions.assertEquals("attempt-1", result.getId());
        }

        /**
         * Verifies that an exception is thrown if the level is not found.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException if level does not exist")
        /* default */ void testCreateAttemptLevelNotFound() {
            final CreateAttemptDTO dto = new CreateAttemptDTO("non-existent", true, Duration.ZERO);
            Mockito.when(levelRepository.findById("non-existent")).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class, () -> attemptService.createAttempt(testUser, dto));
        }
    }

    /**
     * Tests for retrieval and statistics methods.
     */
    @Nested
    @DisplayName("when retrieving attempts and stats")
    /* default */ class Stats {

        /**
         * Verifies retrieval of attempts by user returns correct result.
         */
        @Test
        @DisplayName("should find attempts by user")
        /* default */ void testGetAttemptsByUser() {
            final List<Attempt> attempts = List.of(new Attempt("1", testUser, null, testLevel, true, null));
            Mockito.when(attemptRepository.findByUser(testUser)).thenReturn(attempts);
            final List<Attempt> result = attemptService.getAttemptsByUser(testUser);
            Assertions.assertEquals(1, result.size());
        }

        /**
         * Verifies retrieval of attempts by user delegates to repository.
         */
        @Test
        @DisplayName("should delegate to the repository when getting attempts by user")
        /* default */ void testGetAttemptsByUserCallsRepo() {
            Mockito.when(attemptRepository.findByUser(testUser)).thenReturn(List.of());
            attemptService.getAttemptsByUser(testUser);
            Mockito.verify(attemptRepository).findByUser(testUser);
        }

        /**
         * Verifies played levels count returns correct value.
         */
        @Test
        @DisplayName("should count played levels correctly")
        /* default */ void testGetPlayedLevelsCount() {
            Mockito.when(attemptRepository.countDistinctPlayedLevelsByUser(testUser)).thenReturn(5L);
            final long count = attemptService.getPlayedLevelsCount(testUser);
            Assertions.assertEquals(5L, count);
        }

        /**
         * Verifies played levels count delegates to repository.
         */
        @Test
        @DisplayName("should delegate to the repository when counting played levels")
        /* default */ void testGetPlayedLevelsCountCallsRepo() {
            Mockito.when(attemptRepository.countDistinctPlayedLevelsByUser(testUser)).thenReturn(5L);
            attemptService.getPlayedLevelsCount(testUser);
            Mockito.verify(attemptRepository).countDistinctPlayedLevelsByUser(testUser);
        }

        /**
         * Verifies completed levels count returns correct value.
         */
        @Test
        @DisplayName("should count completed levels correctly")
        /* default */ void testGetCompletedLevelsCount() {
            Mockito.when(attemptRepository.countDistinctCompletedLevelsByUser(testUser)).thenReturn(3L);
            final long count = attemptService.getCompletedLevelsCount(testUser);
            Assertions.assertEquals(3L, count);
        }

        /**
         * Verifies completed levels count delegates to repository.
         */
        @Test
        @DisplayName("should delegate to the repository when counting completed levels")
        /* default */ void testGetCompletedLevelsCountCallsRepo() {
            Mockito.when(attemptRepository.countDistinctCompletedLevelsByUser(testUser)).thenReturn(3L);
            attemptService.getCompletedLevelsCount(testUser);
            Mockito.verify(attemptRepository).countDistinctCompletedLevelsByUser(testUser);
        }
    }
}