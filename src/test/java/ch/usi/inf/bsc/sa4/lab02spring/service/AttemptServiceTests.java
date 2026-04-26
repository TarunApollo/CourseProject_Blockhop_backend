package ch.usi.inf.bsc.sa4.lab02spring.service;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.TestSecurityConfig;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateAttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateUserDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Integration tests for AttemptService.
 * Uses a real Spring context with embedded MongoDB.
 * All assertions go through the service; repositories are only used
 * for setup cleanup.
 */
/**
 * Integration tests for AttemptService.
 * Uses a real Spring context with embedded MongoDB.
 * All assertions go through the service; repositories are only used
 * for setup cleanup.
 *
 * <p>Bean definition overriding is enabled because
 * {@link TestSecurityConfig} must replace the production
 * {@code SecurityConfiguration}'s filter chain bean. Without this
 * property Spring Boot rejects two beans with the same name.</p>
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
@Import(TestSecurityConfig.class)
@DisplayName("The Attempt Service")
@SuppressWarnings("NullAway")
/* package */ class AttemptServiceTests {

    /** The service under test, wired by Spring. */
    @Autowired
    private AttemptService attemptService;

    /** Used to create the test level through the service layer. */
    @Autowired
    private LevelService levelService;

    /** Used to persist the test user via the service layer. */
    @Autowired
    private UserService userService;

    /** Used only for setup cleanup between tests. */
    @Autowired
    private AttemptRepository attemptRepository;

    /** Used only for setup cleanup between tests. */
    @Autowired
    private LevelRepository levelRepository;

    /** Used only for setup cleanup between tests. */
    @Autowired
    private UserRepository userRepository;

    /** The test user. */
    private User testUser;

    /** A test level created via LevelService. */
    private Level testLevel;

    /** A fixed duration used across tests. */
    private static final Duration TEST_DURATION = Duration.ofSeconds(30);

    /** Clears all data and creates fresh user and level via services. */
    @BeforeEach
    /* package */ void setup() {
        attemptRepository.deleteAll();
        levelRepository.deleteAll();
        userRepository.deleteAll();
        final CreateUserDTO userDto =
                new CreateUserDTO("user-1", "Mario");
        this.testUser = userService.createUser(userDto);
        final CreateLevelDTO levelDto = new CreateLevelDTO(
                "Test Level", "A description");
        this.testLevel = levelService.createLevel(levelDto, testUser.getId());
    }

    /** Tests for the createAttempt method. */
    @Nested
    @DisplayName("after creating a new attempt")
    /* default */ class AfterCreatingANewAttempt {

        private Attempt newAttempt;

        /** Creates a new attempt before each test. */
        @BeforeEach
        /* package */ void setup() {
            final CreateAttemptDTO dto =
                    new CreateAttemptDTO(testLevel.getId(), true, TEST_DURATION);
            this.newAttempt = attemptService.createAttempt(testUser, dto);
        }

        /** Verifies the returned attempt is not null. */
        @DisplayName("the attempt should be non null")
        @Test
        /* package */ void testNotNull() {
            Assertions.assertNotNull(this.newAttempt);
        }

        /** Verifies the attempt is assigned the correct level. */
        @DisplayName("the attempt should reference the correct level")
        @Test
        /* package */ void testLevel() {
            Assertions.assertEquals(testLevel.getId(),
                    this.newAttempt.getLevel().getId());
        }

        /** Verifies the attempt can be retrieved via the service. */
        @DisplayName("the attempt should be retrievable by id and user")
        @Test
        /* package */ void testRetrievable() {
            final Optional<Attempt> found =
                    attemptService.getAttemptByIdAndUser(
                            newAttempt.getId(), testUser);
            Assertions.assertTrue(found.isPresent());
        }
    }

    /** Tests for LevelNotFoundException in createAttempt. */
    @Nested
    @DisplayName("when creating an attempt for a non-existent level")
    /* default */ class CreateAttemptLevelNotFound {

        /** Verifies that LevelNotFoundException is thrown. */
        @DisplayName("should throw LevelNotFoundException")
        @Test
        /* package */ void testThrows() {
            final CreateAttemptDTO dto =
                    new CreateAttemptDTO("non-existent-id", true, TEST_DURATION);
            final Executable call =
                    () -> attemptService.createAttempt(testUser, dto);
            Assertions.assertThrows(LevelNotFoundException.class, call);
        }
    }

    /** Tests for the getAttemptsByUser method. */
    @Nested
    @DisplayName("when retrieving attempts by user")
    /* default */ class GetAttemptsByUser {

        private Attempt attempt;

        /** Creates an attempt before each test. */
        @BeforeEach
        /* package */ void setup() {
            final CreateAttemptDTO dto =
                    new CreateAttemptDTO(testLevel.getId(), true, TEST_DURATION);
            this.attempt = attemptService.createAttempt(testUser, dto);
        }

        /** Verifies the user's attempts are returned. */
        @DisplayName("should return the user's attempts")
        @Test
        /* package */ void testReturnsAttempts() {
            final List<Attempt> result =
                    attemptService.getAttemptsByUser(testUser);
            Assertions.assertEquals(1, result.size());
        }

        /** Verifies the returned attempt matches the created one. */
        @DisplayName("should return the correct attempt")
        @Test
        /* package */ void testCorrectAttempt() {
            final List<Attempt> result =
                    attemptService.getAttemptsByUser(testUser);
            Assertions.assertEquals(
                    attempt.getId(), result.getFirst().getId());
        }
    }

    /** Tests for the getPlayedLevelsCount method. */
    @Nested
    @DisplayName("when counting played levels")
    /* default */ class PlayedLevelsCount {

        /** Verifies the count is zero before any attempts. */
        @DisplayName("should return 0 when no attempts exist")
        @Test
        /* package */ void testZeroInitially() {
            Assertions.assertEquals(0L,
                    attemptService.getPlayedLevelsCount(testUser));
        }

        /** Verifies the count increases after an attempt. */
        @DisplayName("should return 1 after one attempt")
        @Test
        /* package */ void testAfterOneAttempt() {
            final CreateAttemptDTO dto =
                    new CreateAttemptDTO(testLevel.getId(), true, TEST_DURATION);
            attemptService.createAttempt(testUser, dto);
            Assertions.assertEquals(1L,
                    attemptService.getPlayedLevelsCount(testUser));
        }
    }

    /** Tests for the getCompletedLevelsCount method. */
    @Nested
    @DisplayName("when counting completed levels")
    /* default */ class CompletedLevelsCount {

        /** Verifies the count is zero before any attempts. */
        @DisplayName("should return 0 when no attempts exist")
        @Test
        /* package */ void testZeroInitially() {
            Assertions.assertEquals(0L,
                    attemptService.getCompletedLevelsCount(testUser));
        }

        /** Verifies the count increases after a completed attempt. */
        @DisplayName("should return 1 after a completed attempt")
        @Test
        /* package */ void testAfterCompletedAttempt() {
            final CreateAttemptDTO dto =
                    new CreateAttemptDTO(testLevel.getId(), true, TEST_DURATION);
            attemptService.createAttempt(testUser, dto);
            Assertions.assertEquals(1L,
                    attemptService.getCompletedLevelsCount(testUser));
        }
    }

    /** Tests for the setAttemptUncompleted method. */
    @Nested
    @DisplayName("after marking an attempt as uncompleted")
    /* default */ class SetUncompleted {

        /** Creates a completed attempt before each test. */
        @BeforeEach
        /* package */ void setup() {
            final CreateAttemptDTO dto =
                    new CreateAttemptDTO(testLevel.getId(), true, TEST_DURATION);
            attemptService.createAttempt(testUser, dto);
        }

        /** Verifies the attempt is marked as uncompleted. */
        @DisplayName("should mark the attempt as uncompleted")
        @Test
        /* package */ void testMarkedUncompleted() {
            attemptService.setAttemptUncompleted(testUser, testLevel);
            Assertions.assertFalse(
                    attemptService.hasCompleted(testUser, testLevel));
        }
    }

    /** Tests for the hasCompleted method. */
    @Nested
    @DisplayName("when checking completion status")
    /* default */ class HasCompleted {

        /** Verifies false when no attempts exist. */
        @DisplayName("should return false when no attempts exist")
        @Test
        /* package */ void testNoAttempts() {
            Assertions.assertFalse(
                    attemptService.hasCompleted(testUser, testLevel));
        }

        /** Verifies true after a completed attempt. */
        @DisplayName("should return true after a completed attempt")
        @Test
        /* package */ void testAfterCompleted() {
            final CreateAttemptDTO dto =
                    new CreateAttemptDTO(testLevel.getId(), true, TEST_DURATION);
            attemptService.createAttempt(testUser, dto);
            Assertions.assertTrue(
                    attemptService.hasCompleted(testUser, testLevel));
        }
    }

    /** Tests for the submitAttempt method. */
    @Nested
    @DisplayName("after submitting an attempt")
    /* default */ class SubmitAttempt {

        /** Verifies the attempt is retrievable after submission. */
        @DisplayName("should create a retrievable attempt")
        @Test
        /* package */ void testRetrievableAfterSubmit() {
            final AttemptDTO dto = new AttemptDTO(
                    Map.of(), new Position(0, 0),
                    ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0,
                            java.time.ZoneOffset.UTC),
                    TEST_DURATION, true);
            attemptService.submitAttempt(testUser, testLevel, dto);
            final List<Attempt> result =
                    attemptService.getAttemptsByUser(testUser);
            Assertions.assertEquals(1, result.size());
        }
    }

    /** Tests for the getAttemptByIdAndUser method. */
    @Nested
    @DisplayName("when retrieving an attempt by id and user")
    /* default */ class GetByIdAndUser {

        /** Verifies the correct attempt is returned. */
        @DisplayName("should return the correct attempt")
        @Test
        /* package */ void testCorrectAttempt() {
            final CreateAttemptDTO dto =
                    new CreateAttemptDTO(testLevel.getId(), true, TEST_DURATION);
            final Attempt created =
                    attemptService.createAttempt(testUser, dto);
            final Optional<Attempt> found =
                    attemptService.getAttemptByIdAndUser(
                            created.getId(), testUser);
            Assertions.assertTrue(found.isPresent());
        }
    }
}
