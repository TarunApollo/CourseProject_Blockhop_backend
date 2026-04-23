package ch.usi.inf.bsc.sa4.lab02spring.model;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
public class LevelTests {

    @Test
    @DisplayName("can be created with title, description, and creator")
    public void creatorTest() {
        User creator = new User("user-1", "Mario");
        Executable codeToExecute = () -> new Level("Test level", "A level description", creator);
        assertDoesNotThrow(codeToExecute);
    }

    @Nested
    @DisplayName("when a level is newly created")
    class NewlyCreatedLevel {

        private Level level;
        private User creator;

        @BeforeEach
        void setUp() {
            this.creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", this.creator);
        }

        @Test
        @DisplayName("should store the provided metadata")
        public void storesProvidedMetadata() {
            assertEquals("Test level", this.level.getTitle());
            assertEquals("A level description", this.level.getDescription());
            assertSame(this.creator, this.level.getCreator());
        }

        @Test
        @DisplayName("should be unpublished")
        public void isUnpublished() {
            assertFalse(this.level.isPublished());
            assertTrue(this.level.canBeModified());
        }

        @Test
        @DisplayName("should start as not publish eligible")
        public void isNotPublishEligible() {
            assertFalse(this.level.isPublishEligible());
        }

        @Test
        @DisplayName("should expose the fixed dimensions")
        public void hasFixedDimensions() {
            assertEquals(256, this.level.getWidth());
            assertEquals(14, this.level.getHeight());
        }

        @Test
        @DisplayName("should start with the default clear condition")
        public void hasDefaultClearCondition() {
            ClearCondition clearCondition = this.level.getClearCondition();
            assertTrue(clearCondition.condition() instanceof Condition.NoClearCondition);
            assertEquals(0, clearCondition.targetAmount());
        }

        @Test
        @DisplayName("should start with empty layers")
        public void startsWithEmptyLayers() {
            assertTrue(this.level.getObjectLayer().isEmpty());
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Old title", "Old description", creator);
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.SLIME), 2);
        }

        @Test
        @DisplayName("should update the mutable fields")
        public void updatesMutableFields() {
            this.level.setTitle("New title");
            this.level.setDescription("New description");
            this.level.setClearCondition(this.clearCondition);

            assertEquals("New title", this.level.getTitle());
            assertEquals("New description", this.level.getDescription());
            assertSame(this.clearCondition, this.level.getClearCondition());
        }
    }

    @Nested
    @DisplayName("methods isOwnedBy and ensureOwnedBy")
    class OwnershipMethods {

        private Level level;

        @BeforeEach
        void setUp() {
            User creator = new User("owner-id", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        @Test
        @DisplayName("should report ownership for the matching user id")
        public void matchesUserId() {
            assertTrue(this.level.isOwnedBy("owner-id"));
            assertFalse(this.level.isOwnedBy("other-id"));
        }

        @Test
        @DisplayName("should report ownership for the matching user")
        public void matchesUser() {
            assertTrue(this.level.isOwnedBy(new User("owner-id", "Mario clone")));
            assertFalse(this.level.isOwnedBy(new User("other-id", "Luigi")));
        }

        @Nested
        @DisplayName("method ensureOwnedBy")
        class EnsureOwnedByMethod {

            @Test
            @DisplayName("throws ForbiddenUserException")
            public void throwsForbiddenUserException() {
                Executable codeToExecute = () -> OwnershipMethods.this.level.ensureOwnedBy("other-id");
                assertThrows(ForbiddenUserException.class, codeToExecute);
            }

            @Test
            @DisplayName("should not throw when the user owns the level")
            public void allowsOwner() {
                Executable codeToExecute = () -> OwnershipMethods.this.level.ensureOwnedBy("owner-id");
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        @Test
        @DisplayName("should allow modification when unpublished")
        public void allowsModificationWhenUnpublished() {
            assertTrue(this.level.canBeModified());
            assertDoesNotThrow(() -> this.level.ensureModifiable());
        }

        @Test
        @DisplayName("should reject modification when published")
        public void rejectsModificationWhenPublished() {
            Position flagPos = new Position(1, 1);
            Position doorPos = new Position(2, 1);
            this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
            this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
            this.level.validatePublishEligible("user-1");
            this.level.publish("user-1");
            assertFalse(this.level.canBeModified());
            assertThrows(LevelPublishedException.class, () -> this.level.ensureModifiable());
        }
    }

    @Nested
    @DisplayName("methods isWithinBounds and ensureWithinBounds")
    class BoundsMethods {

        private Level level;

        @BeforeEach
        void setUp() {
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        @Test
        @DisplayName("should accept positions on the borders")
        public void acceptsBorderPositions() {
            assertTrue(this.level.isWithinBounds(new Position(0, 0)));
            assertTrue(this.level.isWithinBounds(new Position(255, 13)));
        }

        @Test
        @DisplayName("should reject positions outside the valid range")
        public void rejectsOutOfBoundsPositions() {
            assertFalse(this.level.isWithinBounds(new Position(-1, 0)));
            assertFalse(this.level.isWithinBounds(new Position(256, 0)));
            assertFalse(this.level.isWithinBounds(new Position(0, -1)));
            assertFalse(this.level.isWithinBounds(new Position(0, 14)));
        }

        @Nested
        @DisplayName("method ensureWithinBounds")
        class EnsureWithinBoundsMethod {

            @Test
            @DisplayName("throws IllegalArgumentException when position is null")
            public void nullPosition() {
                assertThrows(IllegalArgumentException.class, () -> BoundsMethods.this.level.ensureWithinBounds(null));
            }

            @Test
            @DisplayName("throws IllegalArgumentException when position is out of bounds")
            public void outOfBoundsPosition() {
                assertThrows(IllegalArgumentException.class,
                    () -> BoundsMethods.this.level.ensureWithinBounds(new Position(256, 14)));
            }

            @Test
            @DisplayName("should not throw when position is valid")
            public void validPosition() {
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.position = new Position(1, 1);
            this.level.putWorldLayer(this.position, new GroundObject(8));
            this.level.putObjectLayer(this.position, new StartFlag(9, this.position));
        }

        @Test
        @DisplayName("should return unmodifiable views")
        public void returnsUnmodifiableViews() {
            Executable modifyWorldLayer = () -> this.level.getWorldLayer().put(new Position(2, 2), new GroundObject(3));
            Executable modifyObjectLayer = () -> this.level.getObjectLayer().put(new Position(2, 2), new StartFlag(4, new Position(2, 2)));

            assertThrows(UnsupportedOperationException.class, modifyWorldLayer);
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.objectPosition = new Position(2, 3);
            this.worldPosition = new Position(4, 5);
        }

        @Test
        @DisplayName("should add and replace entries in both layers")
        public void addsAndReplacesEntries() {
            StartFlag firstObject = new StartFlag(10, this.objectPosition);
            Coin replacementObject = new Coin(11, this.objectPosition, CoinType.BRONZE_COIN
);
            GroundObject firstGround = new GroundObject(20);
            GroundObject replacementGround = new GroundObject(21);

            this.level.putObjectLayer(this.objectPosition, firstObject);
            this.level.putObjectLayer(this.objectPosition, replacementObject);
            this.level.putWorldLayer(this.worldPosition, firstGround);
            this.level.putWorldLayer(this.worldPosition, replacementGround);

            assertSame(replacementObject, this.level.getObjectLayer().get(this.objectPosition));
            assertEquals(replacementGround, this.level.getWorldLayer().get(this.worldPosition));
        }

        @Test
        @DisplayName("should remove existing entries from both layers")
        public void removesEntries() {
            this.level.putObjectLayer(this.objectPosition, new StartFlag(10, this.objectPosition));
            this.level.putWorldLayer(this.worldPosition, new GroundObject(20));

            this.level.removeObjectLayer(this.objectPosition);
            this.level.removeGroundObject(this.worldPosition);
            assertFalse(this.level.getObjectLayer().containsKey(this.objectPosition));
            assertFalse(this.level.getWorldLayer().containsKey(this.worldPosition));
        }
    }

    @Nested
    @DisplayName("method setWorldLayer")
    class SetWorldLayerMethod {

        private Level level;
        private Position pos1;
        private Position pos2;

        @BeforeEach
        void setUp() {
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.pos1 = new Position(1, 2);
            this.pos2 = new Position(3, 4);
            this.level.putWorldLayer(this.pos1, new GroundObject(5));
            this.level.putWorldLayer(this.pos2, new GroundObject(6));
        }

        @Test
        @DisplayName("should replace the entire world layer")
        public void replacesEntireLayer() {
            Position newPos = new Position(7, 8);
            Map<Position, GroundObject> newLayer = new HashMap<>();
            newLayer.put(newPos, new GroundObject(10));
            this.level.setWorldLayer(newLayer);
            assertFalse(this.level.getWorldLayer().containsKey(this.pos1));
            assertFalse(this.level.getWorldLayer().containsKey(this.pos2));
            assertEquals(new GroundObject(10), this.level.getWorldLayer().get(newPos));
        }

        @Test
        @DisplayName("should clear the world layer when given an empty map")
        public void clearsLayer() {
            this.level.setWorldLayer(new HashMap<>());
            assertTrue(this.level.getWorldLayer().isEmpty());
        }
    }

    @Nested
    @DisplayName("method setObjectLayer")
    class SetObjectLayerMethod {

        private Level level;
        private Position pos;

        @BeforeEach
        void setUp() {
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.pos = new Position(1, 2);
            this.level.putObjectLayer(this.pos, new Coin(33, this.pos, CoinType.GOLD_COIN));
        }

        @Test
        @DisplayName("should replace the entire object layer")
        public void replacesEntireLayer() {
            Position newPos = new Position(5, 6);
            Map<Position, GameObject> newLayer = new HashMap<>();
            newLayer.put(newPos, new StartFlag(77, newPos));
            this.level.setObjectLayer(newLayer);
            assertFalse(this.level.getObjectLayer().containsKey(this.pos));
            assertTrue(this.level.getObjectLayer().containsKey(newPos));
        }

        @Test
        @DisplayName("should clear the object layer when given an empty map")
        public void clearsLayer() {
            this.level.setObjectLayer(new HashMap<>());
            assertTrue(this.level.getObjectLayer().isEmpty());
        }
    }

    @Nested
    @DisplayName("method cloneFor")
    class CloneForMethod {

        private Level original;
        private User originalCreator;
        private User cloneCreator;
        private Position worldPosition;
        private Position objectPosition;
        private ClearCondition clearCondition;

        @BeforeEach
        void setUp() {
            this.originalCreator = new User("user-1", "Mario");
            this.cloneCreator = new User("user-2", "Luigi");
            this.original = new Level("Original", "Original description", this.originalCreator);
            this.worldPosition = new Position(3, 4);
            this.objectPosition = new Position(5, 6);
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.COIN), 5);
            Position flagPos = new Position(1, 1);
            Position doorPos = new Position(2, 1);
            this.original.putObjectLayer(flagPos, new StartFlag(68, flagPos));
            this.original.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
            this.original.validatePublishEligible("user-1");
            this.original.publish("user-1");
            this.original.setClearCondition(this.clearCondition);
            this.original.putWorldLayer(this.worldPosition, new GroundObject(21));
            this.original.putObjectLayer(this.objectPosition, new Coin(33, this.objectPosition, CoinType.GOLD_COIN
));
        }

        @Test
        @DisplayName("should create an unpublished copy for the new creator")
        public void createsUnpublishedCopy() {
            Level cloned = this.original.cloneFor(this.cloneCreator, "Cloned Title");
            assertFalse(cloned.isPublished());
            assertTrue(cloned.canBeModified());
            assertSame(this.cloneCreator, cloned.getCreator());
            assertEquals("Cloned Title", cloned.getTitle());
        }

        @Test
        @DisplayName("should copy metadata, condition, and layers")
        public void copiesState() {
            Level cloned = this.original.cloneFor(this.cloneCreator, this.original.getTitle());
            assertEquals(this.original.getTitle(), cloned.getTitle());
            assertEquals(this.original.getDescription(), cloned.getDescription());
            assertEquals(this.clearCondition, cloned.getClearCondition());
            assertEquals(this.original.getWorldLayer(), cloned.getWorldLayer());
            assertEquals(this.original.getObjectLayer(), cloned.getObjectLayer());
        }

        @Test
        @DisplayName("should copy the layer maps instead of sharing them")
        public void copiesLayerMaps() {
            Level cloned = this.original.cloneFor(this.cloneCreator, "Copy");
            Position clonedOnlyWorldPosition = new Position(10, 2);
            Position clonedOnlyObjectPosition = new Position(11, 3);
            cloned.putWorldLayer(clonedOnlyWorldPosition, new GroundObject(99));
            cloned.putObjectLayer(clonedOnlyObjectPosition, new StartFlag(77, clonedOnlyObjectPosition));
            assertFalse(this.original.getWorldLayer().containsKey(clonedOnlyWorldPosition));
            assertFalse(this.original.getObjectLayer().containsKey(clonedOnlyObjectPosition));
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.flagPos = new Position(1, 1);
            this.doorPos = new Position(2, 1);
        }

        @Nested
        @DisplayName("throws ForbiddenUserException")
        class WhenUserIsNotOwner {

            @Test
            @DisplayName("when user does not own the level")
            public void wrongUser() {
                Executable codeToExecute = () -> PublishMethod.this.level.publish("other-id");
                assertThrows(ForbiddenUserException.class, codeToExecute);
            }
        }

        @Nested
        @DisplayName("throws ForbiddenLevelActionException")
        class WhenCannotPublish {

            @Test
            @DisplayName("when object layer has no start flag")
            public void noStartFlag() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos, new ExitDoor(115, PublishMethod.this.doorPos));
                PublishMethod.this.level.validatePublishEligible("user-1");
                Executable codeToExecute = () -> PublishMethod.this.level.publish("user-1");
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when object layer has no exit door")
            public void noExitDoor() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos, new StartFlag(68, PublishMethod.this.flagPos));
                PublishMethod.this.level.validatePublishEligible("user-1");
                Executable codeToExecute = () -> PublishMethod.this.level.publish("user-1");
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when level is not publish eligible")
            public void notPublishEligible() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos, new StartFlag(68, PublishMethod.this.flagPos));
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos, new ExitDoor(115, PublishMethod.this.doorPos));
                Executable codeToExecute = () -> PublishMethod.this.level.publish("user-1");
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }
        }

        @Nested
        @DisplayName("when publishing is valid")
        class WhenValid {

            @Test
            @DisplayName("should mark the level as published")
            public void marksAsPublished() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos, new StartFlag(68, PublishMethod.this.flagPos));
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos, new ExitDoor(115, PublishMethod.this.doorPos));
                PublishMethod.this.level.validatePublishEligible("user-1");
                PublishMethod.this.level.publish("user-1");
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        @Test
        @DisplayName("throws ForbiddenUserException when user does not own the level")
        public void wrongUser() {
            Executable codeToExecute = () -> this.level.validatePublishEligible("other-id");
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        @Test
        @DisplayName("should set publish eligible to true")
        public void setsPublishEligible() {
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.level.validatePublishEligible("user-1");
        }

        @Test
        @DisplayName("throws ForbiddenUserException when user does not own the level")
        public void wrongUser() {
            Executable codeToExecute = () -> this.level.invalidatePublishEligible("other-id");
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        @Test
        @DisplayName("should set publish eligible to false")
        public void setsPublishIneligible() {
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            Position flagPos = new Position(1, 1);
            Position doorPos = new Position(2, 1);
            this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
            this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
            this.level.validatePublishEligible("user-1");
            this.level.publish("user-1");
        }

        @Test
        @DisplayName("throws ForbiddenUserException when user does not own the level")
        public void wrongUser() {
            Executable codeToExecute = () -> this.level.unpublish("other-id");
            assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        @Test
        @DisplayName("should mark the level as unpublished")
        public void marksAsUnpublished() {
            this.level.unpublish("user-1");
            assertFalse(this.level.isPublished());
        }

        @Test
        @DisplayName("should be idempotent when called on an already unpublished level")
        public void isIdempotent() {
            this.level.unpublish("user-1");
            Executable codeToExecute = () -> this.level.unpublish("user-1");
            assertDoesNotThrow(codeToExecute);
            assertFalse(this.level.isPublished());
        }
    }

    @Nested
    @DisplayName("method ensureValidObjectLayer")
    class EnsureValidObjectLayerMethod {

        private Level level;

        @BeforeEach
        void setUp() {
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        @Nested
        @DisplayName("throws IllegalArgumentException")
        class WhenObjectLayerIsInvalid {

            @Test
            @DisplayName("when there are more than one start flags")
            public void moreThanOneFlag() {
                Position pos1 = new Position(1, 1);
                Position pos2 = new Position(3, 1);
                Map<Position, GameObject> layer = new HashMap<>();
                layer.put(pos1, new StartFlag(68, pos1));
                layer.put(pos2, new StartFlag(68, pos2));
                Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level.ensureValidObjectLayer(layer);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            @Test
            @DisplayName("when there are more than one exit doors")
            public void moreThanOneDoor() {
                Position pos1 = new Position(1, 1);
                Position pos2 = new Position(3, 1);
                Map<Position, GameObject> layer = new HashMap<>();
                layer.put(pos1, new ExitDoor(115, pos1));
                layer.put(pos2, new ExitDoor(115, pos2));
                Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level.ensureValidObjectLayer(layer);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }
        }

        @Nested
        @DisplayName("when object layer is valid")
        class WhenObjectLayerIsValid {

            @Test
            @DisplayName("should not throw for an empty layer")
            public void emptyLayer() {
                Map<Position, GameObject> layer = new HashMap<>();
                Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level.ensureValidObjectLayer(layer);
                assertDoesNotThrow(codeToExecute);
            }

            @Test
            @DisplayName("should not throw for one flag and one door")
            public void oneFlagOneDoor() {
                Position flagPos = new Position(1, 1);
                Position doorPos = new Position(2, 1);
                Map<Position, GameObject> layer = new HashMap<>();
                layer.put(flagPos, new StartFlag(68, flagPos));
                layer.put(doorPos, new ExitDoor(115, doorPos));
                Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level.ensureValidObjectLayer(layer);
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        @Nested
        @DisplayName("throws ForbiddenLevelActionException")
        class WhenObjectLayerIsNotPublishable {

            @Test
            @DisplayName("when there are no start flags")
            public void noFlag() {
                Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when there are no exit doors")
            public void noDoor() {
                Position flagPos = new Position(1, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when there are more than one start flags")
            public void moreThanOneFlag() {
                Position flagPos1 = new Position(1, 1);
                Position flagPos2 = new Position(3, 1);
                Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos1, new StartFlag(68, flagPos1));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos2, new StartFlag(68, flagPos2));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            @Test
            @DisplayName("when there are more than one exit doors")
            public void moreThanOneDoor() {
                Position flagPos = new Position(1, 1);
                Position doorPos1 = new Position(2, 1);
                Position doorPos2 = new Position(4, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos1, new ExitDoor(115, doorPos1));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos2, new ExitDoor(115, doorPos2));
                Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
                assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }
        }

        @Nested
        @DisplayName("when object layer is publishable")
        class WhenObjectLayerIsPublishable {

            @Test
            @DisplayName("should not throw when there is exactly one flag and one door")
            public void oneFlagOneDoor() {
                Position flagPos = new Position(1, 1);
                Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level.ensurePublishableObjectLayer();
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        @Test
        @DisplayName("throws LevelNotPlayableException when level is unpublished and user is not the owner")
        public void unpublishedNotOwner() {
            Executable codeToExecute = () -> this.level.ensurePlayable("other-id");
            assertThrows(LevelNotPlayableException.class, codeToExecute);
        }

        @Test
        @DisplayName("should not throw when level is unpublished but user is the owner")
        public void unpublishedOwner() {
            Executable codeToExecute = () -> this.level.ensurePlayable("user-1");
            assertDoesNotThrow(codeToExecute);
        }

        @Test
        @DisplayName("should not throw when level is published regardless of user")
        public void publishedLevel() {
            Position flagPos = new Position(1, 1);
            Position doorPos = new Position(2, 1);
            this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
            this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
            this.level.validatePublishEligible("user-1");
            this.level.publish("user-1");
            Executable codeToExecute = () -> this.level.ensurePlayable("other-id");
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.position = new Position(3, 3);
        }

        @Nested
        @DisplayName("throws ObjectPlacementConflictException")
        class WhenPositionIsOccupied {

            @Test
            @DisplayName("when world layer has an object at the position")
            public void worldLayerOccupied() {
                EnsureObjectCanBePlacedAtMethod.this.level.putWorldLayer(EnsureObjectCanBePlacedAtMethod.this.position, new GroundObject(5));
                Executable codeToExecute = () -> EnsureObjectCanBePlacedAtMethod.this.level.ensureObjectCanBePlacedAt(EnsureObjectCanBePlacedAtMethod.this.position);
                assertThrows(ObjectPlacementConflictException.class, codeToExecute);
            }

            @Test
            @DisplayName("when object layer has an object at the position")
            public void objectLayerOccupied() {
                EnsureObjectCanBePlacedAtMethod.this.level.putObjectLayer(EnsureObjectCanBePlacedAtMethod.this.position, new StartFlag(68, EnsureObjectCanBePlacedAtMethod.this.position));
                Executable codeToExecute = () -> EnsureObjectCanBePlacedAtMethod.this.level.ensureObjectCanBePlacedAt(EnsureObjectCanBePlacedAtMethod.this.position);
                assertThrows(ObjectPlacementConflictException.class, codeToExecute);
            }
        }

        @Test
        @DisplayName("should not throw when both layers are empty at the position")
        public void bothLayersEmpty() {
            Executable codeToExecute = () -> this.level.ensureObjectCanBePlacedAt(this.position);
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
            User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.validPosition = new Position(5, 5);
            this.outOfBoundsPosition = new Position(256, 14);
        }

        @Nested
        @DisplayName("throws IllegalArgumentException")
        class WhenPositionOrObjectIsInvalid {

            @Test
            @DisplayName("when position is out of bounds")
            public void outOfBounds() {
                Content content = new Content.NoContent();
                Executable codeToExecute = () -> UpdateBoxContentMethod.this.level.updateBoxContent(UpdateBoxContentMethod.this.outOfBoundsPosition, content);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            @Test
            @DisplayName("when the object at the position is not a box")
            public void notABox() {
                UpdateBoxContentMethod.this.level.putObjectLayer(UpdateBoxContentMethod.this.validPosition, new StartFlag(68, UpdateBoxContentMethod.this.validPosition));
                Content content = new Content.NoContent();
                Executable codeToExecute = () -> UpdateBoxContentMethod.this.level.updateBoxContent(UpdateBoxContentMethod.this.validPosition, content);
                assertThrows(IllegalArgumentException.class, codeToExecute);
            }
        }

        @Test
        @DisplayName("throws NoSuchElementException when no object exists at the position")
        public void noObjectAtPosition() {
            Content content = new Content.NoContent();
            Executable codeToExecute = () -> this.level.updateBoxContent(this.validPosition, content);
            assertThrows(NoSuchElementException.class, codeToExecute);
        }

        @Test
        @DisplayName("should update the content of a box at the given position")
        public void updatesBoxContent() {
            Content originalContent = new Content.NoContent();
            Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            Box box = new Box(42, this.validPosition, originalContent);
            this.level.putObjectLayer(this.validPosition, box);
            this.level.updateBoxContent(this.validPosition, newContent);
            GameObject updated = this.level.getObjectLayer().get(this.validPosition);
            assertTrue(updated instanceof Box);
            assertEquals(newContent, ((Box) updated).content());
        }
    }
}
