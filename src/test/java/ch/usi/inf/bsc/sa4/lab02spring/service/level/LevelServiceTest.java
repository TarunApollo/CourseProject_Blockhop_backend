package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CloneLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreatedLevelProfileDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.UpdateLevelDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearConditionType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.AttemptRepository;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelPublishedException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

/// Unit tests for the level CRUD service.
/// Verifies create, clone, update, delete, and listing semantics.
@SpringBootTest
@DisplayName("The Level Service")
// @SuppressWarnings({"NullAway", "PMD.ExcessiveImports"})
class LevelServiceTests {

    /// Identifier of the level under test.
    private static final String LEVEL_ID = "level-1";
    /// Identifier of the level owner used by fixtures.
    private static final String OWNER_ID = "owner-1";
    /// Identifier of a non-owner user used by fixtures.
    private static final String OTHER_USER_ID = "other-1";
    /// Display name of the owner user.
    private static final String OWNER_NAME = "Mario";
    /// Display name of the non-owner user.
    private static final String OTHER_NAME = "Luigi";
    /// Default level description used by fixtures.
    private static final String DEFAULT_DESC = "desc";
    /// Default level title used by fixtures.
    private static final String DEFAULT_TITLE = "title";
    /// Sample level title used by createLevel tests.
    private static final String SAMPLE_TITLE = "My Level";
    /// Sample level description used by createLevel tests.
    private static final String SAMPLE_DESC = "Description";
    /// DTO used to create a level.
    private static final CreateLevelDTO CREATE_LEVEL_DTO =
            new CreateLevelDTO(SAMPLE_TITLE, SAMPLE_DESC);
    /// Old level title used by update tests.
    private static final String OLD_TITLE = "old-title";
    /// New level title used by update tests.
    private static final String NEW_TITLE = "new-title";
    /// New level description used by update tests.
    private static final String NEW_DESC = "new-desc";
    /// Title of the first clone source level.
    private static final String ADVENTURE_TITLE = "Adventure";
    /// Expected title of the first cloned adventure level.
    private static final String ADVENTURE_FIRST_CLONE_TITLE = "Adventure (2)";
    /// Root title used by clone collision tests.
    private static final String QUEST_TITLE = "Quest";
    /// Existing second quest clone title.
    private static final String QUEST_SECOND_CLONE_TITLE = "Quest (2)";
    /// Existing third quest clone title.
    private static final String QUEST_THIRD_CLONE_TITLE = "Quest (3)";
    /// Expected next quest clone title.
    private static final String QUEST_FOURTH_CLONE_TITLE = "Quest (4)";
    /// Clone source title that already has a numeric suffix.
    private static final String MAZE_FIFTH_CLONE_TITLE = "Maze (5)";
    /// Expected clone title after stripping the old maze suffix.
    private static final String MAZE_SECOND_CLONE_TITLE = "Maze (2)";
    /// First short title used by listing tests.
    private static final String LIST_TITLE_A = "a";
    /// Second short title used by listing tests.
    private static final String LIST_TITLE_B = "b";
    /// Old level description used by update tests.
    private static final String OLD_DESC = "old-desc";
    /// Short new title used by failure-path update tests.
    private static final String SHORT_NEW_TITLE = "new";
    /// Short title used when invalidating publish eligibility.
    private static final String INVALIDATING_TITLE = "x";

    /// Service under test.
    @Autowired
    private LevelService service;

    /// Mocked level repository providing per-test fixtures.
    @MockitoBean
    private LevelRepository levelRepository;
    /// Mocked attempt repository for play and completion counts.
    @MockitoBean
    private AttemptRepository attemptRepository;
    /// Mocked user service for resolving the level creator.
    @MockitoBean
    private UserService userService;

    /// Shared owner user fixture.
    private User owner;
    /// Shared non-owner user fixture.
    private User otherUser;
    /// Expected level created from the create-level DTO.
    private Level expectedCreatedLevel;

    /// Initializes shared user fixtures before each test.
    @BeforeEach
    void setUp() {
        owner = new User(OWNER_ID, OWNER_NAME);
        otherUser = new User(OTHER_USER_ID, OTHER_NAME);
        expectedCreatedLevel = new Level(SAMPLE_TITLE, SAMPLE_DESC, owner);
    }

    /// Builds a level with the given title owned by the given creator.
    private Level newLevel(final String title, final User creator) {
        return new Level(title, DEFAULT_DESC, creator);
    }

    /// Builds a level with one start flag and one exit door so publish() can succeed.
    private Level publishableLevel() {
        final Level level = newLevel(DEFAULT_TITLE, owner);
        final Position flag = new Position(1, 1);
        final Position door = new Position(2, 1);
        level.putObjectLayer(flag, new StartFlag(68, flag));
        level.putObjectLayer(door, new ExitDoor(115, door));
        return level;
    }

    /// Sanity test so static analyzers see at least one top-level @Test on the class.
    @Test
    @DisplayName("test fixture initializes the service")
    void serviceWired() {
        Assertions.assertNotNull(service);
    }

    // ====================================================================
    // createLevel
    // ====================================================================

    /// Tests for the createLevel entry point.
    @Nested
    @DisplayName("createLevel")
    class CreateLevel {

        /// Missing user should surface as UserNotFoundException.
        @Test
        @DisplayName("throws UserNotFoundException when the user does not exist")
        void userNotFound() {
            Mockito.when(userService.getById(OWNER_ID)).thenReturn(Optional.empty());
            Assertions.assertThrows(UserNotFoundException.class,
                () -> service.createLevel(CREATE_LEVEL_DTO, OWNER_ID));

            Mockito.verify(levelRepository, Mockito.never()).save(Mockito.any());
        }

        /// Verifies that the DTO is mapped to a level, saved, and returned.
        @Test
        @DisplayName("maps DTO to Level, saves it, and returns the result")
        void createdLevelIsMappedSavedAndReturned() {
            Mockito.when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            Mockito.when(levelRepository.save(Mockito.refEq(expectedCreatedLevel)))
                    .thenReturn(expectedCreatedLevel);

            final Level result = service.createLevel(CREATE_LEVEL_DTO, OWNER_ID);

            Assertions.assertSame(expectedCreatedLevel, result);
        }
    }

    // ====================================================================
    // cloneLevel
    // ====================================================================

    /// Tests for the cloneLevel entry point.
    @Nested
    @DisplayName("cloneLevel")
    class CloneLevel {

        /// Missing source level should yield an empty Optional.
        @Test
        @DisplayName("returns empty when the source level does not exist")
        void sourceNotFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            Assertions.assertTrue(result.isEmpty());
        }

        /// Missing source level should not persist anything.
        @Test
        @DisplayName("does not persist when the source level does not exist")
        void sourceNotFoundSkipsSave() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            Mockito.verify(levelRepository, Mockito.never()).save(ArgumentMatchers.<Level>any());
        }

        /// Source level not owned by the user should yield an empty Optional.
        @Test
        @DisplayName("returns empty when the source level is not owned by the requesting user")
        void notOwnedReturnsEmpty() {
            final Level source = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), otherUser);

            Assertions.assertTrue(result.isEmpty());
        }

        /// Source level not owned by the user should not persist anything.
        @Test
        @DisplayName("does not persist when the source level is not owned by the requesting user")
        void notOwnedSkipsSave() {
            final Level source = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));

            service.cloneLevel(new CloneLevelDTO(LEVEL_ID), otherUser);

            Mockito.verify(levelRepository, Mockito.never()).save(ArgumentMatchers.<Level>any());
        }

        /// First clone of a level should be titled '<original> (2)'.
        @Test
        @DisplayName("clones with title 'X (2)' when no other clones exist")
        void firstCloneSuffix() {
            final Level source = newLevel(ADVENTURE_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            Mockito.when(levelRepository.findByCreator(owner)).thenReturn(List.of(source));
            Mockito.when(levelRepository.save(Mockito.argThat(savedLevel ->
                    ADVENTURE_FIRST_CLONE_TITLE.equals(savedLevel.getTitle())
                            && owner.equals(savedLevel.getCreator())
            ))).thenAnswer(inv -> inv.getArgument(0));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            Assertions.assertEquals(ADVENTURE_FIRST_CLONE_TITLE, result.orElseThrow().getTitle());
        }

        /// Subsequent clones should pick the next free index.
        @Test
        @DisplayName("picks the next free index when previous clones already exist")
        void picksNextAvailableSuffix() {
            final Level source = newLevel(QUEST_TITLE, owner);
            final Level clone2 = newLevel(QUEST_SECOND_CLONE_TITLE, owner);
            final Level clone3 = newLevel(QUEST_THIRD_CLONE_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            Mockito.when(levelRepository.findByCreator(owner)).thenReturn(List.of(source, clone2, clone3));
            Mockito.when(levelRepository.save(Mockito.argThat(savedLevel ->
                    QUEST_FOURTH_CLONE_TITLE.equals(savedLevel.getTitle())
                            && owner.equals(savedLevel.getCreator())
            ))).thenAnswer(inv -> inv.getArgument(0));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            Assertions.assertEquals(QUEST_FOURTH_CLONE_TITLE, result.orElseThrow().getTitle());
        }

        /// Cloning a level that already has a (n) suffix should strip the suffix first.
        @Test
        @DisplayName("strips an existing (n) suffix from the source title before computing a new one")
        void stripsExistingSuffix() {
            final Level source = newLevel(MAZE_FIFTH_CLONE_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            Mockito.when(levelRepository.findByCreator(owner)).thenReturn(List.of(source));
            Mockito.when(levelRepository.save(Mockito.argThat(savedLevel ->
                    MAZE_SECOND_CLONE_TITLE.equals(savedLevel.getTitle())
                            && owner.equals(savedLevel.getCreator())
            ))).thenAnswer(inv -> inv.getArgument(0));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            // "Maze (5)" → root "Maze" → next free is "Maze (2)"
            Assertions.assertEquals(MAZE_SECOND_CLONE_TITLE, result.orElseThrow().getTitle());
        }
    }

    // ====================================================================
    // deleteLevel
    // ====================================================================

    /// Tests for the deleteLevel entry point.
    @Nested
    @DisplayName("deleteLevel")
    class DeleteLevel {

        /// Missing level should surface as LevelNotFoundException.
        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
        }

        /// Missing level should skip the delete.
        @Test
        @DisplayName("does not delete when the level does not exist")
        void levelNotFoundSkipsDelete() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
            Mockito.verify(levelRepository, Mockito.never()).deleteById(LEVEL_ID);
        }

        /// Non-owner cannot delete.
        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to delete")
        void nonOwnerCannotDelete() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(ForbiddenUserException.class,
                () -> service.deleteLevel(OTHER_USER_ID, LEVEL_ID));
        }

        /// Non-owner failure should not delete.
        @Test
        @DisplayName("does not delete when a non-owner attempt fails")
        void nonOwnerFailureSkipsDelete() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(ForbiddenUserException.class,
                () -> service.deleteLevel(OTHER_USER_ID, LEVEL_ID));
            Mockito.verify(levelRepository, Mockito.never()).deleteById(LEVEL_ID);
        }

        /// Published levels cannot be deleted.
        @Test
        @DisplayName("throws LevelPublishedException when the level is published")
        void publishedLevelCannotBeDeleted() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(LevelPublishedException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
        }

        /// Published-level failure should not delete.
        @Test
        @DisplayName("does not delete when the level is published")
        void publishedLevelFailureSkipsDelete() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(LevelPublishedException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
            Mockito.verify(levelRepository, Mockito.never()).deleteById(LEVEL_ID);
        }

        /// Owner can delete their unpublished level.
        @Test
        @DisplayName("deletes an unpublished level owned by the user")
        void ownerDeletesUnpublishedLevel() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.deleteLevel(OWNER_ID, LEVEL_ID);

            Mockito.verify(levelRepository).deleteById(LEVEL_ID);
        }
    }

    // ====================================================================
    // getCreatedLevelsByUser
    // ====================================================================

    /// Tests for the getCreatedLevelsByUser entry point.
    @Nested
    @DisplayName("getCreatedLevelsByUser")
    class GetCreatedLevels {

        /// Empty repository should yield an empty list.
        @Test
        @DisplayName("returns an empty list when the user has no levels")
        void noLevels() {
            Mockito.when(levelRepository.findByCreator(owner)).thenReturn(List.of());

            Assertions.assertEquals(List.of(), service.getCreatedLevelsByUser(owner));
        }

        /// One DTO should be produced per source level.
        @Test
        @DisplayName("returns one DTO per source level")
        void oneDtoPerLevel() {
            final Level a = newLevel(LIST_TITLE_A, owner);
            final Level b = newLevel(LIST_TITLE_B, owner);
            Mockito.when(levelRepository.findByCreator(owner)).thenReturn(List.of(a, b));
            Mockito.when(attemptRepository.countByLevel(a)).thenReturn(0L);
            Mockito.when(attemptRepository.countByLevelAndCompletedTrue(a)).thenReturn(0L);
            Mockito.when(attemptRepository.countByLevel(b)).thenReturn(0L);
            Mockito.when(attemptRepository.countByLevelAndCompletedTrue(b)).thenReturn(0L);

            final List<CreatedLevelProfileDTO> result = service.getCreatedLevelsByUser(owner);

            Assertions.assertEquals(2, result.size());
        }

        /// DTOs should carry the per-level play count from the repository.
        @Test
        @DisplayName("populates each DTO with the level's play count")
        void carriesPlayCount() {
            final Level a = newLevel(LIST_TITLE_A, owner);
            Mockito.when(levelRepository.findByCreator(owner)).thenReturn(List.of(a));
            Mockito.when(attemptRepository.countByLevel(a)).thenReturn(10L);
            Mockito.when(attemptRepository.countByLevelAndCompletedTrue(a)).thenReturn(7L);

            final List<CreatedLevelProfileDTO> result = service.getCreatedLevelsByUser(owner);

            Assertions.assertEquals(10L, result.get(0).playCount());
        }

        /// DTOs should carry the per-level completion count from the repository.
        @Test
        @DisplayName("populates each DTO with the level's completion count")
        void carriesCompleteCount() {
            final Level a = newLevel(LIST_TITLE_A, owner);
            Mockito.when(levelRepository.findByCreator(owner)).thenReturn(List.of(a));
            Mockito.when(attemptRepository.countByLevel(a)).thenReturn(10L);
            Mockito.when(attemptRepository.countByLevelAndCompletedTrue(a)).thenReturn(7L);

            final List<CreatedLevelProfileDTO> result = service.getCreatedLevelsByUser(owner);

            Assertions.assertEquals(7L, result.get(0).completeCount());
        }
    }

    // ====================================================================
    // updateLevelProperties
    // ====================================================================

    /// Tests for the updateLevelProperties entry point.
    @Nested
    @DisplayName("updateLevelProperties")
    class UpdateLevelProperties {

        /// Missing level should surface as LevelNotFoundException.
        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                () -> service.updateLevelProperties(owner, LEVEL_ID,
                    new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty())));
        }

        /// Non-owner cannot update.
        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to update")
        void nonOwnerCannotUpdate() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(ForbiddenUserException.class,
                () -> service.updateLevelProperties(otherUser, LEVEL_ID,
                    new UpdateLevelDTO(Optional.of(SHORT_NEW_TITLE), Optional.empty(), Optional.empty())));
        }

        /// Published levels cannot be updated.
        @Test
        @DisplayName("throws LevelPublishedException when updating a published level")
        void publishedCannotBeUpdated() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(LevelPublishedException.class,
                () -> service.updateLevelProperties(owner, LEVEL_ID,
                    new UpdateLevelDTO(Optional.of(SHORT_NEW_TITLE), Optional.empty(), Optional.empty())));
        }

        /// A title-only DTO should update the title.
        @Test
        @DisplayName("updates the title when a title is present in the DTO")
        void updatesTitle() {
            final Level level = newLevel(OLD_TITLE, owner);
            level.setDescription(OLD_DESC);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.empty(), Optional.empty()));

            Assertions.assertEquals(NEW_TITLE, level.getTitle());
        }

        /// A title-only DTO must not touch other fields.
        @Test
        @DisplayName("leaves the description unchanged when only a title is present")
        void titleOnlyLeavesDescription() {
            final Level level = newLevel(OLD_TITLE, owner);
            level.setDescription(OLD_DESC);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.empty(), Optional.empty()));

            Assertions.assertEquals(OLD_DESC, level.getDescription());
        }

        /// A full DTO should update the title.
        @Test
        @DisplayName("updates the title when all fields are present")
        void updatesAllFieldsTitle() {
            final Level level = newLevel(OLD_TITLE, owner);
            final ClearCondition clearCondition = new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.of(NEW_DESC),
                    Optional.of(clearCondition)));

            Assertions.assertEquals(NEW_TITLE, level.getTitle());
        }

        /// A full DTO should update the description.
        @Test
        @DisplayName("updates the description when all fields are present")
        void updatesAllFieldsDescription() {
            final Level level = newLevel(OLD_TITLE, owner);
            final ClearCondition clearCondition = new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.of(NEW_DESC),
                    Optional.of(clearCondition)));

            Assertions.assertEquals(NEW_DESC, level.getDescription());
        }

        /// A full DTO should update the clear condition.
        @Test
        @DisplayName("updates the clear condition when all fields are present")
        void updatesAllFieldsClearCondition() {
            final Level level = newLevel(OLD_TITLE, owner);
            final ClearCondition clearCondition = new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.of(NEW_DESC),
                    Optional.of(clearCondition)));

            Assertions.assertEquals(clearCondition, level.getClearCondition());
        }

        /// A successful update should reset the publish-eligible flag.
        @Test
        @DisplayName("invalidates publish eligibility after a successful update")
        void invalidatesPublishEligibility() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            level.validatePublishEligible(OWNER_ID); // start eligible
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(INVALIDATING_TITLE), Optional.empty(), Optional.empty()));

            Assertions.assertFalse(level.isPublishEligible());
        }

        /// The saved level instance should be returned.
        @Test
        @DisplayName("returns the saved level")
        void returnsSavedLevel() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(levelRepository.save(level)).thenReturn(level);

            final Level result = service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty()));

            Assertions.assertSame(level, result);
        }
    }

    // ====================================================================
    // getById
    // ====================================================================

    /// Tests for the getById entry point.
    @Nested
    @DisplayName("getById")
    class GetById {

        /// Existing level should be returned wrapped in an Optional.
        @Test
        @DisplayName("returns the level when present in the repository")
        void presentLevel() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            final Optional<Level> result = service.getById(LEVEL_ID);

            Assertions.assertTrue(result.isPresent());
        }

        /// The wrapped instance should be the exact one returned by the repository.
        @Test
        @DisplayName("wraps the same instance returned by the repository")
        void wrapsRepositoryInstance() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            final Optional<Level> result = service.getById(LEVEL_ID);

            Assertions.assertSame(level, result.get());
        }

        /// Missing level should yield an empty Optional.
        @Test
        @DisplayName("returns empty when the level is missing")
        void missingLevel() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertTrue(service.getById(LEVEL_ID).isEmpty());
        }
    }
}
