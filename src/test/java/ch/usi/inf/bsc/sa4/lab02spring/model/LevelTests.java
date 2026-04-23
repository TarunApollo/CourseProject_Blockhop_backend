package ch.usi.inf.bsc.sa4.lab02spring.model;


import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

///
/// Verifies creation, mutation, ownership, bounds checking, layer handling,
/// publication rules, and cloning behavior for Level.
///
@DisplayName("In the Level class")
@SuppressWarnings("NullAway")
public class LevelTests {

    ///
    /// Verifies that a level can be created with title, description, and creator.
    ///
    @Test
    @DisplayName("can be created with title, description, and creator")
    public void creatorTest() {
        final User creator = new User("user-1", "Mario");
        final Executable codeToExecute = () -> new Level("Test level", "A level description", creator);
        Assertions.assertDoesNotThrow(codeToExecute);
    }

    ///
    /// Tests the initial state of a newly created Level.
    ///
    @Nested
    @DisplayName("when a level is newly created")
    class NewlyCreatedLevel {

        /// The level under test.
        private Level level;
        /// The creator of the level.
        private User creator;

        @BeforeEach
        void setUp() {
            this.creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", this.creator);
        }

        ///
        /// Verifies that the provided metadata is stored correctly.
        ///
        @Test
        @DisplayName("should store the provided metadata")
        public void storesProvidedMetadata() {
            Assertions.assertEquals("Test level", this.level.getTitle());
            Assertions.assertEquals("A level description", this.level.getDescription());
            Assertions.assertSame(this.creator, this.level.getCreator());
        }

        ///
        /// Verifies that a new level starts unpublished and modifiable.
        ///
        @Test
        @DisplayName("should be unpublished")
        public void isUnpublished() {
            Assertions.assertFalse(this.level.isPublished());
            Assertions.assertTrue(this.level.canBeModified());
        }

        ///
        /// Verifies that the level exposes its fixed dimensions.
        ///
        @Test
        @DisplayName("should expose the fixed dimensions")
        public void hasFixedDimensions() {
            Assertions.assertEquals(256, this.level.getWidth());
            Assertions.assertEquals(14, this.level.getHeight());
        }

        ///
        /// Verifies that the default clear condition is present.
        ///
        @Test
        @DisplayName("should start with the default clear condition")
        public void hasDefaultClearCondition() {
            final ClearCondition clearCondition = this.level.getClearCondition();
            Assertions.assertTrue(clearCondition.condition() instanceof Condition.NoClearCondition);
            Assertions.assertEquals(0, clearCondition.targetAmount());
        }

        ///
        /// Verifies that both layers start empty.
        ///
        @Test
        @DisplayName("should start with empty layers")
        public void startsWithEmptyLayers() {
            Assertions.assertTrue(this.level.getObjectLayer().isEmpty());
            Assertions.assertTrue(this.level.getWorldLayer().isEmpty());
        }
    }

    ///
    /// Tests the mutator methods for title, description, and clear condition.
    ///
    @Nested
    @DisplayName("methods setTitle, setDescription, and setClearCondition")
    class Setters {

        /// The level under test.
        private Level level;
        /// The clear condition to assign.
        private ClearCondition clearCondition;

        @BeforeEach
        void setUp() {
            final User creator = new User("user-1", "Mario");
            this.level = new Level("Old title", "Old description", creator);
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.SLIME), 2);
        }

        ///
        /// Verifies that mutable fields are updated correctly.
        ///
        @Test
        @DisplayName("should update the mutable fields")
        public void updatesMutableFields() {
            this.level.setTitle("New title");
            this.level.setDescription("New description");
            this.level.setClearCondition(this.clearCondition);

            Assertions.assertEquals("New title", this.level.getTitle());
            Assertions.assertEquals("New description", this.level.getDescription());
            Assertions.assertSame(this.clearCondition, this.level.getClearCondition());
        }
    }

    ///
    /// Tests ownership-related methods on Level.
    ///
    @Nested
    @DisplayName("methods isOwnedBy and ensureOwnedBy")
    class OwnershipMethods {

        /// The level under test.
        private Level level;

        @BeforeEach
        void setUp() {
            final User creator = new User("owner-id", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        ///
        /// Verifies ownership checks using a user id.
        ///
        @Test
        @DisplayName("should report ownership for the matching user id")
        public void matchesUserId() {
            Assertions.assertTrue(this.level.isOwnedBy("owner-id"));
            Assertions.assertFalse(this.level.isOwnedBy("other-id"));
        }

        ///
        /// Verifies ownership checks using a user instance.
        ///
        @Test
        @DisplayName("should report ownership for the matching user")
        public void matchesUser() {
            Assertions.assertTrue(this.level.isOwnedBy(new User("owner-id", "Mario clone")));
            Assertions.assertFalse(this.level.isOwnedBy(new User("other-id", "Luigi")));
        }

        ///
        /// Tests the method that enforces ownership.
        ///
        @Nested
        @DisplayName("method ensureOwnedBy")
        class EnsureOwnedByMethod {

            ///
            /// Verifies that a non-owner triggers a forbidden exception.
            ///
            @Test
            @DisplayName("throws ForbiddenUserException")
            public void throwsForbiddenUserException() {
                final Executable codeToExecute = () -> OwnershipMethods.this.level.ensureOwnedBy("other-id");
                Assertions.assertThrows(ForbiddenUserException.class, codeToExecute);
            }

            ///
            /// Verifies that the owner is allowed to perform the operation.
            ///
            @Test
            @DisplayName("should not throw when the user owns the level")
            public void allowsOwner() {
                final Executable codeToExecute = () -> OwnershipMethods.this.level.ensureOwnedBy("owner-id");
                Assertions.assertDoesNotThrow(codeToExecute);
            }
        }
    }

    ///
    /// Tests publication-related methods on Level.
    ///
    @Nested
    @DisplayName("methods canBeModified and ensureModifiable")
    class PublicationMethods {

        /// The level under test.
        private Level level;

        @BeforeEach
        void setUp() {
            final User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        ///
        /// Verifies that an unpublished level can be modified.
        ///
        @Test
        @DisplayName("should allow modification when unpublished")
        public void allowsModificationWhenUnpublished() {
            Assertions.assertTrue(this.level.canBeModified());
            Assertions.assertDoesNotThrow(() -> this.level.ensureModifiable());
        }

        ///
        /// Verifies that a published level rejects modifications.
        ///
        @Test
        @DisplayName("should reject modification when published")
        public void rejectsModificationWhenPublished() {
            final Position flagPos = new Position(1, 1);
            final Position doorPos = new Position(2, 1);
            this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
            this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
            this.level.validatePublishEligible("user-1");
            this.level.publish("user-1");
            Assertions.assertFalse(this.level.canBeModified());
            Assertions.assertThrows(LevelPublishedException.class, () -> this.level.ensureModifiable());
        }
    }

    ///
    /// Tests bounds-related methods on Level.
    ///
    @Nested
    @DisplayName("methods isWithinBounds and ensureWithinBounds")
    class BoundsMethods {

        /// The level under test.
        private Level level;

        @BeforeEach
        void setUp() {
            final User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
        }

        ///
        /// Verifies that boundary positions are accepted.
        ///
        @Test
        @DisplayName("should accept positions on the borders")
        public void acceptsBorderPositions() {
            Assertions.assertTrue(this.level.isWithinBounds(new Position(0, 0)));
            Assertions.assertTrue(this.level.isWithinBounds(new Position(255, 13)));
        }

        ///
        /// Verifies that positions outside the valid range are rejected.
        ///
        @Test
        @DisplayName("should reject positions outside the valid range")
        public void rejectsOutOfBoundsPositions() {
            Assertions.assertFalse(this.level.isWithinBounds(new Position(-1, 0)));
            Assertions.assertFalse(this.level.isWithinBounds(new Position(256, 0)));
            Assertions.assertFalse(this.level.isWithinBounds(new Position(0, -1)));
            Assertions.assertFalse(this.level.isWithinBounds(new Position(0, 14)));
        }

        ///
        /// Tests the bounds-enforcement method.
        ///
        @Nested
        @DisplayName("method ensureWithinBounds")
        class EnsureWithinBoundsMethod {

            ///
            /// Verifies that a null position is rejected.
            ///
            @Test
            @DisplayName("throws IllegalArgumentException when position is null")
            public void nullPosition() {
                Assertions.assertThrows(IllegalArgumentException.class, () -> BoundsMethods.this.level.ensureWithinBounds(null));
            }

            ///
            /// Verifies that an out-of-bounds position is rejected.
            ///
            @Test
            @DisplayName("throws IllegalArgumentException when position is out of bounds")
            public void outOfBoundsPosition() {
                Assertions.assertThrows(IllegalArgumentException.class,
                    () -> BoundsMethods.this.level.ensureWithinBounds(new Position(256, 14)));
            }

            ///
            /// Verifies that a valid position is accepted.
            ///
            @Test
            @DisplayName("should not throw when position is valid")
            public void validPosition() {
                Assertions.assertDoesNotThrow(() -> BoundsMethods.this.level.ensureWithinBounds(new Position(255, 13)));
            }
        }
    }

    ///
    /// Tests the layer getter methods on Level.
    ///
    @Nested
    @DisplayName("methods getObjectLayer and getWorldLayer")
    class LayerGetters {

        /// The level under test.
        private Level level;
        /// A reference position used in the layer setup.
        private Position position;

        @BeforeEach
        void setUp() {
            final User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.position = new Position(1, 1);
            this.level.putWorldLayer(this.position, new GroundObject(8));
            this.level.putObjectLayer(this.position, new StartFlag(9, this.position));
        }

        ///
        /// Verifies that the returned layer views cannot be modified directly.
        ///
        @Test
        @DisplayName("should return unmodifiable views")
        public void returnsUnmodifiableViews() {
            final Executable modifyWorldLayer = () -> this.level.getWorldLayer().put(new Position(2, 2), new GroundObject(3));
            final Executable modifyObjectLayer = () -> this.level.getObjectLayer().put(new Position(2, 2), new StartFlag(4, new Position(2, 2)));

            Assertions.assertThrows(UnsupportedOperationException.class, modifyWorldLayer);
            Assertions.assertThrows(UnsupportedOperationException.class, modifyObjectLayer);
        }
    }

    ///
    /// Tests mutation methods for the object and world layers.
    ///
    @Nested
    @DisplayName("methods putObjectLayer, putWorldLayer, removeObjectLayer, and removeGroundObject")
    class LayerMutationMethods {

        /// The level under test.
        private Level level;
        /// The position used for object-layer operations.
        private Position objectPosition;
        /// The position used for world-layer operations.
        private Position worldPosition;

        @BeforeEach
        void setUp() {
            final User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.objectPosition = new Position(2, 3);
            this.worldPosition = new Position(4, 5);
        }

        ///
        /// Verifies that entries can be added and replaced in both layers.
        ///
        @Test
        @DisplayName("should add and replace entries in both layers")
        public void addsAndReplacesEntries() {
            final StartFlag firstObject = new StartFlag(10, this.objectPosition);
            final Coin replacementObject = new Coin(11, this.objectPosition, CoinType.BRONZE_COIN
);
            final GroundObject firstGround = new GroundObject(20);
            final GroundObject replacementGround = new GroundObject(21);

            this.level.putObjectLayer(this.objectPosition, firstObject);
            this.level.putObjectLayer(this.objectPosition, replacementObject);
            this.level.putWorldLayer(this.worldPosition, firstGround);
            this.level.putWorldLayer(this.worldPosition, replacementGround);

            Assertions.assertSame(replacementObject, this.level.getObjectLayer().get(this.objectPosition));
            Assertions.assertEquals(replacementGround, this.level.getWorldLayer().get(this.worldPosition));
        }

        ///
        /// Verifies that entries can be removed from both layers.
        ///
        @Test
        @DisplayName("should remove existing entries from both layers")
        public void removesEntries() {
            this.level.putObjectLayer(this.objectPosition, new StartFlag(10, this.objectPosition));
            this.level.putWorldLayer(this.worldPosition, new GroundObject(20));

            this.level.removeObjectLayer(this.objectPosition);
            this.level.removeGroundObject(this.worldPosition);
            Assertions.assertFalse(this.level.getObjectLayer().containsKey(this.objectPosition));
            Assertions.assertFalse(this.level.getWorldLayer().containsKey(this.worldPosition));
        }
    }

    ///
    /// Tests replacing the full world layer at once.
    ///
    @Nested
    @DisplayName("method setWorldLayer")
    class SetWorldLayerMethod {

        /// The level under test.
        private Level level;
        /// The first position used during setup.
        private Position pos1;
        /// The second position used during setup.
        private Position pos2;

        @BeforeEach
        void setUp() {
            final User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.pos1 = new Position(1, 2);
            this.pos2 = new Position(3, 4);
            this.level.putWorldLayer(this.pos1, new GroundObject(5));
            this.level.putWorldLayer(this.pos2, new GroundObject(6));
        }

        ///
        /// Verifies that the entire world layer is replaced.
        ///
        @Test
        @DisplayName("should replace the entire world layer")
        public void replacesEntireLayer() {
            final Position newPos = new Position(7, 8);
            final Map<Position, GroundObject> newLayer = new HashMap<>();
            newLayer.put(newPos, new GroundObject(10));
            this.level.setWorldLayer(newLayer);
            Assertions.assertFalse(this.level.getWorldLayer().containsKey(this.pos1));
            Assertions.assertFalse(this.level.getWorldLayer().containsKey(this.pos2));
            Assertions.assertEquals(new GroundObject(10), this.level.getWorldLayer().get(newPos));
        }

        ///
        /// Verifies that an empty map clears the world layer.
        ///
        @Test
        @DisplayName("should clear the world layer when given an empty map")
        public void clearsLayer() {
            this.level.setWorldLayer(new HashMap<>());
            Assertions.assertTrue(this.level.getWorldLayer().isEmpty());
        }
    }

    ///
    /// Tests replacing the full object layer at once.
    ///
    @Nested
    @DisplayName("method setObjectLayer")
    class SetObjectLayerMethod {

        /// The level under test.
        private Level level;
        /// The position used during setup.
        private Position pos;

        @BeforeEach
        void setUp() {
            final User creator = new User("user-1", "Mario");
            this.level = new Level("Test level", "A level description", creator);
            this.pos = new Position(1, 2);
            this.level.putObjectLayer(this.pos, new Coin(33, this.pos, CoinType.GOLD_COIN));
        }

        ///
        /// Verifies that the entire object layer is replaced.
        ///
        @Test
        @DisplayName("should replace the entire object layer")
        public void replacesEntireLayer() {
            final Position newPos = new Position(5, 6);
            final Map<Position, GameObject> newLayer = new HashMap<>();
            newLayer.put(newPos, new StartFlag(77, newPos));
            this.level.setObjectLayer(newLayer);
            Assertions.assertFalse(this.level.getObjectLayer().containsKey(this.pos));
            Assertions.assertTrue(this.level.getObjectLayer().containsKey(newPos));
        }

        ///
        /// Verifies that an empty map clears the object layer.
        ///
        @Test
        @DisplayName("should clear the object layer when given an empty map")
        public void clearsLayer() {
            this.level.setObjectLayer(new HashMap<>());
            Assertions.assertTrue(this.level.getObjectLayer().isEmpty());
        }
    }

    ///
    /// Tests cloning behavior for Level.
    ///
    @Nested
    @DisplayName("method cloneFor")
    class CloneForMethod {

        /// The original level used as the source for cloning.
        private Level original;
        /// The original creator of the source level.
        private User originalCreator;
        /// The user that should receive the cloned level.
        private User cloneCreator;
        /// A position used for world-layer setup.
        private Position worldPosition;
        /// A position used for object-layer setup.
        private Position objectPosition;
        /// The clear condition assigned to the original level.
        private ClearCondition clearCondition;

        @BeforeEach
        void setUp() {
            this.originalCreator = new User("user-1", "Mario");
            this.cloneCreator = new User("user-2", "Luigi");
            this.original = new Level("Original", "Original description", this.originalCreator);
            this.worldPosition = new Position(3, 4);
            this.objectPosition = new Position(5, 6);
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.COIN), 5);
            final Position flagPos = new Position(1, 1);
            final Position doorPos = new Position(2, 1);
            this.original.putObjectLayer(flagPos, new StartFlag(68, flagPos));
            this.original.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
            this.original.validatePublishEligible("user-1");
            this.original.publish("user-1");
            this.original.setClearCondition(this.clearCondition);
            this.original.putWorldLayer(this.worldPosition, new GroundObject(21));
            this.original.putObjectLayer(this.objectPosition, new Coin(33, this.objectPosition, CoinType.GOLD_COIN
));
        }

        ///
        /// Verifies that cloning creates an unpublished copy for the new creator.
        ///
        @Test
        @DisplayName("should create an unpublished copy for the new creator")
        public void createsUnpublishedCopy() {
            final Level cloned = this.original.cloneFor(this.cloneCreator, "Cloned Title");
            Assertions.assertFalse(cloned.isPublished());
            Assertions.assertTrue(cloned.canBeModified());
            Assertions.assertSame(this.cloneCreator, cloned.getCreator());
            Assertions.assertEquals("Cloned Title", cloned.getTitle());
        }

        ///
        /// Verifies that cloning copies metadata, clear condition, and layers.
        ///
        @Test
        @DisplayName("should copy metadata, condition, and layers")
        public void copiesState() {
            final Level cloned = this.original.cloneFor(this.cloneCreator, this.original.getTitle());
            Assertions.assertEquals(this.original.getTitle(), cloned.getTitle());
            Assertions.assertEquals(this.original.getDescription(), cloned.getDescription());
            Assertions.assertEquals(this.clearCondition, cloned.getClearCondition());
            Assertions.assertEquals(this.original.getWorldLayer(), cloned.getWorldLayer());
            Assertions.assertEquals(this.original.getObjectLayer(), cloned.getObjectLayer());
        }

        ///
        /// Verifies that cloning copies layer contents without sharing the same maps.
        ///
        @Test
        @DisplayName("should copy the layer maps instead of sharing them")
        public void copiesLayerMaps() {
            final Level cloned = this.original.cloneFor(this.cloneCreator, "Copy");
            final Position clonedOnlyWorldPosition = new Position(10, 2);
            final Position clonedOnlyObjectPosition = new Position(11, 3);
            cloned.putWorldLayer(clonedOnlyWorldPosition, new GroundObject(99));
            cloned.putObjectLayer(clonedOnlyObjectPosition, new StartFlag(77, clonedOnlyObjectPosition));
            Assertions.assertFalse(this.original.getWorldLayer().containsKey(clonedOnlyWorldPosition));
            Assertions.assertFalse(this.original.getObjectLayer().containsKey(clonedOnlyObjectPosition));
        }
    }
}
