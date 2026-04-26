package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;

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

    /** The service under test. */
    @InjectMocks
    private AttemptService attemptService;

    /** The test user. */
    private User testUser;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    /* default */ void setup() {
        this.testUser = new User("user-1", "Mario");
    }

    /**
     * Tests for retrieval and statistics methods.
     */
    @Nested
    @DisplayName("when retrieving attempts and stats")
    /* default */ class Stats {

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