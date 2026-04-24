package ch.usi.inf.bsc.sa4.lab02spring.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for Attempt model: construction,
 * getters, and setCompleted.
 */
/* package */ class AttemptTests {

    /** User ID used when building test fixtures. */
    private static final String USER_ID = "user-1";

    /** Username used when building test fixtures. */
    private static final String USERNAME = "Mario";

    /** Level title used when building test fixtures. */
    private static final String LEVEL_TITLE = "Test Level";

    /** Level description used when building test fixtures. */
    private static final String LEVEL_DESC = "A test level.";

    /** Fixed timestamp reused across all tests. */
    private static final ZonedDateTime TIMESTAMP =
            ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    /** Fixed duration reused across all tests. */
    private static final Duration DURATION = Duration.ofMinutes(5);

    /** Attempt ID for the six-argument constructor tests. */
    private static final String ATTEMPT_ID = "attempt-1";

    /**
     * Instantiates a new AttemptTests.
     */
    /* package */ AttemptTests() {
        // Required by pmd:AtLeastOneConstructor
    }

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
     * getCompleted reflects the value given at construction.
     */
    @Test
    /* package */ void verifyCompletedReflectsConstructorArg() {
        final User user = createTestUser();
        final Level level = createTestLevel();
        final Attempt attempt =
                new Attempt(user, TIMESTAMP, level, true, DURATION);
        assertTrue(attempt.getCompleted());
    }

    /**
     * Tests for the five-argument Attempt constructor.
     */
    @Nested
    /* package */ class FiveArgConstructor {

        /** The Attempt under test, created in setUp. */
        private Attempt attempt;

        /** User passed at construction; held for reference comparison. */
        private User user;

        /** Level passed at construction; held for reference comparison. */
        private Level level;

        /**
         * Instantiates a new FiveArgConstructor.
         */
        /* package */ FiveArgConstructor() {
            // Required by pmd:AtLeastOneConstructor
        }

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
        @Test
        /* package */ void returnsCorrectUser() {
            assertSame(this.user, this.attempt.getUser());
        }

        /**
         * Verifies that getTimestamp returns the timestamp
         * from construction.
         */
        @Test
        /* package */ void returnsCorrectTimestamp() {
            assertEquals(TIMESTAMP, this.attempt.getTimestamp());
        }

        /**
         * Verifies that getLevel returns the exact level
         * reference passed at construction.
         */
        @Test
        /* package */ void returnsCorrectLevel() {
            assertSame(this.level, this.attempt.getLevel());
        }

        /**
         * Verifies that getTimeTaken returns the duration
         * from construction.
         */
        @Test
        /* package */ void returnsCorrectTimeTaken() {
            assertEquals(DURATION, this.attempt.getTimeTaken());
        }

        /**
         * Verifies that getCompleted returns true when
         * constructed with true.
         */
        @Test
        /* package */ void returnsCompletedTrue() {
            assertTrue(this.attempt.getCompleted());
        }
    }

    /**
     * Tests for the six-argument Attempt constructor,
     * which additionally accepts an ID.
     */
    @Nested
    /* package */ class SixArgConstructor {

        /** The Attempt under test, created in setUp. */
        private Attempt attempt;

        /**
         * Instantiates a new SixArgConstructor.
         */
        /* package */ SixArgConstructor() {
            // Required by pmd:AtLeastOneConstructor
        }

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
        @Test
        /* package */ void returnsCorrectId() {
            assertEquals(ATTEMPT_ID, this.attempt.getId());
        }

        /**
         * Verifies that getCompleted returns false when
         * constructed with false.
         */
        @Test
        /* package */ void returnsCompletedFalse() {
            assertFalse(this.attempt.getCompleted());
        }
    }

    /**
     * Tests for the setCompleted method.
     */
    @Nested
    /* package */ class SetCompletedMethod {

        /** The Attempt under test, created in setUp. */
        private Attempt attempt;

        /**
         * Instantiates a new SetCompletedMethod.
         */
        /* package */ SetCompletedMethod() {
            // Required by pmd:AtLeastOneConstructor
        }

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
         * getCompleted to return true.
         */
        @Test
        /* package */ void setsTrueCorrectly() {
            this.attempt.setCompleted(true);
            assertTrue(this.attempt.getCompleted());
        }

        /**
         * Verifies that setCompleted(false) causes
         * getCompleted to return false.
         */
        @Test
        /* package */ void setsFalseCorrectly() {
            this.attempt.setCompleted(false);
            assertFalse(this.attempt.getCompleted());
        }
    }
}
