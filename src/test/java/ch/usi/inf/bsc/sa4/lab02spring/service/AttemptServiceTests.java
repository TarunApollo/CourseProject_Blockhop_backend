package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateAttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the AttemptService.
 * Verifies the lifecycle and retrieval of level play attempts.
 */
@SpringBootTest
@DisplayName("The Attempt Service")
public class AttemptServiceTests {

    /** Test password for user creation. */
    private static final String TEST_PASSWORD = "Attempt Tester";

    /** Name for the test level. */
    private static final String TEST_LEVEL_NAME = "Test Level";

    /** Description for the test level. */
    private static final String TEST_LEVEL_DESC = "Description";

    @Autowired
    private AttemptService attemptService;

    @Autowired
    private UserService userService;

    @Autowired
    private LevelService levelService;

    private User testUser;
    private Level testLevel;

    /**
     * Initializes a test user and a test level before each test.
     */
    @BeforeEach
    void setup() {
        var userDTO = new CreateUserDTO("tester-" + System.currentTimeMillis(), TEST_PASSWORD);
        testUser = userService.createUser(userDTO);

        var levelDTO = new CreateLevelDTO(TEST_LEVEL_NAME, TEST_LEVEL_DESC);
        testLevel = levelService.createLevel(levelDTO, testUser.getId());
    }

    /**
     * Tests for attempt creation functionality.
     */
    @Nested
    @DisplayName("when creating an attempt")
    class Creation {

        /**
         * Verifies that a created attempt has a non-null ID.
         */
        @Test
        @DisplayName("the attempt should have a generated ID")
        public void testCreateAttemptHasId() {
            var createDTO = new CreateAttemptDTO(testLevel.getId(), true, Duration.ofSeconds(120));
            Attempt attempt = attemptService.createAttempt(testUser, createDTO);
            assertNotNull(attempt.getId());
        }

        /**
         * Verifies that a created attempt is associated with the correct user.
         */
        @Test
        @DisplayName("the attempt should be associated with the correct user")
        public void testCreateAttemptHasCorrectUser() {
            var createDTO = new CreateAttemptDTO(testLevel.getId(), true, Duration.ofSeconds(120));
            Attempt attempt = attemptService.createAttempt(testUser, createDTO);
            assertEquals(testUser.getId(), attempt.getUser().getId());
        }

        /**
         * Verifies that a created attempt is associated with the correct level.
         */
        @Test
        @DisplayName("the attempt should be associated with the correct level")
        public void testCreateAttemptHasCorrectLevel() {
            var createDTO = new CreateAttemptDTO(testLevel.getId(), true, Duration.ofSeconds(120));
            Attempt attempt = attemptService.createAttempt(testUser, createDTO);
            assertEquals(testLevel.getId(), attempt.getLevel().getId());
        }
    }

    /**
     * Tests for attempt retrieval and statistics.
     */
    @Nested
    @DisplayName("when retrieving attempts")
    class Retrieval {

        /**
         * Prepares test data by creating an attempt before each retrieval test.
         */
        @BeforeEach
        void setup() {
            var createDTO = new CreateAttemptDTO(testLevel.getId(), true, Duration.ofSeconds(50));
            attemptService.createAttempt(testUser, createDTO);
        }

        /**
         * Verifies that attempts can be retrieved for a specific user.
         */
        @Test
        @DisplayName("should retrieve attempts by user")
        public void testGetAttemptsByUser() {
            List<Attempt> attempts = attemptService.getAttemptsByUser(testUser);
            boolean exists = attempts.stream().anyMatch(a -> a.getLevel().getId().equals(testLevel.getId()));
            assertTrue(exists);
        }

        /**
         * Verifies that the count of played levels is accurate.
         */
        @Test
        @DisplayName("should count played levels correctly")
        public void testGetPlayedLevelsCount() {
            long count = attemptService.getPlayedLevelsCount(testUser);
            assertEquals(1, count);
        }
    }
}
