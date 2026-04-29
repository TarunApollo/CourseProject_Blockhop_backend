package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

/**
 * Shared fixtures and helpers for split {@link Level} tests.
 */
final class LevelTestFixtures {

    /** Default test user ID. */
    static final String USER_ID = "user-1";

    /** User ID denoting the level owner in ownership tests. */
    static final String OWNER_ID = "owner-id";

    /** User ID denoting a non-owner in ownership tests. */
    static final String OTHER_ID = "other-id";

    /**
     * Utility class.
     */
    private LevelTestFixtures() {
    }

    /**
     * Creates a test user.
     *
     * @return a new {@link User} instance
     */
    static User createTestUser() {
        return new User(USER_ID, "Mario");
    }

    /**
     * Creates a level for a specific creator.
     *
     * @param creator the user who creates the level
     * @return a new {@link Level} instance
     */
    static Level createLevelFor(final User creator) {
        return new Level("Test level", "A level description", creator);
    }

    /**
     * Creates a standard test level.
     *
     * @return a new {@link Level} instance
     */
    static Level createTestLevel() {
        return createLevelFor(createTestUser());
    }

    /**
     * Publishes a test level by adding required objects and validating
     * eligibility.
     *
     * @param level the level to publish
     */
    static void publishTestLevel(final Level level) {
        final Position flagPos = new Position(1, 1);
        final Position doorPos = new Position(2, 1);
        level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
        level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
        level.validatePublishEligible(USER_ID);
        level.publish(USER_ID);
    }
}
