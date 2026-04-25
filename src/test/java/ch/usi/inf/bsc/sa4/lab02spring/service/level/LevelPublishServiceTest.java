package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("LevelPublishService")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class LevelPublishServiceTest {

    private static final String LEVEL_ID = "level-1";
    private static final String OWNER_ID = "owner-1";
    private static final String OTHER_USER_ID = "other-1";

    @Mock private LevelRepository levelRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private LevelPublishService service;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(OWNER_ID, "Mario");
    }

    private Level newLevel() {
        return new Level("title", "desc", owner);
    }

    private Level publishableLevel() {
        final Level level = newLevel();
        final Position flag = new Position(1, 1);
        final Position door = new Position(2, 1);
        level.putObjectLayer(flag, new StartFlag(68, flag));
        level.putObjectLayer(door, new ExitDoor(115, door));
        return level;
    }

    // ====================================================================
    // publish
    // ====================================================================

    @Nested
    @DisplayName("publish")
    class Publish {

        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.publish(OWNER_ID, LEVEL_ID));
        }

        @Test
        @DisplayName("throws UserNotFoundException when the user does not exist")
        void userNotFound() {
            final Level level = newLevel();
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> service.publish(OWNER_ID, LEVEL_ID));
        }

        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to publish")
        void nonOwnerCannotPublish() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(userRepository.findById(OTHER_USER_ID)).thenReturn(Optional.of(new User(OTHER_USER_ID, "Luigi")));

            assertThrows(ForbiddenUserException.class,
                () -> service.publish(OTHER_USER_ID, LEVEL_ID));
            assertFalse(level.isPublished());
        }

        @Test
        @DisplayName("throws ForbiddenLevelActionException when the level is not publish-eligible")
        void notEligibleCannotPublish() {
            final Level level = publishableLevel();
            // Did NOT call validatePublishEligible — publishEligible defaults to false.
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));

            assertThrows(ForbiddenLevelActionException.class,
                () -> service.publish(OWNER_ID, LEVEL_ID));
        }

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

    @Nested
    @DisplayName("unpublishLevel")
    class Unpublish {

        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.unpublishLevel(OWNER_ID, LEVEL_ID));
        }

        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to unpublish")
        void nonOwnerCannotUnpublish() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenUserException.class,
                () -> service.unpublishLevel(OTHER_USER_ID, LEVEL_ID));
            assertTrue(level.isPublished());
        }

        @Test
        @DisplayName("marks a published level as unpublished when the owner requests it")
        void ownerUnpublishes() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.unpublishLevel(OWNER_ID, LEVEL_ID);

            assertFalse(level.isPublished());
        }

        @Test
        @DisplayName("is idempotent on an already-unpublished level")
        void unpublishingUnpublishedIsIdempotent() {
            final Level level = newLevel();
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.unpublishLevel(OWNER_ID, LEVEL_ID);

            assertFalse(level.isPublished());
        }
    }

    // ====================================================================
    // validateLevelPublishEligible
    // ====================================================================

    @Nested
    @DisplayName("validateLevelPublishEligible")
    class ValidateEligible {

        @Test
        @DisplayName("sets the publish-eligible flag and persists the level")
        void setsFlagAndSaves() {
            final Level level = newLevel();

            service.validateLevelPublishEligible(level, OWNER_ID);

            assertTrue(level.isPublishEligible());
            verify(levelRepository).save(level);
        }

        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to validate")
        void nonOwnerCannotValidate() {
            final Level level = newLevel();

            assertThrows(ForbiddenUserException.class,
                () -> service.validateLevelPublishEligible(level, OTHER_USER_ID));
            assertFalse(level.isPublishEligible());
            verify(levelRepository, never()).save(level);
        }
    }

    // ====================================================================
    // invalidateLevelPublishEligible
    // ====================================================================

    @Nested
    @DisplayName("invalidateLevelPublishEligible")
    class InvalidateEligible {

        @Test
        @DisplayName("clears the publish-eligible flag and persists the level")
        void clearsFlagAndSaves() {
            final Level level = newLevel();
            level.validatePublishEligible(OWNER_ID); // make it eligible first

            service.invalidateLevelPublishEligible(level, OWNER_ID);

            assertFalse(level.isPublishEligible());
            verify(levelRepository).save(level);
        }

        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to invalidate")
        void nonOwnerCannotInvalidate() {
            final Level level = newLevel();
            level.validatePublishEligible(OWNER_ID);

            assertThrows(ForbiddenUserException.class,
                () -> service.invalidateLevelPublishEligible(level, OTHER_USER_ID));
            assertTrue(level.isPublishEligible());
            verify(levelRepository, never()).save(level);
        }
    }
}
