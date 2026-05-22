package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

/// Shared fixtures and helpers for split [Level] tests.
final class LevelTestFixtures {

    /// Default test user ID.
    /* default */ static final String USER_ID = "user-1";
    /// Default test user name.
    /* default */ static final String USER_NAME = "Mario";

    /// User ID denoting the level owner in ownership tests.
    /* default */ static final String OWNER_ID = "owner-id";
    /// User name used for the level owner in ownership tests.
    /* default */ static final String OWNER_NAME = "Mario";

    /// User ID denoting a non-owner in ownership tests.
    /* default */ static final String OTHER_ID = "other-id";
    /// User name used for non-owners in ownership tests.
    /* default */ static final String OTHER_NAME = "Luigi";

    /// User ID used for cloned levels.
    /* default */ static final String CLONE_ID = "user-2";
    /// User name used for cloned levels.
    /* default */ static final String CLONE_NAME = "Luigi";

    /// Expected default level width.
    /* default */ static final int LEVEL_WIDTH = 256;
    /// Expected default level height.
    /* default */ static final int LEVEL_HEIGHT = 14;

    /// GID used for start flags in tests.
    /* default */ static final String START_FLAG_TILE_ID = "flag.green";
    /// GID used for exit doors in tests.
    /* default */ static final String EXIT_DOOR_TILE_ID = "door.closed.bottom";

    /// Utility class.
    private LevelTestFixtures() {
    }

    /// Creates a test user.
    ///
    /// @return a new [User] instance
    /* default */ static User createTestUser() {
        return new User(USER_ID, USER_NAME);
    }

    /// Creates the owner user used in ownership tests.
    ///
    /// @return a new owner [User] instance
    /* default */ static User createOwnerUser() {
        return new User(OWNER_ID, OWNER_NAME);
    }

    /// Creates a non-owner user used in ownership tests.
    ///
    /// @return a new non-owner [User] instance
    /* default */ static User createOtherUser() {
        return new User(OTHER_ID, OTHER_NAME);
    }

    /// Creates the clone creator used in clone tests.
    ///
    /// @return a new clone creator [User] instance
    /* default */ static User createCloneUser() {
        return new User(CLONE_ID, CLONE_NAME);
    }

    /// Creates a level for a specific creator.
    ///
    /// @param creator the user who creates the level
    /// @return a new [Level] instance
    /* default */ static Level createLevelFor(final User creator) {
        return new Level("Test level", "A level description", creator);
    }

    /// Creates a standard test level.
    ///
    /// @return a new [Level] instance
    /* default */ static Level createTestLevel() {
        return createLevelFor(createTestUser());
    }

    /// Creates a start flag for the supplied position.
    ///
    /// @param position the desired position
    /// @return a new [StartFlag]
    /* default */ static StartFlag createStartFlag(final Position position) {
        return new StartFlag(START_FLAG_TILE_ID, position);
    }

    /// Creates an exit door for the supplied position.
    ///
    /// @param position the desired position
    /// @return a new [ExitDoor]
    /* default */ static ExitDoor createExitDoor(final Position position) {
        return new ExitDoor(EXIT_DOOR_TILE_ID, position);
    }

    /// Publishes a test level by adding required objects and validating
    /// eligibility.
    ///
    /// @param level the level to publish
    /* default */ static void publishTestLevel(final Level level) {
        final Position flagPos = new Position(1, 1);
        final Position doorPos = new Position(2, 1);
        level.putObjectLayer(flagPos, createStartFlag(flagPos));
        level.putObjectLayer(doorPos, createExitDoor(doorPos));
        level.validatePublishEligible(USER_ID);
        level.publish(USER_ID);
    }
}
