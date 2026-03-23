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
}

