package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotPlayableException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

/**
 * Tests covering {@link Level} publication and playability.
 */
@DisplayName("In the Level publication and playability API")
@SuppressWarnings({ "PMD.TooManyStaticImports", "java:S2187" })
class LevelPublicationTests {

    /**
     * Tests for publication methods.
     */
    @Nested
    @DisplayName("methods canBeModified and ensureModifiable")
    class PublicationMethods {

        /** The level instance. */
        private Level level;

        /** Sets up publication tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
        }

        /** Verify canBeModified for unpublished. */
        @Test
        @DisplayName("an unpublished level is modifiable")
        void isModifiableWhenUnpublished() {
            assertTrue(this.level.canBeModified());
        }

        /** Verify ensureModifiable for unpublished. */
        @Test
        @DisplayName("ensureModifiable does not throw for an unpublished level")
        void doesNotThrowWhenUnpublished() {
            assertDoesNotThrow(() -> this.level.ensureModifiable());
        }

        /** Verify canBeModified for published. */
        @Test
        @DisplayName("a published level is not modifiable")
        void isNotModifiableWhenPublished() {
            LevelTestFixtures.publishTestLevel(this.level);
            assertFalse(this.level.canBeModified());
        }

        /** Verify ensureModifiable for published. */
        @Test
        @DisplayName("ensureModifiable throws LevelPublishedException for a published level")
        void throwsWhenPublished() {
            LevelTestFixtures.publishTestLevel(this.level);
            assertThrows(LevelPublishedException.class, () -> this.level.ensureModifiable());
        }
    }

    /**
     * Tests for the publish method.
     */
    @Nested
    @DisplayName("method publish")
    class PublishMethod {

        /** The level instance. */
        private Level level;
        /** Flag position. */
        private Position flagPos;
        /** Door position. */
        private Position doorPos;

        /** Sets up publish tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            this.flagPos = new Position(1, 1);
            this.doorPos = new Position(2, 1);
        }

        /**
         * Tests for unauthorized publish attempts.
         */
        @Nested
        @DisplayName("when user is not the owner")
        class WhenUserIsNotOwner {

            /** Verify wrong user. */
            @Test
            @DisplayName("it throws ForbiddenUserException")
            void wrongUser() {
                final Executable codeToExecute = () -> PublishMethod.this.level.publish(LevelTestFixtures.OTHER_ID);
                assertThrows(ForbiddenUserException.class, codeToExecute);
            }
        }

        /**
         * Tests for invalid publish states.
         */
        @Nested
        @DisplayName("when the level cannot be published")
        class WhenCannotPublish {

            /** Verify no flag. */
            @Test
            @DisplayName("it throws when the object layer has no start flag")
            void noStartFlag() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos,
                        LevelTestFixtures.createExitDoor(PublishMethod.this.doorPos));
                PublishMethod.this.level.validatePublishEligible(LevelTestFixtures.USER_ID);
                final Executable codeToExecute = () -> PublishMethod.this.level.publish(LevelTestFixtures.USER_ID);
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /** Verify no door. */
            @Test
            @DisplayName("it throws when the object layer has no exit door")
            void noExitDoor() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos,
                        LevelTestFixtures.createStartFlag(PublishMethod.this.flagPos));
                PublishMethod.this.level.validatePublishEligible(LevelTestFixtures.USER_ID);
                final Executable codeToExecute = () -> PublishMethod.this.level.publish(LevelTestFixtures.USER_ID);
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /** Verify not eligible. */
            @Test
            @DisplayName("it throws when the level is not marked publish eligible")
            void notPublishEligible() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos,
                        LevelTestFixtures.createStartFlag(PublishMethod.this.flagPos));
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos,
                        LevelTestFixtures.createExitDoor(PublishMethod.this.doorPos));
                final Executable codeToExecute = () -> PublishMethod.this.level.publish(LevelTestFixtures.USER_ID);
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }
        }

        /**
         * Tests for valid publish.
         */
        @Nested
        @DisplayName("when publishing is valid")
        class WhenValid {

            /** Verify published status. */
            @Test
            @DisplayName("it marks the level as published")
            void marksAsPublished() {
                LevelTestFixtures.publishTestLevel(PublishMethod.this.level);
                assertTrue(PublishMethod.this.level.isPublished());
            }
        }
    }

    /**
     * Tests for validatePublishEligible.
     */
    @Nested
    @DisplayName("method validatePublishEligible")
    class ValidatePublishEligibleMethod {

        /** The level instance. */
        private Level level;

        /** Sets up validation tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
        }

        /** Verify wrong user. */
        @Test
        @DisplayName("it throws ForbiddenUserException when user does not own the level")
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.validatePublishEligible(LevelTestFixtures.OTHER_ID);
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        /** Verify successful validation. */
        @Test
        @DisplayName("it sets publish eligible to true")
        void setsPublishEligible() {
            this.level.validatePublishEligible(LevelTestFixtures.USER_ID);
            assertTrue(this.level.isPublishEligible());
        }
    }

    /**
     * Tests for invalidatePublishEligible.
     */
    @Nested
    @DisplayName("method invalidatePublishEligible")
    class InvalidatePublishEligibleMethod {

        /** The level instance. */
        private Level level;

        /** Sets up invalidation tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            this.level.validatePublishEligible(LevelTestFixtures.USER_ID);
        }

        /** Verify wrong user. */
        @Test
        @DisplayName("it throws ForbiddenUserException when user does not own the level")
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.invalidatePublishEligible(LevelTestFixtures.OTHER_ID);
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        /** Verify successful invalidation. */
        @Test
        @DisplayName("it sets publish eligible to false")
        void setsPublishIneligible() {
            this.level.invalidatePublishEligible(LevelTestFixtures.USER_ID);
            assertFalse(this.level.isPublishEligible());
        }
    }

    /**
     * Tests for unpublish.
     */
    @Nested
    @DisplayName("method unpublish")
    class UnpublishMethod {

        /** The level instance. */
        private Level level;

        /** Sets up unpublish tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            LevelTestFixtures.publishTestLevel(this.level);
        }

        /** Verify wrong user. */
        @Test
        @DisplayName("it throws ForbiddenUserException when user does not own the level")
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.unpublish(LevelTestFixtures.OTHER_ID);
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        /** Verify successful unpublish. */
        @Test
        @DisplayName("it marks the level as unpublished")
        void marksAsUnpublished() {
            this.level.unpublish(LevelTestFixtures.USER_ID);
            assertFalse(this.level.isPublished());
        }

        /** Verify idempotency. */
        @Test
        @DisplayName("it does not throw when called twice")
        void doesNotThrowWhenCalledTwice() {
            this.level.unpublish(LevelTestFixtures.USER_ID);
            final Executable codeToExecute = () -> this.level.unpublish(LevelTestFixtures.USER_ID);
            assertDoesNotThrow(codeToExecute);
        }

        /** Verify status after second call. */
        @Test
        @DisplayName("it remains unpublished after a second unpublish call")
        void remainsUnpublishedAfterSecondCall() {
            this.level.unpublish(LevelTestFixtures.USER_ID);
            this.level.unpublish(LevelTestFixtures.USER_ID);
            assertFalse(this.level.isPublished());
        }
    }

    /**
     * Tests for ensurePlayable.
     */
    @Nested
    @DisplayName("method ensurePlayable")
    class EnsurePlayableMethod {

        /** The level instance. */
        private Level level;

        /** Sets up playability tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
        }

        /** Verify unpublished playability for non-owner. */
        @Test
        @DisplayName("it throws when the level is unpublished and user is not the owner")
        void unpublishedNotOwner() {
            final Executable codeToExecute = () -> this.level.ensurePlayable(LevelTestFixtures.OTHER_ID);
            assertThrows(LevelNotPlayableException.class, codeToExecute);
        }

        /** Verify unpublished playability for owner. */
        @Test
        @DisplayName("it allows the owner to play an unpublished level")
        void unpublishedOwner() {
            final Executable codeToExecute = () -> this.level.ensurePlayable(LevelTestFixtures.USER_ID);
            assertDoesNotThrow(codeToExecute);
        }

        /** Verify published playability. */
        @Test
        @DisplayName("it allows any user to play a published level")
        void publishedLevel() {
            LevelTestFixtures.publishTestLevel(this.level);
            final Executable codeToExecute = () -> this.level.ensurePlayable(LevelTestFixtures.OTHER_ID);
            assertDoesNotThrow(codeToExecute);
        }
    }
}
