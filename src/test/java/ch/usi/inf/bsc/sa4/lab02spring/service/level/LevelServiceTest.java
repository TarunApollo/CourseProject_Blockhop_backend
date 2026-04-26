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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Unit tests for the level CRUD service.
/// Verifies create, clone, update, delete, and listing semantics.
@DisplayName("LevelService")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "PMD.TooManyStaticImports", "PMD.ExcessiveImports"})
class LevelServiceTest {

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
    /// Old level title used by update tests.
    private static final String OLD_TITLE = "old-title";
    /// New level title used by update tests.
    private static final String NEW_TITLE = "new-title";
    /// New level description used by update tests.
    private static final String NEW_DESC = "new-desc";

    /// Mocked level repository providing per-test fixtures.
    @Mock private LevelRepository levelRepository;
    /// Mocked attempt repository for play and completion counts.
    @Mock private AttemptRepository attemptRepository;
    /// Mocked user service for resolving the level creator.
    @Mock private UserService userService;

    /// Service under test, with mocks injected.
    @InjectMocks private LevelService service;

    /// Shared owner user fixture.
    private User owner;
    /// Shared non-owner user fixture.
    private User otherUser;

    /// Initializes shared user fixtures before each test.
    @BeforeEach
    void setUp() {
        owner = new User(OWNER_ID, OWNER_NAME);
        otherUser = new User(OTHER_USER_ID, OTHER_NAME);
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
        assertNotNull(service);
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
            when(userService.getById(OWNER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> service.createLevel(new CreateLevelDTO("t", "d"), OWNER_ID));
        }

        /// Created level should expose the title from the DTO.
        @Test
        @DisplayName("the created level has the title from the DTO")
        void createdLevelHasTitle() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.save(ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Level result =
                service.createLevel(new CreateLevelDTO(SAMPLE_TITLE, SAMPLE_DESC), OWNER_ID);

            assertEquals(SAMPLE_TITLE, result.getTitle());
        }

        /// Created level should expose the description from the DTO.
        @Test
        @DisplayName("the created level has the description from the DTO")
        void createdLevelHasDescription() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.save(ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Level result =
                service.createLevel(new CreateLevelDTO(SAMPLE_TITLE, SAMPLE_DESC), OWNER_ID);

            assertEquals(SAMPLE_DESC, result.getDescription());
        }

        /// Created level should be owned by the resolved user.
        @Test
        @DisplayName("the created level is owned by the resolved user")
        void createdLevelHasOwner() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.save(ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Level result =
                service.createLevel(new CreateLevelDTO(SAMPLE_TITLE, SAMPLE_DESC), OWNER_ID);

            assertSame(owner, result.getCreator());
        }

        /// Created level should be persisted via the repository.
        @Test
        @DisplayName("persists the new level via the repository")
        void createdLevelIsPersisted() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.save(ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Level result =
                service.createLevel(new CreateLevelDTO(SAMPLE_TITLE, SAMPLE_DESC), OWNER_ID);

            verify(levelRepository).save(result);
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
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            assertTrue(result.isEmpty());
        }

        /// Missing source level should not persist anything.
        @Test
        @DisplayName("does not persist when the source level does not exist")
        void sourceNotFoundSkipsSave() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            verify(levelRepository, never()).save(ArgumentMatchers.<Level>any());
        }

        /// Source level not owned by the user should yield an empty Optional.
        @Test
        @DisplayName("returns empty when the source level is not owned by the requesting user")
        void notOwnedReturnsEmpty() {
            final Level source = newLevel(DEFAULT_TITLE, owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), otherUser);

            assertTrue(result.isEmpty());
        }

        /// Source level not owned by the user should not persist anything.
        @Test
        @DisplayName("does not persist when the source level is not owned by the requesting user")
        void notOwnedSkipsSave() {
            final Level source = newLevel(DEFAULT_TITLE, owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));

            service.cloneLevel(new CloneLevelDTO(LEVEL_ID), otherUser);

            verify(levelRepository, never()).save(ArgumentMatchers.<Level>any());
        }

        /// First clone of a level should be titled '<original> (2)'.
        @Test
        @DisplayName("clones with title 'X (2)' when no other clones exist")
        void firstCloneSuffix() {
            final Level source = newLevel("Adventure", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(source));
            when(levelRepository.save(ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            assertEquals("Adventure (2)", result.orElseThrow().getTitle());
        }

        /// Subsequent clones should pick the next free index.
        @Test
        @DisplayName("picks the next free index when previous clones already exist")
        void picksNextAvailableSuffix() {
            final Level source = newLevel("Quest", owner);
            final Level clone2 = newLevel("Quest (2)", owner);
            final Level clone3 = newLevel("Quest (3)", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(source, clone2, clone3));
            when(levelRepository.save(ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            assertEquals("Quest (4)", result.get().getTitle());
        }

        /// Cloning a level that already has a (n) suffix should strip the suffix first.
        @Test
        @DisplayName("strips an existing (n) suffix from the source title before computing a new one")
        void stripsExistingSuffix() {
            final Level source = newLevel("Maze (5)", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(source));
            when(levelRepository.save(ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            // "Maze (5)" → root "Maze" → next free is "Maze (2)"
            assertEquals("Maze (2)", result.get().getTitle());
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
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
        }

        /// Missing level should skip the delete.
        @Test
        @DisplayName("does not delete when the level does not exist")
        void levelNotFoundSkipsDelete() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
            verify(levelRepository, never()).deleteById(LEVEL_ID);
        }

        /// Non-owner cannot delete.
        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to delete")
        void nonOwnerCannotDelete() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenUserException.class,
                () -> service.deleteLevel(OTHER_USER_ID, LEVEL_ID));
        }

        /// Non-owner failure should not delete.
        @Test
        @DisplayName("does not delete when a non-owner attempt fails")
        void nonOwnerFailureSkipsDelete() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenUserException.class,
                () -> service.deleteLevel(OTHER_USER_ID, LEVEL_ID));
            verify(levelRepository, never()).deleteById(LEVEL_ID);
        }

        /// Published levels cannot be deleted.
        @Test
        @DisplayName("throws LevelPublishedException when the level is published")
        void publishedLevelCannotBeDeleted() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(LevelPublishedException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
        }

        /// Published-level failure should not delete.
        @Test
        @DisplayName("does not delete when the level is published")
        void publishedLevelFailureSkipsDelete() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(LevelPublishedException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
            verify(levelRepository, never()).deleteById(LEVEL_ID);
        }

        /// Owner can delete their unpublished level.
        @Test
        @DisplayName("deletes an unpublished level owned by the user")
        void ownerDeletesUnpublishedLevel() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.deleteLevel(OWNER_ID, LEVEL_ID);

            verify(levelRepository).deleteById(LEVEL_ID);
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
            when(levelRepository.findByCreator(owner)).thenReturn(List.of());

            assertEquals(List.of(), service.getCreatedLevelsByUser(owner));
        }

        /// One DTO should be produced per source level.
        @Test
        @DisplayName("returns one DTO per source level")
        void oneDtoPerLevel() {
            final Level a = newLevel("a", owner);
            final Level b = newLevel("b", owner);
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(a, b));
            when(attemptRepository.countByLevel(a)).thenReturn(0L);
            when(attemptRepository.countByLevelAndCompletedTrue(a)).thenReturn(0L);
            when(attemptRepository.countByLevel(b)).thenReturn(0L);
            when(attemptRepository.countByLevelAndCompletedTrue(b)).thenReturn(0L);

            final List<CreatedLevelProfileDTO> result = service.getCreatedLevelsByUser(owner);

            assertEquals(2, result.size());
        }

        /// DTOs should carry the per-level play count from the repository.
        @Test
        @DisplayName("populates each DTO with the level's play count")
        void carriesPlayCount() {
            final Level a = newLevel("a", owner);
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(a));
            when(attemptRepository.countByLevel(a)).thenReturn(10L);
            when(attemptRepository.countByLevelAndCompletedTrue(a)).thenReturn(7L);

            final List<CreatedLevelProfileDTO> result = service.getCreatedLevelsByUser(owner);

            assertEquals(10L, result.get(0).playCount());
        }

        /// DTOs should carry the per-level completion count from the repository.
        @Test
        @DisplayName("populates each DTO with the level's completion count")
        void carriesCompleteCount() {
            final Level a = newLevel("a", owner);
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(a));
            when(attemptRepository.countByLevel(a)).thenReturn(10L);
            when(attemptRepository.countByLevelAndCompletedTrue(a)).thenReturn(7L);

            final List<CreatedLevelProfileDTO> result = service.getCreatedLevelsByUser(owner);

            assertEquals(7L, result.get(0).completeCount());
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
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.updateLevelProperties(owner, LEVEL_ID,
                    new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty())));
        }

        /// Non-owner cannot update.
        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to update")
        void nonOwnerCannotUpdate() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenUserException.class,
                () -> service.updateLevelProperties(otherUser, LEVEL_ID,
                    new UpdateLevelDTO(Optional.of("new"), Optional.empty(), Optional.empty())));
        }

        /// Published levels cannot be updated.
        @Test
        @DisplayName("throws LevelPublishedException when updating a published level")
        void publishedCannotBeUpdated() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(LevelPublishedException.class,
                () -> service.updateLevelProperties(owner, LEVEL_ID,
                    new UpdateLevelDTO(Optional.of("new"), Optional.empty(), Optional.empty())));
        }

        /// A title-only DTO should update the title.
        @Test
        @DisplayName("updates the title when a title is present in the DTO")
        void updatesTitle() {
            final Level level = newLevel(OLD_TITLE, owner);
            level.setDescription("old-desc");
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.empty(), Optional.empty()));

            assertEquals(NEW_TITLE, level.getTitle());
        }

        /// A title-only DTO must not touch other fields.
        @Test
        @DisplayName("leaves the description unchanged when only a title is present")
        void titleOnlyLeavesDescription() {
            final Level level = newLevel(OLD_TITLE, owner);
            level.setDescription("old-desc");
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.empty(), Optional.empty()));

            assertEquals("old-desc", level.getDescription());
        }

        /// A full DTO should update the title.
        @Test
        @DisplayName("updates the title when all fields are present")
        void updatesAllFieldsTitle() {
            final Level level = newLevel(OLD_TITLE, owner);
            final ClearCondition clearCondition = new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.of(NEW_DESC),
                    Optional.of(clearCondition)));

            assertEquals(NEW_TITLE, level.getTitle());
        }

        /// A full DTO should update the description.
        @Test
        @DisplayName("updates the description when all fields are present")
        void updatesAllFieldsDescription() {
            final Level level = newLevel(OLD_TITLE, owner);
            final ClearCondition clearCondition = new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.of(NEW_DESC),
                    Optional.of(clearCondition)));

            assertEquals(NEW_DESC, level.getDescription());
        }

        /// A full DTO should update the clear condition.
        @Test
        @DisplayName("updates the clear condition when all fields are present")
        void updatesAllFieldsClearCondition() {
            final Level level = newLevel(OLD_TITLE, owner);
            final ClearCondition clearCondition = new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of(NEW_TITLE), Optional.of(NEW_DESC),
                    Optional.of(clearCondition)));

            assertEquals(clearCondition, level.getClearCondition());
        }

        /// A successful update should reset the publish-eligible flag.
        @Test
        @DisplayName("invalidates publish eligibility after a successful update")
        void invalidatesPublishEligibility() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            level.validatePublishEligible(OWNER_ID); // start eligible
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of("x"), Optional.empty(), Optional.empty()));

            assertFalse(level.isPublishEligible());
        }

        /// The saved level instance should be returned.
        @Test
        @DisplayName("returns the saved level")
        void returnsSavedLevel() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            final Level result = service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty()));

            assertSame(level, result);
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
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            final Optional<Level> result = service.getById(LEVEL_ID);

            assertTrue(result.isPresent());
        }

        /// The wrapped instance should be the exact one returned by the repository.
        @Test
        @DisplayName("wraps the same instance returned by the repository")
        void wrapsRepositoryInstance() {
            final Level level = newLevel(DEFAULT_TITLE, owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            final Optional<Level> result = service.getById(LEVEL_ID);

            assertSame(level, result.get());
        }

        /// Missing level should yield an empty Optional.
        @Test
        @DisplayName("returns empty when the level is missing")
        void missingLevel() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertTrue(service.getById(LEVEL_ID).isEmpty());
        }
    }
}
