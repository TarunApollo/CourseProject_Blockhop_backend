package ch.usi.inf.bsc.sa4.lab02spring.service.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
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
import ch.usi.inf.bsc.sa4.lab02spring.utils.converter.LayerToTiledMapConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/// Unit tests for [LevelPlayService].
@SpringBootTest
@SuppressWarnings("PMD.ExcessiveImports")
@DisplayName("The Level Play Service")
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
    /// Immutable completed attempt DTO used by submission tests.
    private static final AttemptDTO COMPLETED_DTO = new AttemptDTO(
            Map.of(), new Position(0, 0),
            ZonedDateTime.now(), Duration.ofSeconds(10), true);
    /// Immutable unfinished attempt DTO used by submission tests.
    private static final AttemptDTO UNFINISHED_DTO = new AttemptDTO(
            Map.of(), new Position(0, 0),
            ZonedDateTime.now(), Duration.ofSeconds(10), false);
    /// Immutable empty tileset used by play-pipeline tests.
    private static final TileSet EMPTY_TILESET = new TileSet(
            1, "atlas", 128, 128, 0, 8,
            "atlas.png", 1024, 1024, 0, 0, List.of());

    /// The service under test.
    @Autowired
    private LevelPlayService service;

    /// Mocked level repository providing per-test fixtures.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mocked user service for resolving submitting users.
    @MockitoBean
    private UserService userService;

    /// Mocked attempt service that records submitted attempts.
    @MockitoBean
    private AttemptService attemptService;

    /// Mocked tileset service used by playable map generation.
    @MockitoBean
    private TileSetService tileSetService;

    /// Mocked publish service that validates publish eligibility.
    @MockitoBean
    private LevelPublishService levelPublishService;

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

    // Builds an AttemptDTO marked as completed.
    private static AttemptDTO completedAttempt() {
        return new AttemptDTO(Map.of(), new Position(0, 0),
                ZonedDateTime.now(), Duration.ofSeconds(10), true);
    }

    /// Tests for the getPlayableMap entry point.
    @Nested
    @DisplayName("when retrieving a playable map")
    class GetPlayableMap {

        /// Missing level should surface as LevelNotFoundException.
        @Test
        @DisplayName("throws LevelNotFoundException when the level id does not exist")
        void levelNotFound() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> service.getPlayableMap(owner, LEVEL_ID));
        }

        /// Non-owner playing an unpublished level should be rejected.
        @Test
        @DisplayName("throws when the user is not allowed to play an unpublished level")
        void notAllowedToPlay() {
            final Level level = newLevel();
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(RuntimeException.class,
                    () -> service.getPlayableMap(otherUser, LEVEL_ID));
        }

        /// Published level should produce a Tiled map by delegating to the
        /// static converter.
        @Test
        @DisplayName("returns the generated Tiled map from the static converter for a published level")
        void returnsTiledMapForPublishedLevel() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(tileSetService.getTileSet()).thenReturn(EMPTY_TILESET);

            final Map<String, Object> fakeMap = Map.of(LAYERS_KEY, List.of());
            try (MockedStatic<LayerToTiledMapConverter> mockedStatic = Mockito
                    .mockStatic(LayerToTiledMapConverter.class)) {
                mockedStatic.when(() -> LayerToTiledMapConverter.convertPipeline(
                        level, EMPTY_TILESET, tileSetService)).thenReturn(fakeMap);

                final Map<String, Object> result = service.getPlayableMap(owner, LEVEL_ID);

                Assertions.assertSame(fakeMap, result);
            }
        }

        /// Owner should always be able to play their own level even if unpublished.
        @Test
        @DisplayName("returns a Tiled map when the owner plays an unpublished level")
        void ownerCanPlayOwnUnpublishedLevel() {
            final Level level = newLevel();
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            Mockito.when(tileSetService.getTileSet()).thenReturn(EMPTY_TILESET);

            final Map<String, Object> fakeMap = Map.of("data", "test");
            try (MockedStatic<LayerToTiledMapConverter> mockedStatic = Mockito
                    .mockStatic(LayerToTiledMapConverter.class)) {
                mockedStatic.when(() -> LayerToTiledMapConverter.convertPipeline(
                        level, EMPTY_TILESET, tileSetService)).thenReturn(fakeMap);

                final Map<String, Object> result = service.getPlayableMap(owner, LEVEL_ID);

                Assertions.assertSame(fakeMap, result);
            }
        }
    }

    /// Tests for the handleLevelSubmission entry point.
    @Nested
    @DisplayName("when handling a level submission")
    class HandleSubmission {

        /// Missing user should surface as UserNotFoundException.
        @Test
        @DisplayName("throws UserNotFoundException when the user does not exist")
        void userNotFound() {
            Mockito.when(userService.getById(OWNER_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(UserNotFoundException.class,
                    () -> service.handleLevelSubmission(LEVEL_ID, OWNER_ID, COMPLETED_DTO));
        }

        /// Missing level should surface as LevelNotFoundException.
        @Test
        @DisplayName("throws LevelNotFoundException when the level does not exist")
        void levelNotFound() {
            Mockito.when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            Assertions.assertThrows(LevelNotFoundException.class,
                    () -> service.handleLevelSubmission(LEVEL_ID, OWNER_ID, COMPLETED_DTO));
        }

        /// A non-owner submitting to an unpublished level must throw.
        @Test
        @DisplayName("throws ForbiddenLevelActionException when a non-owner submits to an unpublished level")
        void nonOwnerOnUnpublishedThrows() {
            final Level level = newLevel();
            Mockito.when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(ForbiddenLevelActionException.class,
                    () -> service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, COMPLETED_DTO));
        }

        /// A rejected submission must not reach the attempt service.
        @Test
        @DisplayName("does not call submitAttempt when a non-owner submits to an unpublished level")
        void nonOwnerOnUnpublishedSkipsSubmit() {
            final Level level = newLevel();
            Mockito.when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            Assertions.assertThrows(ForbiddenLevelActionException.class,
                    () -> service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, COMPLETED_DTO));
            Mockito.verify(attemptService, Mockito.never())
                    .submitAttempt(
                            ArgumentMatchers.any(),
                            ArgumentMatchers.any(),
                            ArgumentMatchers.any());
        }

        /// Owner completing own unpublished level validates eligibility.
        @Test
        @DisplayName("triggers publish-eligibility validation when owner completes their own unpublished level")
        void ownerCompletesUnpublishedTriggersEligibility() {
            final Level level = publishableLevel();
            Mockito.when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, COMPLETED_DTO);

            Mockito.verify(levelPublishService).validateLevelPublishEligible(level, OWNER_ID);
        }

        /// Owner-completed submission still records the attempt.
        @Test
        @DisplayName("records the attempt when owner completes their own unpublished level")
        void ownerCompletesUnpublishedRecordsAttempt() {
            final Level level = publishableLevel();
            Mockito.when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, COMPLETED_DTO);

            Mockito.verify(attemptService).submitAttempt(owner, level, COMPLETED_DTO);
        }

        /// Unfinished attempt should skip the eligibility check.
        @Test
        @DisplayName("does not trigger eligibility check when owner submits an unfinished attempt")
        void ownerUnfinishedSkipsEligibility() {
            final Level level = newLevel();
            Mockito.when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, UNFINISHED_DTO);

            Mockito.verify(levelPublishService, Mockito.never())
                    .validateLevelPublishEligible(
                            ArgumentMatchers.any(),
                            ArgumentMatchers.any());
        }

        /// Unfinished attempt should still be recorded.
        @Test
        @DisplayName("records the attempt even when owner submits an unfinished attempt")
        void ownerUnfinishedRecordsAttempt() {
            final Level level = newLevel();
            Mockito.when(userService.getById(OWNER_ID)).thenReturn(Optional.of(owner));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OWNER_ID, UNFINISHED_DTO);

            Mockito.verify(attemptService).submitAttempt(owner, level, UNFINISHED_DTO);
        }

        /// Published levels skip eligibility validation regardless of submitter.
        @Test
        @DisplayName("does not trigger eligibility check when the level is already published")
        void publishedLevelSkipsEligibility() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            Mockito.when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, COMPLETED_DTO);

            Mockito.verify(levelPublishService, Mockito.never())
                    .validateLevelPublishEligible(
                            ArgumentMatchers.any(),
                            ArgumentMatchers.any());
        }

        /// Already-published level should still record the attempt.
        @Test
        @DisplayName("records the attempt when the level is already published")
        void publishedLevelRecordsAttempt() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            Mockito.when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, COMPLETED_DTO);

            Mockito.verify(attemptService).submitAttempt(otherUser, level, COMPLETED_DTO);
        }

        /// Successful submission should return the created Attempt.
        @Test
        @DisplayName("returns the created attempt after a valid submission")
        void returnsCreatedAttempt() {
            final Level level = publishableLevel();
            level.validatePublishEligible(OWNER_ID);
            level.publish(OWNER_ID);
            Mockito.when(userService.getById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));
            final Attempt savedAttempt = new Attempt(otherUser, ZonedDateTime.now(ZoneOffset.UTC),
                    level, true, Duration.ofSeconds(10));
            when(attemptService.submitAttempt(any(), any(), any())).thenReturn(savedAttempt);
            final Attempt result = service.handleLevelSubmission(LEVEL_ID, OTHER_USER_ID, completedAttempt());

            assertNotNull(result);
            assertEquals(savedAttempt, result);
        }
    }
}
