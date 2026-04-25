package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateAttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AttemptService.
 * Goal: Test service logic in isolation using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("The Attempt Service (Unit)")
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "NullAway" })
class AttemptServiceTests {

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
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class Creation {

        /**
         * Verifies that a valid attempt is saved and returned.
         */
        @Test
        @DisplayName("should save and return a new attempt")
        void testCreateAttempt() {
            final CreateAttemptDTO dto = new CreateAttemptDTO("level-1", true, Duration.ofSeconds(10));
            final Attempt savedAttempt = new Attempt("attempt-1", testUser, java.time.ZonedDateTime.now(), testLevel,
                    true, Duration.ofSeconds(10));

            when(levelRepository.findById("level-1")).thenReturn(Optional.of(testLevel));
            when(attemptRepository.save(any(Attempt.class))).thenReturn(savedAttempt);

            final Attempt result = attemptService.createAttempt(testUser, dto);

            assertNotNull(result);
            assertEquals("attempt-1", result.getId());
            verify(levelRepository).findById("level-1");
            verify(attemptRepository).save(any(Attempt.class));
        }

        /**
         * Verifies that an exception is thrown if the level is not found.
         */
        @Test
        @DisplayName("should throw LevelNotFoundException if level does not exist")
        void testCreateAttemptLevelNotFound() {
            final CreateAttemptDTO dto = new CreateAttemptDTO("non-existent", true, Duration.ZERO);
            when(levelRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class, () -> attemptService.createAttempt(testUser, dto));
        }
    }

    /**
     * Tests for retrieval and statistics methods.
     */
    @Nested
    @DisplayName("when retrieving attempts and stats")
    @SuppressWarnings("PMD.AtLeastOneConstructor")
    /* default */ class Stats {

        /**
         * Verifies retrieval of attempts by user.
         */
        @Test
        @DisplayName("should find attempts by user")
        void testGetAttemptsByUser() {
            final List<Attempt> attempts = List.of(new Attempt("1", testUser, null, testLevel, true, null));
            when(attemptRepository.findByUser(testUser)).thenReturn(attempts);

            final List<Attempt> result = attemptService.getAttemptsByUser(testUser);

            assertEquals(1, result.size());
            verify(attemptRepository).findByUser(testUser);
        }

        /**
         * Verifies played levels count.
         */
        @Test
        @DisplayName("should count played levels correctly")
        void testGetPlayedLevelsCount() {
            when(attemptRepository.countDistinctPlayedLevelsByUser(testUser)).thenReturn(5L);

            final long count = attemptService.getPlayedLevelsCount(testUser);

            assertEquals(5L, count);
            verify(attemptRepository).countDistinctPlayedLevelsByUser(testUser);
        }

        /**
         * Verifies completed levels count.
         */
        @Test
        @DisplayName("should count completed levels correctly")
        void testGetCompletedLevelsCount() {
            when(attemptRepository.countDistinctCompletedLevelsByUser(testUser)).thenReturn(3L);

            final long count = attemptService.getCompletedLevelsCount(testUser);

            assertEquals(3L, count);
            verify(attemptRepository).countDistinctCompletedLevelsByUser(testUser);
        }
    }
}
