package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearConditionType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

/**
 * Tests covering {@link Level} creation and metadata updates.
 */
@DisplayName("In the Level creation and metadata API")
@SuppressWarnings({ "PMD.TooManyStaticImports", "PMD.CommentSize", "java:S2187" })
class LevelCreationTests {

    /**
     * Test constructor/creation of a level.
     */
    @Test
    @DisplayName("a level can be created with title, description, and creator")
    void creatorTest() {
        assertDoesNotThrow(LevelTestFixtures::createTestLevel);
    }

    /**
     * Tests for a newly created level.
     */
    @Nested
    @DisplayName("when a level is newly created")
    class NewlyCreatedLevel {

        /** The level instance under test. */
        private Level level;
        /** The creator of the level. */
        private User creator;
        /** The title of the level. */
        private String title;
        /** The description of the level. */
        private String description;

        /** Sets up the test environment for a newly created level. */
        @BeforeEach
        void setUp() {
            this.title = "Test level";
            this.description = "A level description";
            this.creator = LevelTestFixtures.createTestUser();
            this.level = LevelTestFixtures.createLevelFor(this.creator);
        }

        /**
         * Checks that all basic metadata about the level is initialised properly.
         */
        @Test
        @DisplayName("it initializes basic metadata correctly")
        void initializesBasicMetadata() {
            assertAll(
                    () -> assertEquals(this.title, this.level.getTitle()),
                    () -> assertEquals(this.description, this.level.getDescription()),
                    () -> assertSame(this.creator, this.level.getCreator()),
                    () -> assertFalse(this.level.isPublished()));
        }

        /**
         * Checks lifecycle flags like modifiability, publishability, and dimensions
         * for a freshly created level.
         */
        @Test
        @DisplayName("it initializes lifecycle flags and dimensions correctly")
        void initializesLifecycleFlagsAndDimensions() {
            assertAll(
                    () -> assertTrue(this.level.canBeModified()),
                    () -> assertFalse(this.level.isPublishEligible()),
                    () -> assertEquals(LevelTestFixtures.LEVEL_WIDTH, this.level.getWidth()),
                    () -> assertEquals(LevelTestFixtures.LEVEL_HEIGHT, this.level.getHeight()));
        }

        /**
         * Checks the default gameplay-related values for a new level.
         */
        @Test
        @DisplayName("it initializes gameplay defaults correctly")
        void initializesGameplayDefaults() {
            assertAll(
                    () -> assertInstanceOf(
                            Condition.NoClearCondition.class,
                            this.level.getClearCondition().condition()),
                    () -> assertEquals(0, this.level.getClearCondition().targetAmount()),
                    () -> assertTrue(this.level.getObjectLayer().isEmpty()),
                    () -> assertTrue(this.level.getWorldLayer().isEmpty()));
        }
    }

    /**
     * Tests for setter methods.
     */
    @Nested
    @DisplayName("metadata setters")
    class Setters {

        /** The level instance. */
        private Level level;
        /** A clear condition for testing. */
        private ClearCondition clearCondition;

        /** Sets up setters tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.SLIME), 2);
        }

        /** Verify setTitle. */
        @Test
        @DisplayName("setTitle updates the title")
        void updatesTitle() {
            this.level.setTitle("New title");
            assertEquals("New title", this.level.getTitle());
        }

        /** Verify setDescription. */
        @Test
        @DisplayName("setDescription updates the description")
        void updatesDescription() {
            this.level.setDescription("New description");
            assertEquals("New description", this.level.getDescription());
        }

        /** Verify setClearCondition. */
        @Test
        @DisplayName("setClearCondition updates the clear condition")
        void updatesClearCondition() {
            this.level.setClearCondition(this.clearCondition);
            assertSame(this.clearCondition, this.level.getClearCondition());
        }
    }
}
