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

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for the LevelPublishService.
@SpringBootTest
@DisplayName("The Level Publish Service")
@SuppressWarnings({ "NullAway", "PMD.TooManyStaticImports" })
class LevelPublishServiceTest {

    /// ID of the level used for testing.
    private static final String LEVEL_ID = "level-1";

    /// ID of the user who owns the level in the tests.
    private static final String OWNER_ID = "owner-1";

    /// ID of a user who does not own the level in the tests.
    private static final String OTHER_USER_ID = "other-1";

    /// Name of the owner user.
    private static final String OWNER_NAME = "Mario";

    /// Name of the non-owner user.
    private static final String OTHER_NAME = "Luigi";

    /// Title of the test level.
    private static final String LEVEL_TITLE = "title";

    /// Description of the test level.
    private static final String LEVEL_DESC = "desc";

    /// The service being tested.
    @Autowired
    private LevelPublishService service;

    /// Mocked level repository to provide test data.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mocked user repository to verify if users exist.
    @MockitoBean
    private UserRepository userRepository;

    /// The user who owns the test level.
    private User owner;

    /// A user who does not own the test level.
    private User otherUser;

    /// A standard level used for testing.
    private Level testLevel;

    /// A level that is ready to be published (it has a start flag and an
    /// exit door).
    private Level publishableLevel;

    /// Sets up the test data before each test.
    @BeforeEach
    void setup() {
        this.owner = new User(OWNER_ID, OWNER_NAME);
        this.otherUser = new User(OTHER_USER_ID, OTHER_NAME);

        this.testLevel = new Level(LEVEL_TITLE, LEVEL_DESC, this.owner);

        this.publishableLevel = new Level(LEVEL_TITLE, LEVEL_DESC, this.owner);
        final Position flag = new Position(1, 1);
        final Position door = new Position(2, 1);
        this.publishableLevel.putObjectLayer(flag, new StartFlag(68, flag));
        this.publishableLevel.putObjectLayer(door, new ExitDoor(115, door));
    }

    /// Tests if the service is loaded correctly.
    @Test
    @DisplayName("initializes the service correctly")
    void serviceWired() {
        Assertions.assertNotNull(service);
    }

    /// Tests for the publish method.
    @Nested
    @DisplayName("publish")
    class Publish {

        /// A missing level throws LevelNotFoundException and does not save.
        @Test
        @DisplayName("throws LevelNotFoundException and does not save when the level is missing")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> service.publish(OWNER_ID, LEVEL_ID));

            verify(levelRepository, never()).save(any());
        }

        /// A missing user throws UserNotFoundException and does not save.
        @Test
        @DisplayName("throws UserNotFoundException and does not save when the user is missing")
        void userNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));
            when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(UserNotFoundException.class,
                    () -> service.publish(OWNER_ID, LEVEL_ID));

            verify(levelRepository, never()).save(any());
        }

        /// A user who is not the owner cannot publish the level.
        @Test
        @DisplayName("throws ForbiddenUserException and does not save when a non-owner tries to publish")
        void nonOwnerCannotPublish() {
            publishableLevel.validatePublishEligible(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishableLevel));
            when(userRepository.findById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> service.publish(OTHER_USER_ID, LEVEL_ID));

            verify(levelRepository, never()).save(any());
        }

        /// A level that is not eligible to be published is rejected.
        @Test
        @DisplayName("throws ForbiddenLevelActionException and does not save when the level is not ready to publish")
        void notEligibleCannotPublish() {
            // Did NOT call validatePublishEligible
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishableLevel));
            when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));

            Assertions.assertThrows(ForbiddenLevelActionException.class,
                    () -> service.publish(OWNER_ID, LEVEL_ID));

            verify(levelRepository, never()).save(any());
        }

        /// The owner can publish an eligible level.
        @Test
        @DisplayName("marks the level as published and saves it")
        void ownerPublishesEligibleLevel() {
            publishableLevel.validatePublishEligible(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishableLevel));
            when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));

            service.publish(OWNER_ID, LEVEL_ID);

            Assertions.assertTrue(publishableLevel.isPublished());
            verify(levelRepository).save(publishableLevel);
        }
    }

    /// Tests for the unpublishLevel method.
    @Nested
    @DisplayName("unpublishLevel")
    class Unpublish {

        /// A missing level throws LevelNotFoundException and does not save.
        @Test
        @DisplayName("throws LevelNotFoundException and does not save when the level is missing")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> service.unpublishLevel(OWNER_ID, LEVEL_ID));

            verify(levelRepository, never()).save(any());
        }

        /// A user who is not the owner cannot unpublish a level.
        @Test
        @DisplayName("throws ForbiddenUserException and does not save when a non-owner tries to unpublish")
        void nonOwnerCannotUnpublish() {
            publishableLevel.validatePublishEligible(OWNER_ID);
            publishableLevel.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishableLevel));

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> service.unpublishLevel(OTHER_USER_ID, LEVEL_ID));

            verify(levelRepository, never()).save(any());
        }

        /// The owner can unpublish their own published level.
        @Test
        @DisplayName("marks a published level as unpublished and saves it")
        void ownerUnpublishes() {
            publishableLevel.validatePublishEligible(OWNER_ID);
            publishableLevel.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(publishableLevel));

            service.unpublishLevel(OWNER_ID, LEVEL_ID);

            Assertions.assertFalse(publishableLevel.isPublished());
            verify(levelRepository).save(publishableLevel);
        }

        /// Unpublishing a level that is already unpublished does nothing.
        @Test
        @DisplayName("does nothing if the level is already unpublished")
        void unpublishingUnpublishedIsIdempotent() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            service.unpublishLevel(OWNER_ID, LEVEL_ID);

            Assertions.assertFalse(testLevel.isPublished());
            verify(levelRepository).save(testLevel);
        }
    }

    /// Tests for the validateLevelPublishEligible method.
    @Nested
    @DisplayName("validateLevelPublishEligible")
    class ValidateEligible {

        /// The owner can mark their level as ready to publish.
        @Test
        @DisplayName("sets the publish-eligible flag for the owner and saves")
        void setsFlag() {
            service.validateLevelPublishEligible(testLevel, OWNER_ID);

            Assertions.assertTrue(testLevel.isPublishEligible());
            verify(levelRepository).save(testLevel);
        }

        /// A user who is not the owner cannot mark a level as ready to publish.
        @Test
        @DisplayName("throws ForbiddenUserException and does not save when a non-owner tries to validate")
        void nonOwnerCannotValidate() {
            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> service.validateLevelPublishEligible(testLevel, OTHER_USER_ID));

            verify(levelRepository, never()).save(any());
        }
    }

    /// Tests for the invalidateLevelPublishEligible method.
    @Nested
    @DisplayName("invalidateLevelPublishEligible")
    class InvalidateEligible {

        /// The owner can remove the ready to publish status from their level.
        @Test
        @DisplayName("clears the publish-eligible flag for the owner and saves")
        void clearsFlag() {
            testLevel.validatePublishEligible(OWNER_ID);

            service.invalidateLevelPublishEligible(testLevel, OWNER_ID);

            Assertions.assertFalse(testLevel.isPublishEligible());
            verify(levelRepository).save(testLevel);
        }

        /// A user who is not the owner cannot remove the ready to publish status.
        @Test
        @DisplayName("throws ForbiddenUserException and does not save when a non-owner tries to invalidate")
        void nonOwnerCannotInvalidate() {
            testLevel.validatePublishEligible(OWNER_ID);

            Assertions.assertThrows(ForbiddenUserException.class,
                    () -> service.invalidateLevelPublishEligible(testLevel, OTHER_USER_ID));

            verify(levelRepository, never()).save(any());
        }
    }
}
