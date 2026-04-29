package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/// Unit tests for [AttemptService].
@SpringBootTest
@DisplayName("The Attempt Service")
class AttemptServiceTests {

    /// Default attempt DTO for testing attempts.
    private static final AttemptDTO ATTEMPT_DTO = new AttemptDTO(
            Map.of(), new Position(0, 0),
            ZonedDateTime.now(), Duration.ofSeconds(15), false);

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

    /// Sets up test data before each test.
    @BeforeEach
    void setup() {
        this.testUser = new User("user-1", "Mario");
        this.testLevel = new Level("Test Level", "desc", testUser);

        this.expectedAttempt = new Attempt(
                this.testUser,
                ATTEMPT_DTO.timestamp(),
                this.testLevel,
                ATTEMPT_DTO.completed(),
                ATTEMPT_DTO.timeTaken());
    }

    /// Tests related to retrieving statistics.
    @Nested
    @DisplayName("when retrieving statistics")
    class Stats {

        /// Verifies delegation to the repository for counting played levels.
        @Test
        @DisplayName("should count played levels correctly")
        void testGetPlayedLevelsCount() {
            when(attemptRepository.countDistinctPlayedLevelsByUser(testUser)).thenReturn(5L);

            final long count = attemptService.getPlayedLevelsCount(testUser);
            Assertions.assertEquals(5L, count);
        }

        /// Verifies delegation to the repository for counting completed levels.
        @Test
        @DisplayName("should count completed levels correctly")
        void testGetCompletedLevelsCount() {
            when(attemptRepository.countDistinctCompletedLevelsByUser(testUser)).thenReturn(3L);

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

            verify(attemptRepository).save(refEq(expectedAttempt));
        }
    }
}
