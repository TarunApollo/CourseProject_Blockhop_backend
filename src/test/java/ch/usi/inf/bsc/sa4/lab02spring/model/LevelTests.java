package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.junit.jupiter.api.Assertions;

import java.util.Collections;
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

/**
 * Unit tests for the {@link Level} class.
 */
@DisplayName(" In the Level class ")
@SuppressWarnings({"PMD.AtLeastOneConstructor", "NullAway"})
class LevelTests {

    /** The ID of a test user. */
    private static final String USER_1 = "user-1";

    /** The ID of a level owner. */
    private static final String OWNER_ID = "owner-id";

    /** The ID of another user. */
    private static final String OTHER_ID = "other-id";

    /**
     * Creates a test user.
     * 
     * @return a new User instance
     */
    private static User createTestUser() {
        return new User(USER_1, "Mario");
    }

    /**
     * Creates a level for a specific creator.
     * 
     * @param creator the user who creates the level
     * @return a new Level instance
     */
    private static Level createLevelFor(final User creator) {
        return new Level("Test level", "A level description", creator);
    }

    /**
     * Creates a standard test level.
     * 
     * @return a new Level instance
     */
    private static Level createTestLevel() {
        return createLevelFor(createTestUser());
    }

    /**
     * Publishes a test level by adding required objects and validating eligibility.
     * 
     * @param level the level to publish
     */
    private static void publishTestLevel(final Level level) {
        final Position flagPos = new Position(1, 1);
        final Position doorPos = new Position(2, 1);
        level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
        level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
        level.validatePublishEligible(USER_1);
        level.publish(USER_1);
    }

    /**
     * Test constructor/creation of a level.
     */
    @DisplayName(" can be created with title, description, and creator ")
    @Test
    void creatorTest() {
        final Executable codeToExecute = LevelTests::createTestLevel;
        Assertions.assertDoesNotThrow(codeToExecute);
    }

    /**
     * Tests for a newly created level.
     */
    @DisplayName(" when a level is newly created ")
    @Nested
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
            this.creator = createTestUser();
            this.level = createLevelFor(this.creator);
        }

        /** Verify title. */
        @DisplayName(" should have the correct title ")
        @Test
        void hasCorrectTitle() {
            Assertions.assertEquals(this.title, this.level.getTitle());
        }

        /** Verify description. */
        @DisplayName(" should have the correct description ")
        @Test
        void hasCorrectDescription() {
            Assertions.assertEquals(this.description, this.level.getDescription());
        }

        /** Verify creator. */
        @DisplayName(" should have the correct creator ")
        @Test
        void hasCorrectCreator() {
            Assertions.assertSame(this.creator, this.level.getCreator());
        }

        /** Verify unpublished status. */
        @DisplayName(" should be unpublished ")
        @Test
        void isNotPublished() {
            Assertions.assertFalse(this.level.isPublished());
        }

        /** Verify modifiable status. */
        @DisplayName(" should be modifiable ")
        @Test
        void isModifiable() {
            Assertions.assertTrue(this.level.canBeModified());
        }

        /** Verify publish eligibility. */
        @DisplayName(" should start as not publish eligible ")
        @Test
        void isNotPublishEligible() {
            Assertions.assertFalse(this.level.isPublishEligible());
        }

        /** Verify width. */
        @DisplayName(" should have the correct width ")
        @Test
        void hasCorrectWidth() {
            Assertions.assertEquals(256, this.level.getWidth());
        }

        /** Verify height. */
        @DisplayName(" should have the correct height ")
        @Test
        void hasCorrectHeight() {
            Assertions.assertEquals(14, this.level.getHeight());
        }

        /** Verify clear condition condition. */
        @DisplayName(" should start with no clear condition ")
        @Test
        void hasNoClearCondition() {
            Assertions.assertInstanceOf(Condition.NoClearCondition.class, this.level.getClearCondition().condition());
        }

        /** Verify clear condition target amount. */
        @DisplayName(" should start with zero target amount ")
        @Test
        void hasZeroTargetAmount() {
            Assertions.assertEquals(0, this.level.getClearCondition().targetAmount());
        }

        /** Verify empty object layer. */
        @DisplayName(" should start with an empty object layer ")
        @Test
        void startsWithEmptyObjectLayer() {
            Assertions.assertTrue(this.level.getObjectLayer().isEmpty());
        }

        /** Verify empty world layer. */
        @DisplayName(" should start with an empty world layer ")
        @Test
        void startsWithEmptyWorldLayer() {
            Assertions.assertTrue(this.level.getWorldLayer().isEmpty());
        }
    }

    /** Tests for setter methods. */
    @DisplayName(" methods setTitle, setDescription, and setClearCondition ")
    @Nested
    class Setters {

        /** The level instance. */
        private Level level;
        /** A clear condition for testing. */
        private ClearCondition clearCondition;

        /** Sets up setters test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.clearCondition = new ClearCondition(new Condition.SomeClearCondition(ClearConditionType.SLIME), 2);
        }

        /** Verify setTitle. */
        @DisplayName(" should update the title ")
        @Test
        void updatesTitle() {
            this.level.setTitle("New title");
            Assertions.assertEquals("New title", this.level.getTitle());
        }

        /** Verify setDescription. */
        @DisplayName(" should update the description ")
        @Test
        void updatesDescription() {
            this.level.setDescription("New description");
            Assertions.assertEquals("New description", this.level.getDescription());
        }

        /** Verify setClearCondition. */
        @DisplayName(" should update the clear condition ")
        @Test
        void updatesClearCondition() {
            this.level.setClearCondition(this.clearCondition);
            Assertions.assertSame(this.clearCondition, this.level.getClearCondition());
        }
    }

    /** Tests for ownership methods. */
    @DisplayName(" methods isOwnedBy and ensureOwnedBy ")
    @Nested
    class OwnershipMethods {

        /** The level instance. */
        private Level level;

        /** Sets up ownership test. */
        @BeforeEach
        void setUp() {
            this.level = createLevelFor(new User(OWNER_ID, "Mario"));
        }

        /** Verify isOwnedBy with ID. */
        @DisplayName(" should return true when user id matches the owner ")
        @Test
        void returnsTrueForOwnerId() {
            Assertions.assertTrue(this.level.isOwnedBy(OWNER_ID));
        }

        /** Verify isOwnedBy with different ID. */
        @DisplayName(" should return false when user id does not match the owner ")
        @Test
        void returnsFalseForOtherId() {
            Assertions.assertFalse(this.level.isOwnedBy(OTHER_ID));
        }

        /** Verify isOwnedBy with user instance. */
        @DisplayName(" should return true when user matches the owner ")
        @Test
        void returnsTrueForOwnerUser() {
            Assertions.assertTrue(this.level.isOwnedBy(new User(OWNER_ID, "Mario clone")));
        }

        /** Verify isOwnedBy with different user instance. */
        @DisplayName(" should return false when user does not match the owner ")
        @Test
        void returnsFalseForOtherUser() {
            Assertions.assertFalse(this.level.isOwnedBy(new User(OTHER_ID, "Luigi")));
        }

        /** Tests for ensureOwnedBy. */
        @DisplayName(" method ensureOwnedBy ")
        @Nested
        class EnsureOwnedByMethod {

            /** Verify ensureOwnedBy throws. */
            @DisplayName(" throws ForbiddenUserException ")
            @Test
            void throwsForbiddenUserException() {
                final Executable codeToExecute = () -> OwnershipMethods.this.level.ensureOwnedBy(OTHER_ID);
                Assertions.assertThrows(ForbiddenUserException.class, codeToExecute);
            }

            /** Verify ensureOwnedBy allowed for owner. */
            @DisplayName(" should not throw when the user owns the level ")
            @Test
            void allowsOwner() {
                final Executable codeToExecute = () -> OwnershipMethods.this.level.ensureOwnedBy(OWNER_ID);
                Assertions.assertDoesNotThrow(codeToExecute);
            }
        }
    }

    /** Tests for publication methods. */
    @DisplayName(" methods canBeModified and ensureModifiable ")
    @Nested
    class PublicationMethods {

        /** The level instance. */
        private Level level;

        /** Sets up publication test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        /** Verify canBeModified for unpublished. */
        @DisplayName(" should report modifiable when unpublished ")
        @Test
        void isModifiableWhenUnpublished() {
            Assertions.assertTrue(this.level.canBeModified());
        }

        /** Verify ensureModifiable for unpublished. */
        @DisplayName(" should not throw ensureModifiable when unpublished ")
        @Test
        void doesNotThrowWhenUnpublished() {
            Assertions.assertDoesNotThrow(() -> this.level.ensureModifiable());
        }

        /** Verify canBeModified for published. */
        @DisplayName(" should report not modifiable when published ")
        @Test
        void isNotModifiableWhenPublished() {
            publishTestLevel(this.level);
            Assertions.assertFalse(this.level.canBeModified());
        }

        /** Verify ensureModifiable for published. */
        @DisplayName(" should throw LevelPublishedException when published ")
        @Test
        void throwsWhenPublished() {
            publishTestLevel(this.level);
            Assertions.assertThrows(LevelPublishedException.class, () -> this.level.ensureModifiable());
        }
    }

    /** Tests for bounds methods. */
    @DisplayName(" methods isWithinBounds and ensureWithinBounds ")
    @Nested
    class BoundsMethods {

        /** The level instance. */
        private Level level;

        /** Sets up bounds test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        /** Verify top-left corner. */
        @DisplayName(" should accept position at top-left corner ")
        @Test
        void acceptsTopLeftCorner() {
            Assertions.assertTrue(this.level.isWithinBounds(new Position(0, 0)));
        }

        /** Verify bottom-right corner. */
        @DisplayName(" should accept position at bottom-right corner ")
        @Test
        void acceptsBottomRightCorner() {
            Assertions.assertTrue(this.level.isWithinBounds(new Position(255, 13)));
        }

        /** Verify negative X. */
        @DisplayName(" should reject position with negative x ")
        @Test
        void rejectsNegativeX() {
            Assertions.assertFalse(this.level.isWithinBounds(new Position(-1, 0)));
        }

        /** Verify X out of bounds. */
        @DisplayName(" should reject position with x equal to width ")
        @Test
        void rejectsXEqualToWidth() {
            Assertions.assertFalse(this.level.isWithinBounds(new Position(256, 0)));
        }

        /** Verify negative Y. */
        @DisplayName(" should reject position with negative y ")
        @Test
        void rejectsNegativeY() {
            Assertions.assertFalse(this.level.isWithinBounds(new Position(0, -1)));
        }

        /** Verify Y out of bounds. */
        @DisplayName(" should reject position with y equal to height ")
        @Test
        void rejectsYEqualToHeight() {
            Assertions.assertFalse(this.level.isWithinBounds(new Position(0, 14)));
        }

        /** Tests for ensureWithinBounds. */
        @DisplayName(" method ensureWithinBounds ")
        @Nested
        class EnsureWithinBoundsMethod {

            /** Verify null position. */
            @DisplayName(" throws IllegalArgumentException when position is null ")
            @Test
            void nullPosition() {
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> BoundsMethods.this.level.ensureWithinBounds(null));
            }

            /** Verify out of bounds position. */
            @DisplayName(" throws IllegalArgumentException when position is out of bounds ")
            @Test
            void outOfBoundsPosition() {
                final Position pos = new Position(256, 14);
                final Executable codeToExecute = () -> BoundsMethods.this.level.ensureWithinBounds(pos);
                Assertions.assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            /** Verify valid position. */
            @DisplayName(" should not throw when position is valid ")
            @Test
            void validPosition() {
                Assertions.assertDoesNotThrow(() -> BoundsMethods.this.level.ensureWithinBounds(new Position(255, 13)));
            }
        }
    }

    /** Tests for layer getters. */
    @DisplayName(" methods getObjectLayer and getWorldLayer ")
    @Nested
    class LayerGetters {

        /** The level instance. */
        private Level level;
        /** A position for testing. */
        private Position position;

        /** Sets up layer getters test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.position = new Position(1, 1);
            this.level.putWorldLayer(this.position, new GroundObject(8));
            this.level.putObjectLayer(this.position, new StartFlag(9, this.position));
        }

        /** Verify unmodifiable world layer. */
        @DisplayName(" should return an unmodifiable world layer ")
        @Test
        void returnsUnmodifiableWorldLayer() {
            final Position pos = new Position(2, 2);
            final GroundObject ground = new GroundObject(3);
            final Executable modifyWorldLayer = () -> this.level.getWorldLayer().put(pos, ground);
            Assertions.assertThrows(UnsupportedOperationException.class, modifyWorldLayer);
        }

        /** Verify unmodifiable object layer. */
        @DisplayName(" should return an unmodifiable object layer ")
        @Test
        void returnsUnmodifiableObjectLayer() {
            final Position pos = new Position(2, 2);
            final StartFlag flag = new StartFlag(4, pos);
            final Executable modifyObjectLayer = () -> this.level.getObjectLayer().put(pos, flag);
            Assertions.assertThrows(UnsupportedOperationException.class, modifyObjectLayer);
        }
    }

    /** Tests for layer mutation methods. */
    @DisplayName(" methods putObjectLayer, putWorldLayer, removeObjectLayer, and removeGroundObject ")
    @Nested
    class LayerMutationMethods {

        /** The level instance. */
        private Level level;
        /** A position for objects. */
        private Position objectPosition;
        /** A position for world objects. */
        private Position worldPosition;

        /** Sets up mutation test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.objectPosition = new Position(2, 3);
            this.worldPosition = new Position(4, 5);
        }

        /** Verify object replacement. */
        @DisplayName(" should replace an object layer entry ")
        @Test
        void replacesObjectLayerEntry() {
            final StartFlag firstObject = new StartFlag(10, this.objectPosition);
            final Coin replacementObject = new Coin(11, this.objectPosition, CoinType.BRONZE_COIN);
            this.level.putObjectLayer(this.objectPosition, firstObject);
            this.level.putObjectLayer(this.objectPosition, replacementObject);
            Assertions.assertSame(replacementObject, this.level.getObjectLayer().get(this.objectPosition));
        }

        /** Verify world replacement. */
        @DisplayName(" should replace a world layer entry ")
        @Test
        void replacesWorldLayerEntry() {
            final GroundObject firstGround = new GroundObject(20);
            final GroundObject replacementGround = new GroundObject(21);
            this.level.putWorldLayer(this.worldPosition, firstGround);
            this.level.putWorldLayer(this.worldPosition, replacementGround);
            Assertions.assertEquals(replacementGround, this.level.getWorldLayer().get(this.worldPosition));
        }

        /** Verify object removal. */
        @DisplayName(" should remove an object layer entry ")
        @Test
        void removesObjectLayerEntry() {
            this.level.putObjectLayer(this.objectPosition, new StartFlag(10, this.objectPosition));
            this.level.removeObjectLayer(this.objectPosition);
            Assertions.assertFalse(this.level.getObjectLayer().containsKey(this.objectPosition));
        }

        /** Verify world removal. */
        @DisplayName(" should remove a world layer entry ")
        @Test
        void removesWorldLayerEntry() {
            this.level.putWorldLayer(this.worldPosition, new GroundObject(20));
            this.level.removeGroundObject(this.worldPosition);
            Assertions.assertFalse(this.level.getWorldLayer().containsKey(this.worldPosition));
        }
    }

    /** Tests for the setWorldLayer method. */
    @DisplayName(" method setWorldLayer ")
    @Nested
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

        /** Sets up world layer replacement test. */
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

        /** Verify old entry removal. */
        @DisplayName(" should remove the first old entry from the layer ")
        @Test
        void removesFirstOldEntry() {
            this.level.setWorldLayer(this.newLayer);
            Assertions.assertFalse(this.level.getWorldLayer().containsKey(this.pos1));
        }

        /** Verify second old entry removal. */
        @DisplayName(" should remove the second old entry from the layer ")
        @Test
        void removesSecondOldEntry() {
            this.level.setWorldLayer(this.newLayer);
            Assertions.assertFalse(this.level.getWorldLayer().containsKey(this.pos2));
        }

        /** Verify new entry presence. */
        @DisplayName(" should contain the new entry in the layer ")
        @Test
        void containsNewEntry() {
            this.level.setWorldLayer(this.newLayer);
            Assertions.assertEquals(new GroundObject(10), this.level.getWorldLayer().get(this.newPos));
        }

        /** Verify clearing layer. */
        @DisplayName(" should clear the world layer when given an empty map ")
        @Test
        void clearsLayer() {
            this.level.setWorldLayer(new HashMap<>());
            Assertions.assertTrue(this.level.getWorldLayer().isEmpty());
        }
    }

    /** Tests for the setObjectLayer method. */
    @DisplayName(" method setObjectLayer ")
    @Nested
    class SetObjectLayerMethod {

        /** The level instance. */
        private Level level;
        /** Original position. */
        private Position pos;
        /** New position added to the replacement layer. */
        private Position newPos;
        /** Replacement object layer used in each test. */
        private Map<Position, GameObject> newLayer;

        /** Sets up object layer replacement test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.pos = new Position(1, 2);
            this.newPos = new Position(5, 6);
            this.newLayer = new HashMap<>();
            this.newLayer.put(this.newPos, new StartFlag(77, this.newPos));
            this.level.putObjectLayer(this.pos, new Coin(33, this.pos, CoinType.GOLD_COIN));
        }

        /** Verify old entry removal. */
        @DisplayName(" should remove the previous entry when replacing ")
        @Test
        void removesPreviousEntry() {
            this.level.setObjectLayer(this.newLayer);
            Assertions.assertFalse(this.level.getObjectLayer().containsKey(this.pos));
        }

        /** Verify new entry presence. */
        @DisplayName(" should contain the new entry after replacing ")
        @Test
        void containsNewEntry() {
            this.level.setObjectLayer(this.newLayer);
            Assertions.assertTrue(this.level.getObjectLayer().containsKey(this.newPos));
        }

        /** Verify clearing layer. */
        @DisplayName(" should clear the object layer when given an empty map ")
        @Test
        void clearsLayer() {
            this.level.setObjectLayer(new HashMap<>());
            Assertions.assertTrue(this.level.getObjectLayer().isEmpty());
        }
    }

    /** Tests for the cloneFor method. */
    @DisplayName(" method cloneFor ")
    @Nested
    class CloneForMethod {

        /** Original level. */
        private Level original;
        /** Creator for the clone. */
        private User cloneCreator;
        /** Position for world objects. */
        private Position worldPosition;
        /** Position for game objects. */
        private Position objectPosition;
        /** Clear condition. */
        private ClearCondition clearCondition;
        /** Cloned level created in setUp for use across all tests. */
        private Level cloned;

        /** Sets up clone test. */
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

        /** Verify clone is unpublished. */
        @DisplayName(" should create an unpublished copy ")
        @Test
        void clonedIsNotPublished() {
            Assertions.assertFalse(this.cloned.isPublished());
        }

        /** Verify clone is modifiable. */
        @DisplayName(" should create a modifiable copy ")
        @Test
        void clonedIsModifiable() {
            Assertions.assertTrue(this.cloned.canBeModified());
        }

        /** Verify clone creator. */
        @DisplayName(" should assign the new creator to the cloned level ")
        @Test
        void clonedHasNewCreator() {
            Assertions.assertSame(this.cloneCreator, this.cloned.getCreator());
        }

        /** Verify clone title. */
        @DisplayName(" should use the given title for the cloned level ")
        @Test
        void clonedHasNewTitle() {
            Assertions.assertEquals("Cloned Title", this.cloned.getTitle());
        }

        /** Verify clone description. */
        @DisplayName(" should copy the description to the cloned level ")
        @Test
        void clonedHasSameDescription() {
            Assertions.assertEquals(this.original.getDescription(), this.cloned.getDescription());
        }

        /** Verify clone clear condition. */
        @DisplayName(" should copy the clear condition to the cloned level ")
        @Test
        void clonedHasSameClearCondition() {
            Assertions.assertEquals(this.clearCondition, this.cloned.getClearCondition());
        }

        /** Verify clone world layer. */
        @DisplayName(" should copy the world layer to the cloned level ")
        @Test
        void clonedHasSameWorldLayer() {
            Assertions.assertEquals(this.original.getWorldLayer(), this.cloned.getWorldLayer());
        }

        /** Verify clone object layer. */
        @DisplayName(" should copy the object layer to the cloned level ")
        @Test
        void clonedHasSameObjectLayer() {
            Assertions.assertEquals(this.original.getObjectLayer(), this.cloned.getObjectLayer());
        }

        /** Verify world layer deep copy. */
        @DisplayName(" should not share the world layer with the original ")
        @Test
        void doesNotShareWorldLayer() {
            final Position clonedOnlyPos = new Position(10, 2);
            this.cloned.putWorldLayer(clonedOnlyPos, new GroundObject(99));
            Assertions.assertFalse(this.original.getWorldLayer().containsKey(clonedOnlyPos));
        }

        /** Verify object layer deep copy. */
        @DisplayName(" should not share the object layer with the original ")
        @Test
        void doesNotShareObjectLayer() {
            final Position clonedOnlyPos = new Position(11, 3);
            this.cloned.putObjectLayer(clonedOnlyPos, new StartFlag(77, clonedOnlyPos));
            Assertions.assertFalse(this.original.getObjectLayer().containsKey(clonedOnlyPos));
        }
    }

    /** Tests for publish method. */
    @DisplayName(" method publish ")
    @Nested
    class PublishMethod {

        /** The level instance. */
        private Level level;
        /** Flag position. */
        private Position flagPos;
        /** Door position. */
        private Position doorPos;

        /** Sets up publish test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.flagPos = new Position(1, 1);
            this.doorPos = new Position(2, 1);
        }

        /** Tests for unauthorized publish attempts. */
        @DisplayName(" throws ForbiddenUserException ")
        @Nested
        class WhenUserIsNotOwner {

            /** Verify wrong user. */
            @DisplayName(" when user does not own the level ")
            @Test
            void wrongUser() {
                final Executable codeToExecute = () -> PublishMethod.this.level.publish(OTHER_ID);
                Assertions.assertThrows(ForbiddenUserException.class, codeToExecute);
            }
        }

        /** Tests for invalid publish states. */
        @DisplayName(" throws ForbiddenLevelActionException ")
        @Nested
        class WhenCannotPublish {

            /** Verify no flag. */
            @DisplayName(" when object layer has no start flag ")
            @Test
            void noStartFlag() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos,
                        new ExitDoor(115, PublishMethod.this.doorPos));
                PublishMethod.this.level.validatePublishEligible(USER_1);
                final Executable codeToExecute = () -> PublishMethod.this.level.publish(USER_1);
                Assertions.assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /** Verify no door. */
            @DisplayName(" when object layer has no exit door ")
            @Test
            void noExitDoor() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos,
                        new StartFlag(68, PublishMethod.this.flagPos));
                PublishMethod.this.level.validatePublishEligible(USER_1);
                final Executable codeToExecute = () -> PublishMethod.this.level.publish(USER_1);
                Assertions.assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /** Verify not eligible. */
            @DisplayName(" when level is not publish eligible ")
            @Test
            void notPublishEligible() {
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.flagPos,
                        new StartFlag(68, PublishMethod.this.flagPos));
                PublishMethod.this.level.putObjectLayer(PublishMethod.this.doorPos,
                        new ExitDoor(115, PublishMethod.this.doorPos));
                final Executable codeToExecute = () -> PublishMethod.this.level.publish(USER_1);
                Assertions.assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }
        }

        /** Tests for valid publish. */
        @DisplayName(" when publishing is valid ")
        @Nested
        class WhenValid {

            /** Verify published status. */
            @DisplayName(" should mark the level as published ")
            @Test
            void marksAsPublished() {
                publishTestLevel(PublishMethod.this.level);
                Assertions.assertTrue(PublishMethod.this.level.isPublished());
            }
        }
    }

    /** Tests for validatePublishEligible. */
    @DisplayName(" method validatePublishEligible ")
    @Nested
    class ValidatePublishEligibleMethod {

        /** The level instance. */
        private Level level;

        /** Sets up validation test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        /** Verify wrong user. */
        @DisplayName(" throws ForbiddenUserException when user does not own the level ")
        @Test
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.validatePublishEligible(OTHER_ID);
            Assertions.assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        /** Verify successful validation. */
        @DisplayName(" should set publish eligible to true ")
        @Test
        void setsPublishEligible() {
            this.level.validatePublishEligible(USER_1);
            Assertions.assertTrue(this.level.isPublishEligible());
        }
    }

    /** Tests for invalidatePublishEligible. */
    @DisplayName(" method invalidatePublishEligible ")
    @Nested
    class InvalidatePublishEligibleMethod {

        /** The level instance. */
        private Level level;

        /** Sets up invalidation test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.level.validatePublishEligible(USER_1);
        }

        /** Verify wrong user. */
        @DisplayName(" throws ForbiddenUserException when user does not own the level ")
        @Test
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.invalidatePublishEligible(OTHER_ID);
            Assertions.assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        /** Verify successful invalidation. */
        @DisplayName(" should set publish eligible to false ")
        @Test
        void setsPublishIneligible() {
            this.level.invalidatePublishEligible(USER_1);
            Assertions.assertFalse(this.level.isPublishEligible());
        }
    }

    /** Tests for unpublish method. */
    @DisplayName(" method unpublish ")
    @Nested
    class UnpublishMethod {

        /** The level instance. */
        private Level level;

        /** Sets up unpublish test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            publishTestLevel(this.level);
        }

        /** Verify wrong user. */
        @DisplayName(" throws ForbiddenUserException when user does not own the level ")
        @Test
        void wrongUser() {
            final Executable codeToExecute = () -> this.level.unpublish(OTHER_ID);
            Assertions.assertThrows(ForbiddenUserException.class, codeToExecute);
        }

        /** Verify successful unpublish. */
        @DisplayName(" should mark the level as unpublished ")
        @Test
        void marksAsUnpublished() {
            this.level.unpublish(USER_1);
            Assertions.assertFalse(this.level.isPublished());
        }

        /** Verify idempotency. */
        @DisplayName(" should not throw when called on an already unpublished level ")
        @Test
        void doesNotThrowWhenCalledTwice() {
            this.level.unpublish(USER_1);
            final Executable codeToExecute = () -> this.level.unpublish(USER_1);
            Assertions.assertDoesNotThrow(codeToExecute);
        }

        /** Verify status after second call. */
        @DisplayName(" should remain unpublished after a second unpublish call ")
        @Test
        void remainsUnpublishedAfterSecondCall() {
            this.level.unpublish(USER_1);
            this.level.unpublish(USER_1);
            Assertions.assertFalse(this.level.isPublished());
        }
    }

    /** Tests for ensureValidObjectLayer. */
    @DisplayName(" method ensureValidObjectLayer ")
    @Nested
    class EnsureValidObjectLayerMethod {

        /** The level instance. */
        private Level level;

        /** Sets up validation test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        /** Tests for invalid object layer. */
        @DisplayName(" throws IllegalArgumentException ")
        @Nested
        class WhenObjectLayerIsInvalid {

            /** Verify multiple flags. */
            @DisplayName(" when there are more than one start flags ")
            @Test
            void moreThanOneFlag() {
                final Position pos1 = new Position(1, 1);
                final Position pos2 = new Position(3, 1);
                final Map<Position, GameObject> layer = new HashMap<>();
                layer.put(pos1, new StartFlag(68, pos1));
                layer.put(pos2, new StartFlag(68, pos2));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level
                        .ensureValidObjectLayer(Collections.unmodifiableMap(layer));
                Assertions.assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            /** Verify multiple doors. */
            @DisplayName(" when there are more than one exit doors ")
            @Test
            void moreThanOneDoor() {
                final Position pos1 = new Position(1, 1);
                final Position pos2 = new Position(3, 1);
                final Map<Position, GameObject> layer = new HashMap<>();
                layer.put(pos1, new ExitDoor(115, pos1));
                layer.put(pos2, new ExitDoor(115, pos2));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level
                        .ensureValidObjectLayer(layer);
                Assertions.assertThrows(IllegalArgumentException.class, codeToExecute);
            }
        }

        /** Tests for valid object layer. */
        @DisplayName(" when object layer is valid ")
        @Nested
        class WhenObjectLayerIsValid {

            /** Verify empty layer. */
            @DisplayName(" should not throw for an empty layer ")
            @Test
            void emptyLayer() {
                final Map<Position, GameObject> layer = new HashMap<>();
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level
                        .ensureValidObjectLayer(layer);
                Assertions.assertDoesNotThrow(codeToExecute);
            }

            /** Verify valid layer. */
            @DisplayName(" should not throw for one flag and one door ")
            @Test
            void oneFlagOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos = new Position(2, 1);
                final Map<Position, GameObject> layer = new HashMap<>();
                layer.put(flagPos, new StartFlag(68, flagPos));
                layer.put(doorPos, new ExitDoor(115, doorPos));
                final Executable codeToExecute = () -> EnsureValidObjectLayerMethod.this.level
                        .ensureValidObjectLayer(layer);
                Assertions.assertDoesNotThrow(codeToExecute);
            }
        }
    }

    /** Tests for ensurePublishableObjectLayer. */
    @DisplayName(" method ensurePublishableObjectLayer ")
    @Nested
    class EnsurePublishableObjectLayerMethod {

        /** The level instance. */
        private Level level;

        /** Sets up validation test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        /** Tests for non-publishable layers. */
        @DisplayName(" throws ForbiddenLevelActionException ")
        @Nested
        class WhenObjectLayerIsNotPublishable {

            /** Verify missing flag. */
            @DisplayName(" when there are no start flags ")
            @Test
            void noFlag() {
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                Assertions.assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /** Verify missing door. */
            @DisplayName(" when there are no exit doors ")
            @Test
            void noDoor() {
                final Position flagPos = new Position(1, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                Assertions.assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /** Verify multiple flags. */
            @DisplayName(" when there are more than one start flags ")
            @Test
            void moreThanOneFlag() {
                final Position flagPos1 = new Position(1, 1);
                final Position flagPos2 = new Position(3, 1);
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos1, new StartFlag(68, flagPos1));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos2, new StartFlag(68, flagPos2));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                Assertions.assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }

            /** Verify multiple doors. */
            @DisplayName(" when there are more than one exit doors ")
            @Test
            void moreThanOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos1 = new Position(2, 1);
                final Position doorPos2 = new Position(4, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos1, new ExitDoor(115, doorPos1));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos2, new ExitDoor(115, doorPos2));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                Assertions.assertThrows(ForbiddenLevelActionException.class, codeToExecute);
            }
        }

        /** Tests for publishable layers. */
        @DisplayName(" when object layer is publishable ")
        @Nested
        class WhenObjectLayerIsPublishable {

            /** Verify valid layer. */
            @DisplayName(" should not throw when there is exactly one flag and one door ")
            @Test
            void oneFlagOneDoor() {
                final Position flagPos = new Position(1, 1);
                final Position doorPos = new Position(2, 1);
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(flagPos, new StartFlag(68, flagPos));
                EnsurePublishableObjectLayerMethod.this.level.putObjectLayer(doorPos, new ExitDoor(115, doorPos));
                final Executable codeToExecute = () -> EnsurePublishableObjectLayerMethod.this.level
                        .ensurePublishableObjectLayer();
                Assertions.assertDoesNotThrow(codeToExecute);
            }
        }
    }

    /** Tests for ensurePlayable. */
    @DisplayName(" method ensurePlayable ")
    @Nested
    class EnsurePlayableMethod {

        /** The level instance. */
        private Level level;

        /** Sets up playability test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
        }

        /** Verify unpublished playability for non-owner. */
        @DisplayName(" throws LevelNotPlayableException when level is unpublished and user is not the owner ")
        @Test
        void unpublishedNotOwner() {
            final Executable codeToExecute = () -> this.level.ensurePlayable(OTHER_ID);
            Assertions.assertThrows(LevelNotPlayableException.class, codeToExecute);
        }

        /** Verify unpublished playability for owner. */
        @DisplayName(" should not throw when level is unpublished but user is the owner ")
        @Test
        void unpublishedOwner() {
            final Executable codeToExecute = () -> this.level.ensurePlayable(USER_1);
            Assertions.assertDoesNotThrow(codeToExecute);
        }

        /** Verify published playability. */
        @DisplayName(" should not throw when level is published regardless of user ")
        @Test
        void publishedLevel() {
            publishTestLevel(this.level);
            final Executable codeToExecute = () -> this.level.ensurePlayable(OTHER_ID);
            Assertions.assertDoesNotThrow(codeToExecute);
        }
    }

    /** Tests for ensureObjectCanBePlacedAt. */
    @DisplayName(" method ensureObjectCanBePlacedAt ")
    @Nested
    class EnsureObjectCanBePlacedAtMethod {

        /** The level instance. */
        private Level level;
        /** A position for testing. */
        private Position position;

        /** Sets up placement test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.position = new Position(3, 3);
        }

        /** Tests for occupied positions. */
        @DisplayName(" throws ObjectPlacementConflictException ")
        @Nested
        class WhenPositionIsOccupied {

            /** Verify world layer occupancy. */
            @DisplayName(" when world layer has an object at the position ")
            @Test
            void worldLayerOccupied() {
                EnsureObjectCanBePlacedAtMethod.this.level.putWorldLayer(EnsureObjectCanBePlacedAtMethod.this.position,
                        new GroundObject(5));
                final Executable codeToExecute = () -> EnsureObjectCanBePlacedAtMethod.this.level
                        .ensureObjectCanBePlacedAt(EnsureObjectCanBePlacedAtMethod.this.position);
                Assertions.assertThrows(ObjectPlacementConflictException.class, codeToExecute);
            }

            /** Verify object layer occupancy. */
            @DisplayName(" when object layer has an object at the position ")
            @Test
            void objectLayerOccupied() {
                EnsureObjectCanBePlacedAtMethod.this.level.putObjectLayer(EnsureObjectCanBePlacedAtMethod.this.position,
                        new StartFlag(68, EnsureObjectCanBePlacedAtMethod.this.position));
                final Executable codeToExecute = () -> EnsureObjectCanBePlacedAtMethod.this.level
                        .ensureObjectCanBePlacedAt(EnsureObjectCanBePlacedAtMethod.this.position);
                Assertions.assertThrows(ObjectPlacementConflictException.class, codeToExecute);
            }
        }

        /** Verify empty position. */
        @DisplayName(" should not throw when both layers are empty at the position ")
        @Test
        void bothLayersEmpty() {
            final Executable codeToExecute = () -> this.level.ensureObjectCanBePlacedAt(this.position);
            Assertions.assertDoesNotThrow(codeToExecute);
        }
    }

    /** Tests for updateBoxContent. */
    @DisplayName(" method updateBoxContent ")
    @Nested
    class UpdateBoxContentMethod {

        /** The level instance. */
        private Level level;
        /** Valid position. */
        private Position validPosition;
        /** Invalid position. */
        private Position outOfBoundsPosition;

        /** Sets up box content update test. */
        @BeforeEach
        void setUp() {
            this.level = createTestLevel();
            this.validPosition = new Position(5, 5);
            this.outOfBoundsPosition = new Position(256, 14);
        }

        /** Tests for invalid updates. */
        @DisplayName(" throws IllegalArgumentException ")
        @Nested
        class WhenPositionOrObjectIsInvalid {

            /** Verify out of bounds. */
            @DisplayName(" when position is out of bounds ")
            @Test
            void outOfBounds() {
                final Content noContent = new Content.NoContent();
                final Executable codeToExecute = () -> UpdateBoxContentMethod.this.level
                        .updateBoxContent(UpdateBoxContentMethod.this.outOfBoundsPosition, noContent);
                Assertions.assertThrows(IllegalArgumentException.class, codeToExecute);
            }

            /** Verify not a box. */
            @DisplayName(" when the object at the position is not a box ")
            @Test
            void notABox() {
                UpdateBoxContentMethod.this.level.putObjectLayer(UpdateBoxContentMethod.this.validPosition,
                        new StartFlag(68, UpdateBoxContentMethod.this.validPosition));
                final Content noContent = new Content.NoContent();
                final Executable codeToExecute = () -> UpdateBoxContentMethod.this.level
                        .updateBoxContent(UpdateBoxContentMethod.this.validPosition, noContent);
                Assertions.assertThrows(IllegalArgumentException.class, codeToExecute);
            }
        }

        /** Verify missing object. */
        @DisplayName(" throws NoSuchElementException when no object exists at the position ")
        @Test
        void noObjectAtPosition() {
            final Content noContent = new Content.NoContent();
            final Executable codeToExecute = () -> this.level.updateBoxContent(this.validPosition, noContent);
            Assertions.assertThrows(NoSuchElementException.class, codeToExecute);
        }

        /** Verify box instance. */
        @DisplayName(" updated object should be a Box ")
        @Test
        void updatedObjectIsBox() {
            final Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            this.level.putObjectLayer(this.validPosition, new Box(42, this.validPosition, new Content.NoContent()));
            this.level.updateBoxContent(this.validPosition, newContent);
            Assertions.assertInstanceOf(Box.class, this.level.getObjectLayer().get(this.validPosition));
        }

        /** Verify box content update. */
        @DisplayName(" updated box should have the new content ")
        @Test
        void updatedBoxHasNewContent() {
            final Content newContent = new Content.SomeContent(CoinType.GOLD_COIN);
            this.level.putObjectLayer(this.validPosition, new Box(42, this.validPosition, new Content.NoContent()));
            this.level.updateBoxContent(this.validPosition, newContent);
            Assertions.assertEquals(newContent, ((Box) this.level.getObjectLayer().get(this.validPosition)).content());
        }
    }
}
