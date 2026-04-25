package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("LevelPlayService")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class LevelPlayServiceTest {

    private static final String LEVEL_ID = "level-1";
    private static final String OWNER_ID = "owner-1";
    private static final String OTHER_USER_ID = "other-1";

    @Mock private LevelRepository levelRepository;
    @Mock private UserService userService;
    @Mock private AttemptService attemptService;
    @Mock private TileSetService tileSetService;
    @Mock private LevelPublishService levelPublishService;

    @InjectMocks private LevelPlayService service;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = new User(OWNER_ID, "Mario");
        otherUser = new User(OTHER_USER_ID, "Luigi");
    }

    private Level newLevel() {
        return new Level("Title", "desc", owner);
    }

    private Level publishableLevel() {
        // The level needs exactly one start flag and exit door to be publishable;
        // construct one so calling publish() works in tests for published flows.
        final Level level = newLevel();
        final Position flag = new Position(1, 1);
        final Position door = new Position(2, 1);
        level.putObjectLayer(flag, new StartFlag(68, flag));
        level.putObjectLayer(door, new ExitDoor(115, door));
        return level;
    }

    private AttemptDTO completedAttempt() {
        return new AttemptDTO(Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ofSeconds(10), true);
    }

    private AttemptDTO unfinishedAttempt() {
        return new AttemptDTO(Map.of(), new Position(0, 0), ZonedDateTime.now(), Duration.ofSeconds(10), false);
    }

    // ====================================================================
    // getPlayableMap
    // ====================================================================

    @Nested
    @DisplayName("getPlayableMap")
    class GetPlayableMap {

        @Test
        @DisplayName("throws LevelNotFoundException when the level id does not exist")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.getPlayableMap(owner, LEVEL_ID));
        }

        @Test
        @DisplayName("throws when the user is not allowed to play (unpublished and not owner)")
        void notAllowedToPlay() {
            final Level level = newLevel(); // unpublished, owner is owner
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            // ensurePlayable is called on the real Level; a non-owner playing an
            // unpublished level should fail.
            assertThrows(RuntimeException.class,
                () -> service.getPlayableMap(otherUser, LEVEL_ID));
        }

        @Test
        @DisplayName("returns a non-null Tiled map for a published level")
        void returnsTiledMapForPublishedLevel() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            final TileSet tileSet = new TileSet(1, "atlas", 128, 128, 0, 8,
                "atlas.png", 1024, 1024, 0, 0, List.of());
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(tileSetService.getTileSet()).thenReturn(tileSet);
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");

            final Map<String, Object> result = service.getPlayableMap(owner, LEVEL_ID);

            assertNotNull(result);
            assertNotNull(result.get("layers"));
        }

        @Test
        @DisplayName("returns a Tiled map when the owner plays an unpublished level")
        void ownerCanPlayOwnUnpublishedLevel() {
            final Level level = newLevel();
            final TileSet tileSet = new TileSet(1, "atlas", 128, 128, 0, 8,
                "atlas.png", 1024, 1024, 0, 0, List.of());
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(tileSetService.getTileSet()).thenReturn(tileSet);

            final Map<String, Object> result = service.getPlayableMap(owner, LEVEL_ID);

            assertNotNull(result);
        }
    }

    // ====================================================================
    // handleLevelSubmission
    // ====================================================================

    @Nested
    @DisplayName("handleLevelSubmission")
    class HandleSubmission {

        @Test
        @DisplayName("throws UserNotFoundException when the user does not exist")
        void userNotFound() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> service.handleLevelSubmission(LEVEL_ID, OWNER_ID, completedAttempt()));
        }

        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.handleLevelSubmission(LEVEL_ID, OWNER_ID, completedAttempt()));
        }

        @Test
        @DisplayName("throws ForbiddenLevelActionException when a non-owner submits to an unpublished level")
        void nonOwnerOnUnpublished() {
            final Level level = newLevel();
            when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenLevelActionException.class,
                () -> service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt()));
            verify(attemptService, never()).submitAttempt(any(), any(), any());
        }

        @Test
        @DisplayName("triggers publish-eligibility validation when owner completes their own unpublished level")
        void ownerCompletesUnpublishedTriggersEligibility() {
            final Level level = publishableLevel();
            final AttemptDTO dto = completedAttempt();
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, dto);

            verify(levelPublishService).validateLevelPublishEligible(level, OWNER_ID);
            verify(attemptService).submitAttempt(owner, level, dto);
        }

        @Test
        @DisplayName("does not trigger eligibility check when owner submits an unfinished attempt")
        void ownerUnfinishedSkipsEligibility() {
            final Level level = newLevel();
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, unfinishedAttempt());

            verify(levelPublishService, never())
                .validateLevelPublishEligible(any(), any());
            verify(attemptService).submitAttempt(any(), any(), any());
        }

        @Test
        @DisplayName("does not trigger eligibility check when level is already published")
        void publishedLevelSkipsEligibility() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt());

            verify(levelPublishService, never())
                .validateLevelPublishEligible(any(), any());
            verify(attemptService).submitAttempt(any(), any(), any());
        }

        @Test
        @DisplayName("returns the success message after a valid submission")
        void returnsSuccessMessage() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            final String result =
                service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt());

            assertEquals("Successful level submission.", result);
        }
    }

}
