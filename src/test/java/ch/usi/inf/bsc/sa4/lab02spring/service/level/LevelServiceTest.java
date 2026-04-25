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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("LevelService")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class LevelServiceTest {

    private static final String LEVEL_ID = "level-1";
    private static final String OWNER_ID = "owner-1";
    private static final String OTHER_USER_ID = "other-1";

    @Mock private LevelRepository levelRepository;
    @Mock private AttemptRepository attemptRepository;
    @Mock private UserService userService;

    @InjectMocks private LevelService service;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = new User(OWNER_ID, "Mario");
        otherUser = new User(OTHER_USER_ID, "Luigi");
    }

    private Level newLevel(final String title, final User creator) {
        return new Level(title, "desc", creator);
    }

    private Level publishableLevel() {
        final Level level = newLevel("title", owner);
        final Position flag = new Position(1, 1);
        final Position door = new Position(2, 1);
        level.putObjectLayer(flag, new StartFlag(68, flag));
        level.putObjectLayer(door, new ExitDoor(115, door));
        return level;
    }

    // ====================================================================
    // createLevel
    // ====================================================================

    @Nested
    @DisplayName("createLevel")
    class CreateLevel {

        @Test
        @DisplayName("throws UserNotFoundException when the user does not exist")
        void userNotFound() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> service.createLevel(new CreateLevelDTO("t", "d"), OWNER_ID));
        }

        @Test
        @DisplayName("persists a new level created by the resolved user")
        void createsAndSavesLevel() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.save(org.mockito.ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Level result =
                service.createLevel(new CreateLevelDTO("My Level", "Description"), OWNER_ID);

            assertEquals("My Level", result.getTitle());
            assertEquals("Description", result.getDescription());
            assertSame(owner, result.getCreator());
            verify(levelRepository).save(result);
        }
    }

    // ====================================================================
    // cloneLevel
    // ====================================================================

    @Nested
    @DisplayName("cloneLevel")
    class CloneLevel {

        @Test
        @DisplayName("returns empty when the source level does not exist")
        void sourceNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            assertTrue(result.isEmpty());
            verify(levelRepository, never()).save(org.mockito.ArgumentMatchers.<Level>any());
        }

        @Test
        @DisplayName("returns empty when the source level is not owned by the requesting user")
        void notOwnedReturnsEmpty() {
            final Level source = newLevel("title", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), otherUser);

            assertTrue(result.isEmpty());
            verify(levelRepository, never()).save(org.mockito.ArgumentMatchers.<Level>any());
        }

        @Test
        @DisplayName("clones with title 'X (2)' when no other clones exist")
        void firstCloneSuffix() {
            final Level source = newLevel("Adventure", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(source));
            when(levelRepository.save(org.mockito.ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            assertTrue(result.isPresent());
            assertEquals("Adventure (2)", result.get().getTitle());
        }

        @Test
        @DisplayName("picks the next free index when previous clones already exist")
        void picksNextAvailableSuffix() {
            final Level source = newLevel("Quest", owner);
            final Level clone2 = newLevel("Quest (2)", owner);
            final Level clone3 = newLevel("Quest (3)", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(source, clone2, clone3));
            when(levelRepository.save(org.mockito.ArgumentMatchers.<Level>any()))
                .thenAnswer(inv -> inv.getArgument(0));

            final Optional<Level> result =
                service.cloneLevel(new CloneLevelDTO(LEVEL_ID), owner);

            assertEquals("Quest (4)", result.get().getTitle());
        }

        @Test
        @DisplayName("strips an existing (n) suffix from the source title before computing a new one")
        void stripsExistingSuffix() {
            final Level source = newLevel("Maze (5)", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(source));
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(source));
            when(levelRepository.save(org.mockito.ArgumentMatchers.<Level>any()))
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

    @Nested
    @DisplayName("deleteLevel")
    class DeleteLevel {

        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
            verify(levelRepository, never()).deleteById(LEVEL_ID);
        }

        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to delete")
        void nonOwnerCannotDelete() {
            final Level level = newLevel("title", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenUserException.class,
                () -> service.deleteLevel(OTHER_USER_ID, LEVEL_ID));
            verify(levelRepository, never()).deleteById(LEVEL_ID);
        }

        @Test
        @DisplayName("throws LevelPublishedException when the level is published")
        void publishedLevelCannotBeDeleted() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(LevelPublishedException.class,
                () -> service.deleteLevel(OWNER_ID, LEVEL_ID));
            verify(levelRepository, never()).deleteById(LEVEL_ID);
        }

        @Test
        @DisplayName("deletes an unpublished level owned by the user")
        void ownerDeletesUnpublishedLevel() {
            final Level level = newLevel("title", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.deleteLevel(OWNER_ID, LEVEL_ID);

            verify(levelRepository).deleteById(LEVEL_ID);
        }
    }

    // ====================================================================
    // getCreatedLevelsByUser
    // ====================================================================

    @Nested
    @DisplayName("getCreatedLevelsByUser")
    class GetCreatedLevels {

        @Test
        @DisplayName("returns an empty list when the user has no levels")
        void noLevels() {
            when(levelRepository.findByCreator(owner)).thenReturn(List.of());

            assertEquals(List.of(), service.getCreatedLevelsByUser(owner));
        }

        @Test
        @DisplayName("returns one DTO per level with play and completion counts")
        void mapsLevelsToDtosWithStats() {
            final Level a = newLevel("a", owner);
            final Level b = newLevel("b", owner);
            when(levelRepository.findByCreator(owner)).thenReturn(List.of(a, b));
            when(attemptRepository.countByLevel(a)).thenReturn(10L);
            when(attemptRepository.countByLevelAndCompletedTrue(a)).thenReturn(7L);
            when(attemptRepository.countByLevel(b)).thenReturn(3L);
            when(attemptRepository.countByLevelAndCompletedTrue(b)).thenReturn(1L);

            final List<CreatedLevelProfileDTO> result = service.getCreatedLevelsByUser(owner);

            assertEquals(2, result.size());
            assertEquals(10L, result.get(0).playCount());
            assertEquals(7L, result.get(0).completeCount());
            assertEquals(3L, result.get(1).playCount());
            assertEquals(1L, result.get(1).completeCount());
        }
    }

    // ====================================================================
    // updateLevelProperties
    // ====================================================================

    @Nested
    @DisplayName("updateLevelProperties")
    class UpdateLevelProperties {

        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.updateLevelProperties(owner, LEVEL_ID,
                    new UpdateLevelDTO(Optional.empty(), Optional.empty(), Optional.empty())));
        }

        @Test
        @DisplayName("throws ForbiddenUserException when a non-owner tries to update")
        void nonOwnerCannotUpdate() {
            final Level level = newLevel("title", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenUserException.class,
                () -> service.updateLevelProperties(otherUser, LEVEL_ID,
                    new UpdateLevelDTO(Optional.of("new"), Optional.empty(), Optional.empty())));
        }

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

        @Test
        @DisplayName("updates only fields present in the DTO")
        void updatesOnlyPresentFields() {
            final Level level = newLevel("old-title", owner);
            level.setDescription("old-desc");
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of("new-title"), Optional.empty(), Optional.empty()));

            assertEquals("new-title", level.getTitle());
            // description unchanged
            assertEquals("old-desc", level.getDescription());
        }

        @Test
        @DisplayName("updates title, description, and clearCondition when all are present")
        void updatesAllFields() {
            final Level level = newLevel("old-title", owner);
            final ClearCondition clearCondition = new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(
                    Optional.of("new-title"),
                    Optional.of("new-desc"),
                    Optional.of(clearCondition)));

            assertEquals("new-title", level.getTitle());
            assertEquals("new-desc", level.getDescription());
            assertEquals(clearCondition, level.getClearCondition());
        }

        @Test
        @DisplayName("invalidates publish eligibility after a successful update")
        void invalidatesPublishEligibility() {
            final Level level = newLevel("title", owner);
            level.validatePublishEligible(OWNER_ID); // start eligible
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(levelRepository.save(level)).thenReturn(level);

            service.updateLevelProperties(owner, LEVEL_ID,
                new UpdateLevelDTO(Optional.of("x"), Optional.empty(), Optional.empty()));

            assertEquals(false, level.isPublishEligible());
        }

        @Test
        @DisplayName("returns the saved level")
        void returnsSavedLevel() {
            final Level level = newLevel("title", owner);
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

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("returns the level when present in the repository")
        void presentLevel() {
            final Level level = newLevel("title", owner);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            final Optional<Level> result = service.getById(LEVEL_ID);

            assertTrue(result.isPresent());
            assertSame(level, result.get());
        }

        @Test
        @DisplayName("returns empty when the level is missing")
        void missingLevel() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertTrue(service.getById(LEVEL_ID).isEmpty());
        }
    }
}
