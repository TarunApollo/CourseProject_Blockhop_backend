package ch.usi.inf.bsc.sa4.lab02spring.model;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotPlayableException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ObjectPlacementConflictException;

@DisplayName("In the Level class")
@SuppressWarnings("NullAway")
class LevelTests {

    private static User createTestUser() {
        return new User("user-1", "Mario");
    }

    private static Level createLevelFor(final User creator) {
        return new Level("Test level", "A level description", creator);
    }

    private static Level createTestLevel() {
        return createLevelFor(createTestUser());
    }

    private static void publishTestLevel(final Level level) {
        final Position flagPos = new Position(1, 1);
        final Position doorPos = new Position(2, 1);
        level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
        level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
        level.validatePublishEligible("user-1");
        level.publish("user-1");
    }

    @Test
    @DisplayName("can be created with title, description, and creator")
    void creatorTest() {
        final Executable codeToExecute = LevelTests::createTestLevel;
        assertDoesNotThrow(codeToExecute);
    }

    @Nested
    @DisplayName("when a level is newly created")
    class NewlyCreatedLevel {

        private Level level;
        private User creator;
        private String title;
        private String description;

        @BeforeEach
        void setUp() {
            this.title = "Test level";
            this.description = "A level description";
            this.creator = createTestUser();
            this.level = createLevelFor(this.creator);
        }

        @Test
        @DisplayName("should have the correct title")
        void hasCorrectTitle() {
            assertEquals(this.title, this.level.getTitle());
        }

        @Test
        @DisplayName("should have the correct description")
        void hasCorrectDescription() {
            assertEquals(this.description, this.level.getDescription());
        }

        @Test
        @DisplayName("should have the correct creator")
        void hasCorrectCreator() {
            assertSame(this.creator, this.level.getCreator());
        }

        @Test
        @DisplayName("should be unpublished")
        void isNotPublished() {
            assertFalse(this.level.isPublished());
        }

        @Test
        @DisplayName("should be modifiable")
        void isModifiable() {
            assertTrue(this.level.canBeModified());
        }

        @Test
        @DisplayName("should start as not publish eligible")
        void isNotPublishEligible() {
            assertFalse(this.level.isPublishEligible());
        }

        @Test
        @DisplayName("should have the correct width")
        void hasCorrectWidth() {
            assertEquals(256, this.level.getWidth());
        }

        @Test
        @DisplayName("should have the correct height")
        void hasCorrectHeight() {
            assertEquals(14, this.level.getHeight());
        }

        @Test
        @DisplayName("should start with no clear condition")
        void hasNoClearCondition() {
            assertInstanceOf(Condition.NoClearCondition.class, this.level.getClearCondition().condition());
        }

        @Test
        @DisplayName("should start with zero target amount")
        void hasZeroTargetAmount() {
            assertEquals(0, this.level.getClearCondition().targetAmount());
        }

        @Test
        @DisplayName("should start with an empty object layer")
        void startsWithEmptyObjectLayer() {
            assertTrue(this.level.getObjectLayer().isEmpty());
        }

        @Test
        @DisplayName("should start with an empty world layer")
        void startsWithEmptyWorldLayer() {
            assertTrue(this.level.getWorldLayer().isEmpty());
        }
    }

    @Nested
    @DisplayName("methods setTitle, setDescription, and setClearCondition")
    class Setters {

        private Level level;
        private ClearCondition clearCondition;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.SLIME), 2);
        }

        @Test
        @DisplayName("should update the title")
        void updatesTitle() {
            this.level.setTitle("New title");
            assertEquals("New title", this.level.getTitle());
        }

        @Test
        @DisplayName("should update the description")
        void updatesDescription() {
            this.level.setDescription("New description");
            assertEquals("New description", this.level.getDescription());
        }

        @Test
        @DisplayName("should update the clear condition")
        void updatesClearCondition() {
            this.level.setClearCondition(this.clearCondition);
            assertSame(this.clearCondition, this.level.getClearCondition());
        }
    }

    @Nested
    @DisplayName("methods isOwnedBy and ensureOwnedBy")
    class OwnershipMethods {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createLevelFor(new User("owner-id", "Mario"));
        }

        @Test
        @DisplayName("should return true when user id matches the owner")
        void returnsTrueForOwnerId() {
            assertTrue(this.level.isOwnedBy("owner-id"));
        }

        @Test
        @DisplayName("should return false when user id does not match the owner")
        void returnsFalseForOtherId() {
            assertFalse(this.level.isOwnedBy("other-id"));
        }

        @Test
        @DisplayName("should return true when user matches the owner")
        void returnsTrueForOwnerUser() {
            assertTrue(this.level.isOwnedBy(new User("owner-id", "Mario clone")));
        }

        @Test
        @DisplayName("should return false when user does not match the owner")
        void returnsFalseForOtherUser() {
            assertFalse(this.level.isOwnedBy(new User("other-id", "Luigi")));
        }

        @Nested
        @DisplayName("method ensureOwnedBy")
        class EnsureOwnedByMethod {

            @Test
            @DisplayName("throws ForbiddenUserException")
            void throwsForbiddenUserException() {
                final Executable codeToExecute = () -> OwnershipMethods.this.level.ensureOwnedBy("other-id");
                assertThrows(ForbiddenUserException.class, codeToExecute);
            }

            @Test
            @DisplayName("should not throw when the user owns the level")
            void allowsOwner() {
                final Executable codeToExecute = () -> OwnershipMethods.this.level.ensureOwnedBy("owner-id");
                assertDoesNotThrow(codeToExecute);
            }
        }
    }

    @Nested
    @DisplayName("methods canBeModified and ensureModifiable")
    class PublicationMethods {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        @Test
        @DisplayName("should report modifiable when unpublished")
        void isModifiableWhenUnpublished() {
            assertTrue(this.level.canBeModified());
        }

        @Test
        @DisplayName("should not throw ensureModifiable when unpublished")
        void doesNotThrowWhenUnpublished() {
            assertDoesNotThrow(() -> this.level.ensureModifiable());
        }

        @Test
        @DisplayName("should report not modifiable when published")
        void isNotModifiableWhenPublished() {
            publishTestLevel(this.level);
            assertFalse(this.level.canBeModified());
        }

        @Test
        @DisplayName("should throw LevelPublishedException when published")
        void throwsWhenPublished() {
            publishTestLevel(this.level);
            assertThrows(LevelPublishedException.class, () -> this.level.ensureModifiable());
        }
    }

    @Nested
    @DisplayName("methods isWithinBounds and ensureWithinBounds")
    class BoundsMethods {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        @Test
        @DisplayName("should accept position at top-left corner")
        void acceptsTopLeftCorner() {
            assertTrue(this.level.isWithinBounds(new Position(0, 0)));
        }

        @Test
        @DisplayName("should accept position at bottom-right corner")
        void acceptsBottomRightCorner() {
            assertTrue(this.level.isWithinBounds(new Position(255, 13)));
        }

        @Test
        @DisplayName("should reject position with negative x")
        void rejectsNegativeX() {
            assertFalse(this.level.isWithinBounds(new Position(-1, 0)));
        }

        @Test
        @DisplayName("should reject position with x equal to width")
        void rejectsXEqualToWidth() {
            assertFalse(this.level.isWithinBounds(new Position(256, 0)));
        }

        @Test
        @DisplayName("should reject position with negative y")
        void rejectsNegativeY() {
            assertFalse(this.level.isWithinBounds(new Position(0, -1)));
        }

        @Test
        @DisplayName("should reject position with y equal to height")
        void rejectsYEqualToHeight() {
            assertFalse(this.level.isWithinBounds(new Position(0, 14)));
        }

        @Nested
        @DisplayName("method ensureWithinBounds")
        class EnsureWithinBoundsMethod {

            @Test
            @DisplayName("throws IllegalArgumentException when position is null")
            void nullPosition() {
                assertThrows(IllegalArgumentException.class, () -> BoundsMethods.this.level.ensureWithinBounds(null));
            }

            @Test
            @DisplayName("throws IllegalArgumentException when position is out of bounds")
            void outOfBoundsPosition() {
                final Position pos = new Position(256, 14);
                final Executable codeToExecute = () -> BoundsMethods.this.level.ensureWithinBounds(pos);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            @Test
            @DisplayName("should not throw when position is valid")
            void validPosition() {
                assertDoesNotThrow(() -> BoundsMethods.this.level.ensureWithinBounds(new Position(255, 13)));
            }
        }
    }

    @Nested
    @DisplayName("methods getObjectLayer and getWorldLayer")
    class LayerGetters {

        private Level level;
        private Position position;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.position = new Position(1, 1);
            this.level.putWorldLayer(this.position, new GroundObject(8));
            this.level.putObjectLayer(this.position, new StartFlag(9, this.position));
        }

        @Test
        @DisplayName("should return an unmodifiable world layer")
        void returnsUnmodifiableWorldLayer() {
            final Position pos = new Position(2, 2);
            final GroundObject ground = new GroundObject(3);
            final Executable modifyWorldLayer = () -> this.level.getWorldLayer().put(pos, ground);
            assertThrows(UnsupportedOperationException.class, modifyWorldLayer);
        }

        @Test
        @DisplayName("should return an unmodifiable object layer")
        void returnsUnmodifiableObjectLayer() {
            final Position pos = new Position(2, 2);
            final StartFlag flag = new StartFlag(4, pos);
            final Executable modifyObjectLayer = () -> this.level.getObjectLayer().put(pos, flag);
            assertThrows(UnsupportedOperationException.class, modifyObjectLayer);
        }
    }

    @Nested
    @DisplayName("methods putObjectLayer, putWorldLayer, removeObjectLayer, and removeGroundObject")
    class LayerMutationMethods {

        private Level level;
        private Position objectPosition;
        private Position worldPosition;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.objectPosition = new Position(2, 3);
            this.worldPosition = new Position(4, 5);
        }

        @Test
        @DisplayName("should replace an object layer entry")
        void replacesObjectLayerEntry() {
            final StartFlag firstObject = new StartFlag(10, this.objectPosition);
            final Coin replacementObject = new Coin(11, this.objectPosition, CoinType.BRONZE_COIN);
            this.level.putObjectLayer(this.objectPosition, firstObject);
            this.level.putObjectLayer(this.objectPosition, replacementObject);
            assertSame(replacementObject, this.level.getObjectLayer().get(this.objectPosition));
        }

        @Test
        @DisplayName("should replace a world layer entry")
        void replacesWorldLayerEntry() {
            final GroundObject firstGround = new GroundObject(20);
            final GroundObject replacementGround = new GroundObject(21);
            this.level.putWorldLayer(this.worldPosition, firstGround);
            this.level.putWorldLayer(this.worldPosition, replacementGround);
            assertEquals(replacementGround, this.level.getWorldLayer().get(this.worldPosition));
        }

        @Test
        @DisplayName("should remove an object layer entry")
        void removesObjectLayerEntry() {
            this.level.putObjectLayer(this.objectPosition, new StartFlag(10, this.objectPosition));
            this.level.removeObjectLayer(this.objectPosition);
            assertFalse(this.level.getObjectLayer().containsKey(this.objectPosition));
        }

        @Test
        @DisplayName("should remove a world layer entry")
        void removesWorldLayerEntry() {
            this.level.putWorldLayer(this.worldPosition, new GroundObject(20));
            this.level.removeGroundObject(this.worldPosition);
            assertFalse(this.level.getWorldLayer().containsKey(this.worldPosition));
        }
    }

    @Nested
    @DisplayName("method setWorldLayer")
    class SetWorldLayerMethod {

        private Level level;
        private Position pos1;
        private Position pos2;
        private Position newPos;
        private Map<Position, GroundObject> newLayer;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.pos1 = new Position(1, 2);
            this.pos2 = new Position(3, 4);
            this.newPos = new Position(7, 8);
            this.newLayer = new HashMap<>();
            this.newLayer.put(this.newPos, new GroundObject(10));
            this.level.putWorldLayer(this.pos1, new GroundObject(5));
            this.level.putWorldLayer(this.pos2, new GroundObject(6));
        }

        @Test
        @DisplayName("should remove the first old entry from the layer")
        void removesFirstOldEntry() {
            this.level.setWorldLayer(this.newLayer);
            assertFalse(this.level.getWorldLayer().containsKey(this.pos1));
        }

        @Test
        @DisplayName("should remove the second old entry from the layer")
        void removesSecondOldEntry() {
            this.level.setWorldLayer(this.newLayer);
            assertFalse(this.level.getWorldLayer().containsKey(this.pos2));
        }

        @Test
        @DisplayName("should contain the new entry in the layer")
        void containsNewEntry() {
            this.level.setWorldLayer(this.newLayer);
            assertEquals(new GroundObject(10), this.level.getWorldLayer().get(this.newPos));
        }

        @Test
        @DisplayName("should clear the world layer when given an empty map")
        void clearsLayer() {
            this.level.setWorldLayer(new HashMap<>());
            assertTrue(this.level.getWorldLayer().isEmpty());
        }
    }

    @Nested
    @DisplayName("method setObjectLayer")
    class SetObjectLayerMethod {

        private Level level;
        private Position pos;
        private Position newPos;
        private Map<Position, GameObject> newLayer;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.pos = new Position(1, 2);
            this.newPos = new Position(5, 6);
            this.newLayer = new HashMap<>();
            this.newLayer.put(this.newPos, new StartFlag(77, this.newPos));
            this.level.putObjectLayer(this.pos, new Coin(33, this.pos, CoinType.GOLD_COIN));
        }

        @Test
        @DisplayName("should remove the previous entry when replacing")
        void removesPreviousEntry() {
            this.level.setObjectLayer(this.newLayer);
            assertFalse(this.level.getObjectLayer().containsKey(this.pos));
        }

        @Test
        @DisplayName("should contain the new entry after replacing")
        void containsNewEntry() {
            this.level.setObjectLayer(this.newLayer);
            assertTrue(this.level.getObjectLayer().containsKey(this.newPos));
        }

        @Test
        @DisplayName("should clear the object layer when given an empty map")
        void clearsLayer() {
            this.level.setObjectLayer(new HashMap<>());
            assertTrue(this.level.getObjectLayer().isEmpty());
        }
    }

    @Nested
    @DisplayName("method cloneFor")
    class CloneForMethod {

        private Level original;
        private User cloneCreator;
        private Position worldPosition;
        private Position objectPosition;
        private ClearCondition clearCondition;
        private Level cloned;

        @BeforeEach
        void setUp() {
            final User originalCreator = createTestUser();
            this.cloneCreator = new User("user-2", "Luigi");
            this.original = new Level("Original", "Original description", originalCreator);
            this.worldPosition = new Position(3, 4);
            this.objectPosition = new Position(5, 6);
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.COIN), 5);
            publishTestLevel(this.original);
            this.original.setClearCondition(this.clearCondition);
            this.original.putWorldLayer(this.worldPosition, new GroundObject(21));
            this.original.putObjectLayer(this.objectPosition, new Coin(33, this.objectPosition, CoinType.GOLD_COIN));
            this.cloned = this.original.cloneFor(this.cloneCreator, "Cloned Title");
        }

        @Test
        @DisplayName("should create an unpublished copy")
        void clonedIsNotPublished() {
            assertFalse(this.cloned.isPublished());
        }

        @Test
        @DisplayName("should create a modifiable copy")
        void clonedIsModifiable() {
            assertTrue(this.cloned.canBeModified());
        }

        @Test
        @DisplayName("should assign the new creator to the cloned level")
        void clonedHasNewCreator() {
            assertSame(this.cloneCreator, this.cloned.getCreator());
        }

        @Test
        @DisplayName("should use the given title for the cloned level")
        void clonedHasNewTitle() {
            assertEquals("Cloned Title", this.cloned.getTitle());
        }

        @Test
        @DisplayName("should copy the description to the cloned level")
        void clonedHasSameDescription() {
            assertEquals(this.original.getDescription(), this.cloned.getDescription());
        }

        @Test
        @DisplayName("should copy the clear condition to the cloned level")
        void clonedHasSameClearCondition() {
            assertEquals(this.clearCondition, this.cloned.getClearCondition());
        }

        @Test
        @DisplayName("should copy the world layer to the cloned level")
        void clonedHasSameWorldLayer() {
            assertEquals(this.original.getWorldLayer(), this.cloned.getWorldLayer());
        }

        @Test
        @DisplayName("should copy the object layer to the cloned level")
        void clonedHasSameObjectLayer() {
            assertEquals(this.original.getObjectLayer(), this.cloned.getObjectLayer());
        }

        @Test
        @DisplayName("should not share the world layer with the original")
        void doesNotShareWorldLayer() {
            final Position clonedOnlyPos = new Position(10, 2);
            this.cloned.putWorldLayer(clonedOnlyPos, new GroundObject(99));
            assertFalse(this.original.getWorldLayer().containsKey(clonedOnlyPos));
        }

        @Test
        @DisplayName("should not share the object layer with the original")
        void doesNotShareObjectLayer() {
            final Position clonedOnlyPos = new Position(11, 3);
            this.cloned.putObjectLayer(clonedOnlyPos, new StartFlag(77, clonedOnlyPos));
            assertFalse(this.original.getObjectLayer().containsKey(clonedOnlyPos));
        }
    }

    @Nested
    @DisplayName("method publish")
    class PublishMethod {

        private Level level;
        private Position flagPos;
        private Position doorPos;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.flagPos = new Position(1, 1);
            this.doorPos = new Position(2, 1);
        }

        @Nested
        @DisplayName("throws ForbiddenUserException")
        class WhenUserIsNotOwner {

            @Test
            @DisplayName("when user does not own the level")
            void wrongUser() {
                final Executable codeToExecute = () -> PublishMethod.this.level.publish("other-id");
                assertThrows(ForbiddenUserException.class, codeToExecute);
            }
        }

        @Nested
        @DisplayName("throws ForbiddenLevelActionException")
        class WhenCannotPublish {

            @Test
            @DisplayName("when object layer has no start flag")
            void noStartFlag() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos, new ExitDoor(115, PublishMethod.this.doorPos));
                PublishMethod.this.level.validatePublishEligible("user-1");
                final Executable codeToExecute = () -> PublishMethod.this.level.publish("user-1");
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when object layer has no exit door")
            void noExitDoor() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos, new StartFlag(68, PublishMethod.this.flagPos));
                PublishMethod.this.level.validatePublishEligible("user-1");
                final Executable codeToExecute = () -> PublishMethod.this.level.publish("user-1");
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when level is not publish eligible")
            void notPublishEligible() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos, new StartFlag(68, PublishMethod.this.flagPos));
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos, new ExitDoor(115, PublishMethod.this.doorPos));
                final Executable codeToExecute = () -> PublishMethod.this.level.publish("user-1");
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }
        }

        @Nested
        @DisplayName("when publishing is valid")
        class WhenValid {

            @Test
            @DisplayName("should mark the level as published")
            void marksAsPublished() {
                publishTestLevel(PublishMethod.this.level);
                assertTrue(PublishMethod.this.level.isPublished());
            }
        }
    }

    @Nested
    @DisplayName("method validatePublishEligible")
    class ValidatePublishEligibleMethod {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        @Test
        @DisplayName("throws ForbiddenUserException when user does not own the level")
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.validatePublishEligible("other-id");
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        @Test
        @DisplayName("should set publish eligible to true")
        void setsPublishEligible() {
            this.level.validatePublishEligible("user-1");
            assertTrue(this.level.isPublishEligible());
        }
    }

    @Nested
    @DisplayName("method invalidatePublishEligible")
    class InvalidatePublishEligibleMethod {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.level.validatePublishEligible("user-1");
        }

        @Test
        @DisplayName("throws ForbiddenUserException when user does not own the level")
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.invalidatePublishEligible("other-id");
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        @Test
        @DisplayName("should set publish eligible to false")
        void setsPublishIneligible() {
            this.level.invalidatePublishEligible("user-1");
            assertFalse(this.level.isPublishEligible());
        }
    }

    @Nested
    @DisplayName("method unpublish")
    class UnpublishMethod {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            publishTestLevel(this.level);
        }

        @Test
        @DisplayName("throws ForbiddenUserException when user does not own the level")
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.unpublish("other-id");
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        @Test
        @DisplayName("should mark the level as unpublished")
        void marksAsUnpublished() {
            this.level.unpublish("user-1");
            assertFalse(this.level.isPublished());
        }

        @Test
        @DisplayName("should not throw when called on an already unpublished level")
        void doesNotThrowWhenCalledTwice() {
            this.level.unpublish("user-1");
            final Executable codeToExecute = () -> this.level.unpublish("user-1");
            assertDoesNotThrow(codeToExecute);
        }

        @Test
        @DisplayName("should remain unpublished after a second unpublish call")
        void remainsUnpublishedAfterSecondCall() {
            this.level.unpublish("user-1");
            this.level.unpublish("user-1");
            assertFalse(this.level.isPublished());
        }
    }

    @Nested
    @DisplayName("method ensureValidObjectLayer")
    class EnsureValidObjectLayerMethod {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        @Nested
        @DisplayName("throws IllegalArgumentException")
        class WhenObjectLayerIsInvalid {

            @Test
            @DisplayName("when there are more than one start flags")
            void moreThanOneFlag() {
                final Position pos1 = new Position(1, 1);
                final Position pos2 = new Position(3, 1);
                final Map<Position, GameObject> layer = new HashMap<>();
                layer.put(pos1, new StartFlag(68, pos1));
                layer.put(pos2, new StartFlag(68, pos2));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level.ensureValidObjectLayer(layer);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            @Test
            @DisplayName("when there are more than one exit doors")
            void moreThanOneDoor() {
                final Position pos1 = new Position(1, 1);
                final Position pos2 = new Position(3, 1);
                final Map<Position, GameObject> layer = new HashMap<>();
                layer.put(pos1, new ExitDoor(115, pos1));
                layer.put(pos2, new ExitDoor(115, pos2));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level.ensureValidObjectLayer(layer);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }
        }

        @Nested
        @DisplayName("when object layer is valid")
        class WhenObjectLayerIsValid {

            @Test
            @DisplayName("should not throw for an empty layer")
            void emptyLayer() {
                final Map<Position, GameObject> layer = new HashMap<>();
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level.ensureValidObjectLayer(layer);
                assertDoesNotThrow(codeToExecute);
            }

            @Test
            @DisplayName("should not throw for one flag and one door")
            void oneFlagOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos = new Position(2, 1);
                final Map<Position, GameObject> layer = new HashMap<>();
                layer.put(flagPos, new StartFlag(68, flagPos));
                layer.put(doorPos, new ExitDoor(115, doorPos));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level.ensureValidObjectLayer(layer);
                assertDoesNotThrow(codeToExecute);
            }
        }
    }

    @Nested
    @DisplayName("method ensurePublishableObjectLayer")
    class EnsurePublishableObjectLayerMethod {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        @Nested
        @DisplayName("throws ForbiddenLevelActionException")
        class WhenObjectLayerIsNotPublishable {

            @Test
            @DisplayName("when there are no start flags")
            void noFlag() {
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when there are no exit doors")
            void noDoor() {
                final Position flagPos = new Position(1, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when there are more than one start flags")
            void moreThanOneFlag() {
                final Position flagPos1 = new Position(1, 1);
                final Position flagPos2 = new Position(3, 1);
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos1, new StartFlag(68, flagPos1));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos2, new StartFlag(68, flagPos2));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when there are more than one exit doors")
            void moreThanOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos1 = new Position(2, 1);
                final Position doorPos2 = new Position(4, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos1, new ExitDoor(115, doorPos1));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos2, new ExitDoor(115, doorPos2));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }
        }

        @Nested
        @DisplayName("when object layer is publishable")
        class WhenObjectLayerIsPublishable {

            @Test
            @DisplayName("should not throw when there is exactly one flag and one door")
            void oneFlagOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertDoesNotThrow(codeToExecute);
            }
        }
    }

    @Nested
    @DisplayName("method ensurePlayable")
    class EnsurePlayableMethod {

        private Level level;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        @Test
        @DisplayName("throws LevelNotPlayableException when level is unpublished and user is not the owner")
        void unpublishedNotOwner() {
            final Executable codeToExecute = () -> this.level.ensurePlayable("other-id");
            assertThrows(LevelNotPlayableException.class, codeToExecute);
        }

        @Test
        @DisplayName("should not throw when level is unpublished but user is the owner")
        void unpublishedOwner() {
            final Executable codeToExecute = () -> this.level.ensurePlayable("user-1");
            assertDoesNotThrow(codeToExecute);
        }

        @Test
        @DisplayName("should not throw when level is published regardless of user")
        void publishedLevel() {
            publishTestLevel(this.level);
            final Executable codeToExecute = () -> this.level.ensurePlayable("other-id");
            assertDoesNotThrow(codeToExecute);
        }
    }

    @Nested
    @DisplayName("method ensureObjectCanBePlacedAt")
    class EnsureObjectCanBePlacedAtMethod {

        private Level level;
        private Position position;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.position = new Position(3, 3);
        }

        @Nested
        @DisplayName("throws ObjectPlacementConflictException")
        class WhenPositionIsOccupied {

            @Test
            @DisplayName("when world layer has an object at the position")
            void worldLayerOccupied() {
                EnsureObjectCanBePlacedAtMethod.this.level.putWorldLayer(EnsureObjectCanBePlacedAtMethod.this.position, new GroundObject(5));
                final Executable codeToExecute = () -> EnsureObjectCanBePlacedAtMethod.this.level.ensureObjectCanBePlacedAt(EnsureObjectCanBePlacedAtMethod.this.position);
                assertThrows(ObjectPlacementConflictException.class, codeToExecute);
            }

            @Test
            @DisplayName("when object layer has an object at the position")
            void objectLayerOccupied() {
                EnsureObjectCanBePlacedAtMethod.this.level.putObjectLayer(EnsureObjectCanBePlacedAtMethod.this.position, new StartFlag(68, EnsureObjectCanBePlacedAtMethod.this.position));
                final Executable codeToExecute = () -> EnsureObjectCanBePlacedAtMethod.this.level.ensureObjectCanBePlacedAt(EnsureObjectCanBePlacedAtMethod.this.position);
                assertThrows(ObjectPlacementConflictException.class, codeToExecute);
            }
        }

        @Test
        @DisplayName("should not throw when both layers are empty at the position")
        void bothLayersEmpty() {
            final Executable codeToExecute = () -> this.level.ensureObjectCanBePlacedAt(this.position);
            assertDoesNotThrow(codeToExecute);
        }
    }

    @Nested
    @DisplayName("method updateBoxContent")
    class UpdateBoxContentMethod {

        private Level level;
        private Position validPosition;
        private Position outOfBoundsPosition;

        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.validPosition = new Position(5, 5);
            this.outOfBoundsPosition = new Position(256, 14);
        }

        @Nested
        @DisplayName("throws IllegalArgumentException")
        class WhenPositionOrObjectIsInvalid {

            @Test
            @DisplayName("when position is out of bounds")
            void outOfBounds() {
                final Content noContent = new Content.NoContent();
                final Executable codeToExecute = () -> UpdateBoxContentMethod.this.level.updateBoxContent(UpdateBoxContentMethod.this.outOfBoundsPosition, noContent);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            @Test
            @DisplayName("when the object at the position is not a box")
            void notABox() {
                UpdateBoxContentMethod.this.level.putObjectLayer(UpdateBoxContentMethod.this.validPosition, new StartFlag(68, UpdateBoxContentMethod.this.validPosition));
                final Content noContent = new Content.NoContent();
                final Executable codeToExecute = () -> UpdateBoxContentMethod.this.level.updateBoxContent(UpdateBoxContentMethod.this.validPosition, noContent);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }
        }

        @Test
        @DisplayName("throws NoSuchElementException when no object exists at the position")
        void noObjectAtPosition() {
            final Content noContent = new Content.NoContent();
            final Executable codeToExecute = () -> this.level.updateBoxContent(this.validPosition, noContent);
            assertThrows(NoSuchElementException.class, codeToExecute);
        }

        @Test
        @DisplayName("updated object should be a Box")
        void updatedObjectIsBox() {
            final Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            this.level.putObjectLayer(this.validPosition, new Box(42, this.validPosition, new Content.NoContent()));
            this.level.updateBoxContent(this.validPosition, newContent);
            assertInstanceOf(Box.class, this.level.getObjectLayer().get(this.validPosition));
        }

        @Test
        @DisplayName("updated box should have the new content")
        void updatedBoxHasNewContent() {
            final Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            this.level.putObjectLayer(this.validPosition, new Box(42, this.validPosition, new Content.NoContent()));
            this.level.updateBoxContent(this.validPosition, newContent);
            assertEquals(newContent, ((Box) this.level.getObjectLayer().get(this.validPosition)).content());
        }
    }
}
