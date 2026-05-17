package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
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
import java.time.ZoneOffset;
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

/// Unit tests for the level play service.
@DisplayName("LevelPlayService")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "PMD.TooManyStaticImports", "PMD.ExcessiveImports"})
class LevelPlayServiceTest {

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
    /// Default level title used by fixtures.
    private static final String LEVEL_TITLE = "Title";
    /// Default level description used by fixtures.
    private static final String LEVEL_DESC = "desc";
    /// JSON key for the layers entry of a Tiled map.
    private static final String LAYERS_KEY = "layers";

    /// Mocked level repository providing per-test fixtures.
    @Mock private LevelRepository levelRepository;
    /// Mocked user service for resolving submitting users.
    @Mock private UserService userService;
    /// Mocked attempt service that records submitted attempts.
    @Mock private AttemptService attemptService;
    /// Mocked tileset service used by playable map generation.
    @Mock private TileSetService tileSetService;
    /// Mocked publish service that validates publish eligibility.
    @Mock private LevelPublishService levelPublishService;

    /// Service under test, with mocks injected.
    @InjectMocks private LevelPlayService service;

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

    /// Builds a basic level owned by the configured owner.
    private Level newLevel() {
        return new Level(LEVEL_TITLE, LEVEL_DESC, owner);
    }

    /// Builds a level with start flag and exit door so publish() succeeds.
    private Level publishableLevel() {
        final Level level = newLevel();
        final Position flag = new Position(1, 1);
        final Position door = new Position(2, 1);
        level.putObjectLayer(flag, new StartFlag(68, flag));
        level.putObjectLayer(door, new ExitDoor(115, door));
        return level;
    }

    /// Builds a sample tileset suitable for play-pipeline tests.
    private static TileSet emptyTileSet() {
        return new TileSet(1, "atlas", 128, 128, 0, 8,
            "atlas.png", 1024, 1024, 0, 0, List.of());
    }

    /// Builds an AttemptDTO marked as completed.
    private static AttemptDTO completedAttempt() {
        return new AttemptDTO(Map.of(), new Position(0, 0),
            ZonedDateTime.now(), Duration.ofSeconds(10), true);
    }

    /// Builds an AttemptDTO marked as not completed.
    private static AttemptDTO unfinishedAttempt() {
        return new AttemptDTO(Map.of(), new Position(0, 0),
            ZonedDateTime.now(), Duration.ofSeconds(10), false);
    }

    /// Sanity test so static analyzers see at least one top-level @Test on the class.
    @Test
    @DisplayName("test fixture initializes the service")
    void serviceWired() {
        assertNotNull(service);
    }

    // ====================================================================
    // getPlayableMap
    // ====================================================================

    /// Tests for the getPlayableMap entry point.
    @Nested
    @DisplayName("getPlayableMap")
    class GetPlayableMap {

        /// Missing level should surface as LevelNotFoundException.
        @Test
        @DisplayName("throws LevelNotFoundException when the level id does not exist")
        void levelNotFound() {
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.getPlayableMap(owner, LEVEL_ID));
        }

        /// Non-owner playing an unpublished level should be rejected.
        @Test
        @DisplayName("throws when the user is not allowed to play (unpublished and not owner)")
        void notAllowedToPlay() {
            final Level level = newLevel(); // unpublished
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(RuntimeException.class,
                () -> service.getPlayableMap(otherUser, LEVEL_ID));
        }

        /// Published level should produce a Tiled map containing layers.
        @Test
        @DisplayName("returns a Tiled map with layers for a published level")
        void returnsTiledMapForPublishedLevel() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(tileSetService.getTileSet()).thenReturn(emptyTileSet());
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");

            final Map<String, Object> result = service.getPlayableMap(owner, LEVEL_ID);

            assertNotNull(result.get(LAYERS_KEY));
        }

        /// Owner should always be able to play their own level even if unpublished.
        @Test
        @DisplayName("returns a Tiled map when the owner plays an unpublished level")
        void ownerCanPlayOwnUnpublishedLevel() {
            final Level level = newLevel();
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            when(tileSetService.getTileSet()).thenReturn(emptyTileSet());

            final Map<String, Object> result = service.getPlayableMap(owner, LEVEL_ID);

            assertNotNull(result);
        }
    }

    // ====================================================================
    // handleLevelSubmission
    // ====================================================================

    /// Tests for the handleLevelSubmission entry point.
    @Nested
    @DisplayName("handleLevelSubmission")
    class HandleSubmission {

        /// Missing user should surface as UserNotFoundException.
        @Test
        @DisplayName("throws UserNotFoundException when the user does not exist")
        void userNotFound() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                () -> service.handleLevelSubmission(LEVEL_ID, OWNER_ID, completedAttempt()));
        }

        /// Missing level should surface as LevelNotFoundException.
        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            assertThrows(LevelNotFoundException.class,
                () -> service.handleLevelSubmission(LEVEL_ID, OWNER_ID, completedAttempt()));
        }

        /// A non-owner submitting to an unpublished level must throw.
        @Test
        @DisplayName("throws ForbiddenLevelActionException when a non-owner submits to an unpublished level")
        void nonOwnerOnUnpublishedThrows() {
            final Level level = newLevel();
            when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenLevelActionException.class,
                () -> service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt()));
        }

        /// A rejected submission must not reach the attempt service.
        @Test
        @DisplayName("does not call submitAttempt when a non-owner submits to an unpublished level")
        void nonOwnerOnUnpublishedSkipsSubmit() {
            final Level level = newLevel();
            when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            assertThrows(ForbiddenLevelActionException.class,
                () -> service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt()));
            verify(attemptService, never()).submitAttempt(any(), any(), any());
        }

        /// Owner completing own unpublished level validates eligibility.
        @Test
        @DisplayName("triggers publish-eligibility validation when owner completes their own unpublished level")
        void ownerCompletesUnpublishedTriggersEligibility() {
            final Level level = publishableLevel();
            final AttemptDTO dto = completedAttempt();
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, dto);

            verify(levelPublishService).validateLevelPublishEligible(level, OWNER_ID);
        }

        /// Owner-completed submission still records the attempt.
        @Test
        @DisplayName("records the attempt when owner completes their own unpublished level")
        void ownerCompletesUnpublishedRecordsAttempt() {
            final Level level = publishableLevel();
            final AttemptDTO dto = completedAttempt();
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, dto);

            verify(attemptService).submitAttempt(owner, level, dto);
        }

        /// Unfinished attempt should skip the eligibility check.
        @Test
        @DisplayName("does not trigger eligibility check when owner submits an unfinished attempt")
        void ownerUnfinishedSkipsEligibility() {
            final Level level = newLevel();
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, unfinishedAttempt());

            verify(levelPublishService, never()).validateLevelPublishEligible(any(), any());
        }

        /// Unfinished attempt should still be recorded.
        @Test
        @DisplayName("records the attempt even when owner submits an unfinished attempt")
        void ownerUnfinishedRecordsAttempt() {
            final Level level = newLevel();
            when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, unfinishedAttempt());

            verify(attemptService).submitAttempt(any(), any(), any());
        }

        /// Published levels skip eligibility validation regardless of submitter.
        @Test
        @DisplayName("does not trigger eligibility check when the level is already published")
        void publishedLevelSkipsEligibility() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt());

            verify(levelPublishService, never()).validateLevelPublishEligible(any(), any());
        }

        /// Already-published level should still record the attempt.
        @Test
        @DisplayName("records the attempt when the level is already published")
        void publishedLevelRecordsAttempt() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt());

            verify(attemptService).submitAttempt(any(), any(), any());
        }

        /// Successful submission should return the created Attempt.
        @Test
        @DisplayName("returns the created attempt after a valid submission")
        void returnsCreatedAttempt() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            final Attempt savedAttempt = new Attempt(otherUser, ZonedDateTime.now(ZoneOffset.UTC),
                    level, true, Duration.ofSeconds(10));
            when(attemptService.submitAttempt(any(), any(), any())).thenReturn(savedAttempt);

            final Attempt result =
                service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt());

            assertNotNull(result);
            assertEquals(savedAttempt, result);
        }
    }
}
