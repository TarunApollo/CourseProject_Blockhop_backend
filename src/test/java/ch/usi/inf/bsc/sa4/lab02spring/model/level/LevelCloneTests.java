package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearConditionType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

/**
 * Tests covering cloning behavior for {@link Level}.
 */
@DisplayName("In the Level cloning API")
class LevelCloneTests {

    /**
     * Tests for the cloneFor method.
     */
    @Nested
    @DisplayName("method cloneFor")
    class CloneForMethod {

        /** Original level. */
        private Level original;
        /** Creator for the clone. */
        private User cloneCreator;
        /** Clear condition. */
        private ClearCondition clearCondition;
        /** Cloned level created in setUp for use across all tests. */
        private Level cloned;

        /** Sets up clone tests. */
        @BeforeEach
        void setUp() {
            final User originalCreator = LevelTestFixtures.createTestUser();
            this.cloneCreator = new User("user-2", "Luigi");
            this.original = new Level("Original", "Original description", originalCreator);
            final Position worldPosition = new Position(3, 4);
            final Position objectPosition = new Position(5, 6);
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.COIN), 5);
            LevelTestFixtures.publishTestLevel(this.original);
            this.original.setClearCondition(this.clearCondition);
            this.original.putWorldLayer(worldPosition, new GroundObject(21));
            this.original.putObjectLayer(objectPosition, new Coin(33, objectPosition, CoinType.GOLD_COIN));
            this.cloned = this.original.cloneFor(this.cloneCreator, "Cloned Title");
        }

       /** Test to ensure that a cloned level has all the exact states. */
       @Test
        @DisplayName("cloneFor copies the expected state into the clone")
        void clonedCopiesExpectedState() {
            assertAll(
                    () -> assertFalse(this.cloned.isPublished()),
                    () -> assertTrue(this.cloned.canBeModified()),
                    () -> assertSame(this.cloneCreator, this.cloned.getCreator()),
                    () -> assertEquals("Cloned Title", this.cloned.getTitle()),
                    () -> assertEquals(this.original.getDescription(), this.cloned.getDescription()),
                    () -> assertEquals(this.clearCondition, this.cloned.getClearCondition()),
                    () -> assertEquals(this.original.getWorldLayer(), this.cloned.getWorldLayer()),
                    () -> assertEquals(this.original.getObjectLayer(), this.cloned.getObjectLayer()));
        }

        /** Verify world layer deep copy. */
        @Test
        @DisplayName("cloneFor does not share the world layer")
        void doesNotShareWorldLayer() {
            final Position clonedOnlyPos = new Position(10, 2);
            this.cloned.putWorldLayer(clonedOnlyPos, new GroundObject(99));
            assertFalse(this.original.getWorldLayer().containsKey(clonedOnlyPos));
        }

        /** Verify object layer deep copy. */
        @Test
        @DisplayName("cloneFor does not share the object layer")
        void doesNotShareObjectLayer() {
            final Position clonedOnlyPos = new Position(11, 3);
            this.cloned.putObjectLayer(clonedOnlyPos, new StartFlag(77, clonedOnlyPos));
            assertFalse(this.original.getObjectLayer().containsKey(clonedOnlyPos));
        }
    }
}
