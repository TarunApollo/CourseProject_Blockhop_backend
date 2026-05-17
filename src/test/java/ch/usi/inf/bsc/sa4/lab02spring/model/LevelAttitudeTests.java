package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/// Tests for LevelAttitude model: construction and getters.
///
@DisplayName(" In the LevelAttitude class ")
class LevelAttitudeTests {

    /// User ID used when building test fixtures.
    private static final String USER_ID = "user-1";

    /// Username used when building test fixtures.
    private static final String USERNAME = "Mario";

    /// Level title used when building test fixtures.
    private static final String LEVEL_TITLE = "Test Level";

    /// Level description used when building test fixtures.
    private static final String LEVEL_DESC = "A test level.";

    /// Level attitude ID used for the explicit-id constructor tests.
    private static final String ATTITUDE_ID = "attitude-1";

    /// Creates a User for testing.
    private static User createTestUser() {
        return new User(USER_ID, USERNAME);
    }

    /// Creates a Level for testing.
    private static Level createTestLevel() {
        return new Level(LEVEL_TITLE, LEVEL_DESC, createTestUser());
    }

    /// Verifies that an attitude can be constructed and read back.
    @DisplayName(" should reflect the attitude given at construction ")
    @Test
    /* package */ void verifyAttitudeReflectsConstructorArg() {
        final User user = createTestUser();
        final Level level = createTestLevel();
        final LevelAttitude attitude = new LevelAttitude(user, level, LevelAttitudeType.LIKE);
        Assertions.assertEquals(LevelAttitudeType.LIKE, attitude.getAttitude());
    }

    /// Tests for the three-argument LevelAttitude constructor.
    @DisplayName(" when using the three-argument constructor ")
    @Nested
    /* package */ class ThreeArgConstructor {

        /// The LevelAttitude under test, created in setUp.
        private LevelAttitude attitude;

        /// User passed at construction; held for reference comparison.
        private User user;

        /// Level passed at construction; held for reference comparison.
        private Level level;

        /// Creates a LevelAttitude via the three-arg constructor before each test.
        @BeforeEach
        /* package */ void setUp() {
            this.user = createTestUser();
            this.level = createTestLevel();
            this.attitude = new LevelAttitude(this.user, this.level, LevelAttitudeType.DISLIKE);
        }

        /// Verifies that getUser returns the exact user reference passed
        /// at construction.
        ///
        @DisplayName(" should return the correct user ")
        @Test
        /* package */ void returnsCorrectUser() {
            Assertions.assertSame(this.user, this.attitude.getUser());
        }

        /// Verifies that getLevel returns the exact level reference passed
        /// at construction.
        ///
        @DisplayName(" should return the correct level ")
        @Test
        /* package */ void returnsCorrectLevel() {
            Assertions.assertSame(this.level, this.attitude.getLevel());
        }

        /// Verifies that getAttitude returns the value from construction.
        @DisplayName(" should return the correct attitude ")
        @Test
        /* package */ void returnsCorrectAttitude() {
            Assertions.assertEquals(LevelAttitudeType.DISLIKE, this.attitude.getAttitude());
        }
    }

    /// Tests for the four-argument LevelAttitude constructor, which additionally
    /// accepts an ID.
    ///
    @DisplayName(" when using the four-argument constructor ")
    @Nested
    /* package */ class FourArgConstructor {

        /// The LevelAttitude under test, created in setUp.
        private LevelAttitude attitude;

        /// Creates a LevelAttitude via the four-argument constructor before each test.
        ///
        @BeforeEach
        /* package */ void setUp() {
            final User user = createTestUser();
            final Level level = createTestLevel();
            this.attitude = new LevelAttitude(ATTITUDE_ID, user, level, LevelAttitudeType.LIKE);
        }

        /// Verifies that getId returns the ID passed at construction.
        @DisplayName(" should return the correct ID ")
        @Test
        /* package */ void returnsCorrectId() {
            Assertions.assertEquals(ATTITUDE_ID, this.attitude.getId());
        }
    }
}