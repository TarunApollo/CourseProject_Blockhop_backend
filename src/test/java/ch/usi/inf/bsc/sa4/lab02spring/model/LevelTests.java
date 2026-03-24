package ch.usi.inf.bsc.sa4.lab02spring.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;

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
    @DisplayName("methods setTitle, setDescription, setPublished, and setClearCondition")
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
            this.level.setPublished(true);
            this.level.setClearCondition(this.clearCondition);

            assertEquals("New title", this.level.getTitle());
            assertEquals("New description", this.level.getDescription());
            assertTrue(this.level.isPublished());
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
            this.level.setPublished(true);
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
            Coin replacementObject = new Coin(11, this.objectPosition, 5);
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
}
