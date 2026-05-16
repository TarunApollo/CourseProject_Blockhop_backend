package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttitudeRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelFavoriteRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.UserRepository;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for the level publish service.
@DisplayName("LevelPublishService")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "PMD.TooManyStaticImports"})
class LevelPublishServiceTest {

    /// Identifier of the level under test.
    private static final String LEVEL_ID = "level-1";
    /// Identifier of the level owner used by fixtures.
    private static final String OWNER_ID = "owner-1";
    /// Identifier of a non-owner user used by fixtures.
    private static final String OTHER_USER_ID = "other-1";
    /// Display name of the owner user.
    private static final String OWNER_NAME = "Mario";
    /// Display name of the non-owner user.
    private static final String OTHER_NAME = "Luigi";
    /// Default level title used by fixtures.
    private static final String LEVEL_TITLE = "title";
    /// Default level description used by fixtures.
    private static final String LEVEL_DESC = "desc";

    /// Mocked level repository providing per-test fixtures.
    @Mock private LevelRepository levelRepository;
    /// Mocked favorite repository for cleaning favorites of unpublished levels.
    @Mock private LevelFavoriteRepository levelFavoriteRepository;
    /// Mocked attitude repository for cleaning attitudes of unpublished levels.
    @Mock private AttitudeRepository attitudeRepository;
    /// Mocked user repository for verifying user existence.
    @Mock private UserRepository userRepository;

    /// Service under test, with mocks injected.
    @InjectMocks private LevelPublishService service;

    /// Shared owner user fixture.
    private User owner;

    /// Initializes the shared owner fixture before each test.
    @BeforeEach
    void setUp() {
        owner = new User(OWNER_ID, OWNER_NAME);
    }

    /// Builds a basic level owned by the configured owner.
    private Level newLevel() {
        return new Level(LEVEL_TITLE, LEVEL_DESC, owner);
    }

    /// Builds a level with one start flag and one exit door so publish() can succeed.
    private Level publishableLevel() {
        final Level level = newLevel();
        final Position flag = new Position(1, 1);
        final Position door = new Position(2, 1);
        level.putObjectLayer(flag, new StartFlag(68, flag));
        level.putObjectLayer(door, new ExitDoor(115, door));
        return level;
    }

    /// Sanity test so static analyzers see at least one top-level @Test on the class.
    @Test
    @DisplayName("test fixture initializes the service")
    void serviceWired() {
        assertNotNull(service);
    }

    // ====================================================================
    // publish
    // ====================================================================

    /// Tests for the publish entry point.
    @Nested
    @DisplayName("publish")
    class Publish {

        /// Missing level should surface as LevelNotFoundException.
        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.publish(OWNER_ID, LEVEL_ID));
        }

        /// Missing user should surface as UserNotFoundException.
        @Test
        @DisplayName("throws UserNotFoundException when the user does not exist")
        void userNotFound() {
            final Level level = newLevel();
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> service.publish(OWNER_ID, LEVEL_ID));
        }

        /// Non-owner cannot publish.
        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to publish")
        void nonOwnerCannotPublish() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(userRepository.findById(OTHER_USER_ID))
                .thenReturn(Optional.of(new User(OTHER_USER_ID, OTHER_NAME)));

            assertThrows(ForbiddenUserException.class,
                () -> service.publish(OTHER_USER_ID, LEVEL_ID));
        }

        /// Publishing a level that is not eligible should be rejected.
        @Test
        @DisplayName("throws ForbiddenLevelActionException when the level is not publish-eligible")
        void notEligibleCannotPublish() {
            final Level level = publishableLevel();
            // Did NOT call validatePublishEligible → publishEligible defaults to false.
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));

            assertThrows(ForbiddenLevelActionException.class,
                () -> service.publish(OWNER_ID, LEVEL_ID));
        }

        /// Owner publishing an eligible level marks it as published.
        @Test
        @DisplayName("marks the level as published when the owner publishes an eligible level")
        void ownerPublishesEligibleLevel() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));

            service.publish(OWNER_ID, LEVEL_ID);

            assertTrue(level.isPublished());
        }
    }

    // ====================================================================
    // unpublishLevel
    // ====================================================================

    /// Tests for the unpublishLevel entry point.
    @Nested
    @DisplayName("unpublishLevel")
    class Unpublish {

        /// Missing level should surface as LevelNotFoundException.
        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.unpublishLevel(OWNER_ID, LEVEL_ID));
        }

        /// Non-owner cannot unpublish a level.
        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to unpublish")
        void nonOwnerCannotUnpublish() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenUserException.class,
                () -> service.unpublishLevel(OTHER_USER_ID, LEVEL_ID));
        }

        /// Owner can unpublish their own published level.
        @Test
        @DisplayName("marks a published level as unpublished when the owner requests it")
        void ownerUnpublishes() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.unpublishLevel(OWNER_ID, LEVEL_ID);

            assertFalse(level.isPublished());
            verify(levelRepository).save(level);
            verify(levelFavoriteRepository).deleteByLevelId(LEVEL_ID);
            verify(attitudeRepository).deleteByLevelId(LEVEL_ID);
        }

        /// Unpublishing an already-unpublished level is a no-op.
        @Test
        @DisplayName("is idempotent on an already-unpublished level")
        void unpublishingUnpublishedIsIdempotent() {
            final Level level = newLevel();
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.unpublishLevel(OWNER_ID, LEVEL_ID);

            assertFalse(level.isPublished());
            verify(levelRepository).save(level);
            verify(levelFavoriteRepository).deleteByLevelId(LEVEL_ID);
            verify(attitudeRepository).deleteByLevelId(LEVEL_ID);
        }
    }

    // ====================================================================
    // validateLevelPublishEligible
    // ====================================================================

    /// Tests for the validateLevelPublishEligible entry point.
    @Nested
    @DisplayName("validateLevelPublishEligible")
    class ValidateEligible {

        /// Owner request should set the publish-eligible flag to true.
        @Test
        @DisplayName("sets the publish-eligible flag for the owner")
        void setsFlag() {
            final Level level = newLevel();

            service.validateLevelPublishEligible(level, OWNER_ID);

            assertTrue(level.isPublishEligible());
        }

        /// Owner request should also persist the level.
        @Test
        @DisplayName("persists the level after marking it eligible")
        void persistsLevel() {
            final Level level = newLevel();

            service.validateLevelPublishEligible(level, OWNER_ID);

            verify(levelRepository).save(level);
        }

        /// Non-owner cannot mark a level as eligible.
        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to validate")
        void nonOwnerCannotValidate() {
            final Level level = newLevel();

            assertThrows(ForbiddenUserException.class,
                () -> service.validateLevelPublishEligible(level, OTHER_USER_ID));
        }

        /// Non-owner failure must not save anything.
        @Test
        @DisplayName("does not persist the level when a non-owner attempt fails")
        void nonOwnerFailureSkipsSave() {
            final Level level = newLevel();

            assertThrows(ForbiddenUserException.class,
                () -> service.validateLevelPublishEligible(level, OTHER_USER_ID));
            verify(levelRepository, never()).save(level);
        }
    }

    // ====================================================================
    // invalidateLevelPublishEligible
    // ====================================================================

    /// Tests for the invalidateLevelPublishEligible entry point.
    @Nested
    @DisplayName("invalidateLevelPublishEligible")
    class InvalidateEligible {

        /// Owner request should clear the publish-eligible flag.
        @Test
        @DisplayName("clears the publish-eligible flag for the owner")
        void clearsFlag() {
            final Level level = newLevel();
            level.validatePublishEligible(OWNER_ID);

            service.invalidateLevelPublishEligible(level, OWNER_ID);

            assertFalse(level.isPublishEligible());
        }

        /// Owner request should persist the level.
        @Test
        @DisplayName("persists the level after clearing the flag")
        void persistsLevel() {
            final Level level = newLevel();
            level.validatePublishEligible(OWNER_ID);

            service.invalidateLevelPublishEligible(level, OWNER_ID);

            verify(levelRepository).save(level);
        }

        /// Non-owner cannot clear the publish-eligible flag.
        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to invalidate")
        void nonOwnerCannotInvalidate() {
            final Level level = newLevel();
            level.validatePublishEligible(OWNER_ID);

            assertThrows(ForbiddenUserException.class,
                () -> service.invalidateLevelPublishEligible(level, OTHER_USER_ID));
        }


        /// Non-owner failure must not save anything.
        @Test
        @DisplayName("does not persist the level when a non-owner attempt fails")
        void nonOwnerFailureSkipsSave() {
            final Level level = newLevel();
            level.validatePublishEligible(OWNER_ID);

            assertThrows(ForbiddenUserException.class,
                () -> service.invalidateLevelPublishEligible(level, OTHER_USER_ID));
            verify(levelRepository, never()).save(level);
        }
    }
}
