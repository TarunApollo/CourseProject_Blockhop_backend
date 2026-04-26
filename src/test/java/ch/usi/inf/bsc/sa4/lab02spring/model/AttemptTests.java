package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for Attempt model: construction,
 * getters, and setCompleted.
 */
@DisplayName(" In the Attempt class ")
class AttemptTests {

    /** User ID used when building test fixtures. */
    private static final String USER_ID = "user-1";

    /** Username used when building test fixtures. */
    private static final String USERNAME = "Mario";

    /** Level title used when building test fixtures. */
    private static final String LEVEL_TITLE = "Test Level";

    /** Level description used when building test fixtures. */
    private static final String LEVEL_DESC = "A test level.";

    /** Fixed timestamp reused across all tests. */
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    /** Fixed duration reused across all tests. */
    private static final Duration DURATION = Duration.ofMinutes(5);

    /** ATTEMPT_ID for the six-argument constructor tests. */
    private static final String ATTEMPT_ID = "attempt-1";

    /**
     * Creates a User for testing.
     *
     * @return a new test user
     */
    private static User createTestUser() {
        return new User(USER_ID, USERNAME);
    }

    /**
     * Creates a Level for testing.
     *
     * @return a new test level
     */
    private static Level createTestLevel() {
        return new Level(LEVEL_TITLE, LEVEL_DESC, createTestUser());
    }

    /**
     * Verifies that an Attempt can be constructed and that
     * isCompleted reflects the value given at construction.
     */
    @DisplayName(" should reflect completion status given at construction ")
    @Test
    /* package */ void verifyCompletedReflectsConstructorArg() {
        final User user = createTestUser();
        final Level level = createTestLevel();
        final Attempt attempt = new Attempt(user, TIMESTAMP, level, true, DURATION);
        Assertions.assertTrue(attempt.isCompleted());
    }

    /**
     * Tests for the five-argument Attempt constructor.
     */
    @DisplayName(" when using the five-argument constructor ")
    @Nested
    /* package */ class FiveArgConstructor {

        /** The Attempt under test, created in setUp. */
        private Attempt attempt;

        /** User passed at construction; held for reference comparison. */
        private User user;

        /** Level passed at construction; held for reference comparison. */
        private Level level;

        /**
         * Creates an Attempt via the five-arg constructor
         * before each test.
         */
        @BeforeEach
        /* package */ void setUp() {
            this.user = createTestUser();
            this.level = createTestLevel();
            this.attempt = new Attempt(
                    this.user, TIMESTAMP, this.level, true, DURATION);
        }

        /**
         * Verifies that getUser returns the exact user
         * reference passed at construction.
         */
        @DisplayName(" should return the correct user ")
        @Test
        /* package */ void returnsCorrectUser() {
            Assertions.assertSame(this.user, this.attempt.getUser());
        }

        /**
         * Verifies that getTimestamp returns the timestamp
         * from construction.
         */
        @DisplayName(" should return the correct timestamp ")
        @Test
        /* package */ void returnsCorrectTimestamp() {
            Assertions.assertEquals(TIMESTAMP, this.attempt.getTimestamp());
        }

        /**
         * Verifies that getLevel returns the exact level
         * reference passed at construction.
         */
        @DisplayName(" should return the correct level ")
        @Test
        /* package */ void returnsCorrectLevel() {
            Assertions.assertSame(this.level, this.attempt.getLevel());
        }

        /**
         * Verifies that getTimeTaken returns the duration
         * from construction.
         */
        @DisplayName(" should return the correct time taken ")
        @Test
        /* package */ void returnsCorrectTimeTaken() {
            Assertions.assertEquals(DURATION, this.attempt.getTimeTaken());
        }

        /**
         * Verifies that isCompleted returns true when
         * constructed with true.
         */
        @DisplayName(" should return true for completed ")
        @Test
        /* package */ void returnsCompletedTrue() {
            Assertions.assertTrue(this.attempt.isCompleted());
        }
    }

    /**
     * Tests for the six-argument Attempt constructor,
     * which additionally accepts an ID.
     */
    @DisplayName(" when using the six-argument constructor ")
    @Nested
    /* package */ class SixArgConstructor {

        /** The Attempt under test, created in setUp. */
        private Attempt attempt;

        /**
         * Creates an Attempt via the six-argument constructor
         * before each test.
         */
        @BeforeEach
        /* package */ void setUp() {
            final User user = createTestUser();
            final Level level = createTestLevel();
            this.attempt = new Attempt(
                    ATTEMPT_ID, user, TIMESTAMP, level, false, DURATION);
        }

        /**
         * Verifies that getId returns the ID passed at construction.
         */
        @DisplayName(" should return the correct ID ")
        @Test
        /* package */ void returnsCorrectId() {
            Assertions.assertEquals(ATTEMPT_ID, this.attempt.getId());
        }

        /**
         * Verifies that isCompleted returns false when
         * constructed with false.
         */
        @DisplayName(" should return false for not completed ")
        @Test
        /* package */ void returnsCompletedFalse() {
            Assertions.assertFalse(this.attempt.isCompleted());
        }
    }

    /**
     * Tests for the setCompleted method.
     */
    @DisplayName(" method setCompleted ")
    @Nested
    /* package */ class SetCompletedMethod {

        /** The Attempt under test, created in setUp. */
        private Attempt attempt;

        /**
         * Creates an Attempt (initially not completed)
         * before each test.
         */
        @BeforeEach
        /* package */ void setUp() {
            final User user = createTestUser();
            final Level level = createTestLevel();
            this.attempt = new Attempt(user, TIMESTAMP, level, false, DURATION);
        }

        /**
         * Verifies that setCompleted(true) causes
         * isCompleted to return true.
         */
        @DisplayName(" should set completed to true ")
        @Test
        /* package */ void setsTrueCorrectly() {
            this.attempt.setCompleted(true);
            Assertions.assertTrue(this.attempt.isCompleted());
        }

        /**
         * Verifies that setCompleted(false) causes
         * isCompleted to return false.
         */
        @DisplayName(" should set completed to false ")
        @Test
        /* package */ void setsFalseCorrectly() {
            this.attempt.setCompleted(false);
            Assertions.assertFalse(this.attempt.isCompleted());
        }
    }
}
