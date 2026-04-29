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

    /** Package-private default test user ID. */
    static final String USER_ID = "user-1";
    /** Package-private default test user name. */
    static final String USER_NAME = "Mario";

    /** Package-private user ID denoting the level owner in ownership tests. */
    static final String OWNER_ID = "owner-id";
    /** Package-private user name used for the level owner in ownership tests. */
    static final String OWNER_NAME = "Mario";

    /** Package-private user ID denoting a non-owner in ownership tests. */
    static final String OTHER_ID = "other-id";
    /** Package-private user name used for non-owners in ownership tests. */
    static final String OTHER_NAME = "Luigi";

    /** Package-private user ID used for cloned levels. */
    static final String CLONE_ID = "user-2";
    /** Package-private user name used for cloned levels. */
    static final String CLONE_NAME = "Luigi";

    /** Package-private expected default level width. */
    static final int LEVEL_WIDTH = 256;
    /** Package-private expected default level height. */
    static final int LEVEL_HEIGHT = 14;

    /** Package-private GID used for start flags in tests. */
    static final int START_FLAG_GID = 68;
    /** Package-private GID used for exit doors in tests. */
    static final int EXIT_DOOR_GID = 115;

    /**
     * Utility class.
     */
    private LevelTestFixtures() {
    }

    /**
     * Package-private helper that creates a test user.
     *
     * @return a new {@link User} instance
     */
    static User createTestUser() {
        return new User(USER_ID, USER_NAME);
    }

    /**
     * Package-private helper that creates the owner user used in ownership tests.
     *
     * @return a new owner {@link User} instance
     */
    static User createOwnerUser() {
        return new User(OWNER_ID, OWNER_NAME);
    }

    /**
     * Package-private helper that creates a non-owner user used in ownership tests.
     *
     * @return a new non-owner {@link User} instance
     */
    static User createOtherUser() {
        return new User(OTHER_ID, OTHER_NAME);
    }

    /**
     * Package-private helper that creates the clone creator used in clone tests.
     *
     * @return a new clone creator {@link User} instance
     */
    static User createCloneUser() {
        return new User(CLONE_ID, CLONE_NAME);
    }

    /**
     * Package-private helper that creates a level for a specific creator.
     *
     * @param creator the user who creates the level
     * @return a new {@link Level} instance
     */
    static Level createLevelFor(final User creator) {
        return new Level("Test level", "A level description", creator);
    }

    /**
     * Package-private helper that creates a standard test level.
     *
     * @return a new {@link Level} instance
     */
    static Level createTestLevel() {
        return createLevelFor(createTestUser());
    }

    /**
     * Package-private helper that creates a start flag for the supplied position.
     *
     * @param position the desired position
     * @return a new {@link StartFlag}
     */
    static StartFlag createStartFlag(final Position position) {
        return new StartFlag(START_FLAG_GID, position);
    }

    /**
     * Package-private helper that creates an exit door for the supplied position.
     *
     * @param position the desired position
     * @return a new {@link ExitDoor}
     */
    static ExitDoor createExitDoor(final Position position) {
        return new ExitDoor(EXIT_DOOR_GID, position);
    }

    /**
     * Package-private helper that publishes a test level by adding required
     * objects and validating eligibility.
     *
     * @param level the level to publish
     */
    static void publishTestLevel(final Level level) {
        final Position flagPos = new Position(1, 1);
        final Position doorPos = new Position(2, 1);
        level.putObjectLayer(flagPos, createStartFlag(flagPos));
        level.putObjectLayer(doorPos, createExitDoor(doorPos));
        level.validatePublishEligible(USER_ID);
        level.publish(USER_ID);
    }
}
