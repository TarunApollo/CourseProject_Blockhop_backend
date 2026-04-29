package ch.usi.inf.bsc.sa4.lab02spring.model.level;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;

/**
 * Tests covering layer getters and layer replacement operations.
 */
@DisplayName("In the Level layer access and replacement API")
@SuppressWarnings({ "PMD.TooManyStaticImports", "java:S2187" })
class LevelLayerAccessTests {

    /**
     * Tests for layer getters.
     */
    @Nested
    @DisplayName("methods getObjectLayer and getWorldLayer")
    class LayerGetters {

        /** The level instance. */
        private Level level;

        /** Sets up layer getters tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            final Position position = new Position(1, 1);
            this.level.putWorldLayer(position, new GroundObject(8));
            this.level.putObjectLayer(position, new StartFlag(9, position));
        }

        /** Verify unmodifiable world layer. */
        @Test
        @DisplayName("getWorldLayer returns an unmodifiable world layer")
        void returnsUnmodifiableWorldLayer() {
            final Position pos = new Position(2, 2);
            final GroundObject ground = new GroundObject(3);
            final Executable modifyWorldLayer = () -> this.level.getWorldLayer().put(pos, ground);
            assertThrows(UnsupportedOperationException.class, modifyWorldLayer);
        }

        /** Verify unmodifiable object layer. */
        @Test
        @DisplayName("getObjectLayer returns an unmodifiable object layer")
        void returnsUnmodifiableObjectLayer() {
            final Position pos = new Position(2, 2);
            final StartFlag flag = new StartFlag(4, pos);
            final Executable modifyObjectLayer = () -> this.level.getObjectLayer().put(pos, flag);
            assertThrows(UnsupportedOperationException.class, modifyObjectLayer);
        }
    }

    /**
     * Tests for layer mutation methods.
     */
    @Nested
    @DisplayName("methods putObjectLayer, putWorldLayer, removeObjectLayer, and removeGroundObject")
    class LayerMutationMethods {

        /** The level instance. */
        private Level level;
        /** A position for objects. */
        private Position objectPosition;
        /** A position for world objects. */
        private Position worldPosition;

        /** Sets up mutation tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            this.objectPosition = new Position(2, 3);
            this.worldPosition = new Position(4, 5);
        }

        /** Verify object replacement. */
        @Test
        @DisplayName("putObjectLayer replaces an object layer entry")
        void replacesObjectLayerEntry() {
            final StartFlag firstObject = new StartFlag(10, this.objectPosition);
            final Coin replacementObject = new Coin(11, this.objectPosition, CoinType.BRONZE_COIN);
            this.level.putObjectLayer(this.objectPosition, firstObject);
            this.level.putObjectLayer(this.objectPosition, replacementObject);
            assertSame(replacementObject, this.level.getObjectLayer().get(this.objectPosition));
        }

        /** Verify world replacement. */
        @Test
        @DisplayName("putWorldLayer replaces a world layer entry")
        void replacesWorldLayerEntry() {
            final GroundObject firstGround = new GroundObject(20);
            final GroundObject replacementGround = new GroundObject(21);
            this.level.putWorldLayer(this.worldPosition, firstGround);
            this.level.putWorldLayer(this.worldPosition, replacementGround);
            assertEquals(replacementGround, this.level.getWorldLayer().get(this.worldPosition));
        }

        /** Verify object removal. */
        @Test
        @DisplayName("removeObjectLayer removes an object layer entry")
        void removesObjectLayerEntry() {
            this.level.putObjectLayer(this.objectPosition, new StartFlag(10, this.objectPosition));
            this.level.removeObjectLayer(this.objectPosition);
            assertFalse(this.level.getObjectLayer().containsKey(this.objectPosition));
        }

        /** Verify world removal. */
        @Test
        @DisplayName("removeGroundObject removes a world layer entry")
        void removesWorldLayerEntry() {
            this.level.putWorldLayer(this.worldPosition, new GroundObject(20));
            this.level.removeGroundObject(this.worldPosition);
            assertFalse(this.level.getWorldLayer().containsKey(this.worldPosition));
        }
    }

    /**
     * Tests for the setWorldLayer method.
     */
    @Nested
    @DisplayName("method setWorldLayer")
    class SetWorldLayerMethod {

        /** The level instance. */
        private Level level;
        /** First position. */
        private Position pos1;
        /** Second position. */
        private Position pos2;
        /** New position added to the replacement layer. */
        private Position newPos;
        /** Replacement world layer used in each test. */
        private Map<Position, GroundObject> newLayer;

        /** Sets up world layer replacement tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            this.pos1 = new Position(1, 2);
            this.pos2 = new Position(3, 4);
            this.newPos = new Position(7, 8);
            this.newLayer = Map.of(this.newPos, new GroundObject(10));
            this.level.putWorldLayer(this.pos1, new GroundObject(5));
            this.level.putWorldLayer(this.pos2, new GroundObject(6));
        }

        /** Verify old entry removal. */
        @Test
        @DisplayName("setWorldLayer removes the first old entry")
        void removesFirstOldEntry() {
            this.level.setWorldLayer(this.newLayer);
            assertFalse(this.level.getWorldLayer().containsKey(this.pos1));
        }

        /** Verify second old entry removal. */
        @Test
        @DisplayName("setWorldLayer removes the second old entry")
        void removesSecondOldEntry() {
            this.level.setWorldLayer(this.newLayer);
            assertFalse(this.level.getWorldLayer().containsKey(this.pos2));
        }

        /** Verify new entry presence. */
        @Test
        @DisplayName("setWorldLayer keeps the new entry")
        void containsNewEntry() {
            this.level.setWorldLayer(this.newLayer);
            assertEquals(new GroundObject(10), this.level.getWorldLayer().get(this.newPos));
        }

        /** Verify clearing layer. */
        @Test
        @DisplayName("setWorldLayer clears the world layer when given an empty map")
        void clearsLayer() {
            this.level.setWorldLayer(Map.of());
            assertTrue(this.level.getWorldLayer().isEmpty());
        }
    }

    /**
     * Tests for the setObjectLayer method.
     */
    @Nested
    @DisplayName("method setObjectLayer")
    class SetObjectLayerMethod {

        /** The level instance. */
        private Level level;
        /** Original position. */
        private Position pos;
        /** New position added to the replacement layer. */
        private Position newPos;
        /** Replacement object layer used in each test. */
        private Map<Position, GameObject> newLayer;

        /** Sets up object layer replacement tests. */
        @BeforeEach
        void setUp() {
            this.level = LevelTestFixtures.createTestLevel();
            this.pos = new Position(1, 2);
            this.newPos = new Position(5, 6);
            this.newLayer = Map.of(this.newPos, new StartFlag(77, this.newPos));
            this.level.putObjectLayer(this.pos, new Coin(33, this.pos, CoinType.GOLD_COIN));
        }

        /** Verify old entry removal. */
        @Test
        @DisplayName("setObjectLayer removes the previous entry when replacing")
        void removesPreviousEntry() {
            this.level.setObjectLayer(this.newLayer);
            assertFalse(this.level.getObjectLayer().containsKey(this.pos));
        }

        /** Verify new entry presence. */
        @Test
        @DisplayName("setObjectLayer contains the new entry after replacing")
        void containsNewEntry() {
            this.level.setObjectLayer(this.newLayer);
            assertTrue(this.level.getObjectLayer().containsKey(this.newPos));
        }

        /** Verify clearing layer. */
        @Test
        @DisplayName("setObjectLayer clears the object layer when given an empty map")
        void clearsLayer() {
            this.level.setObjectLayer(Map.of());
            assertTrue(this.level.getObjectLayer().isEmpty());
        }
    }
}
