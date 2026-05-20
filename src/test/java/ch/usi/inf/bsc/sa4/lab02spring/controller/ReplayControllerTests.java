package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayRequestDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.anticheat.ReplaySubmissionService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

/// Black-box tests for [ReplayController] endpoints using the real security
/// filter chain and the shared controller test security configuration.
@SpringBootTest
@AutoConfigureMockMvc
@Import(ControllerSecurityTestConfig.class)
@DisplayName("The Replay Controller")
@SuppressWarnings("pmd:UnitTestShouldIncludeAssert")
class ReplayControllerTests {

    /// Level id used by replay endpoint tests.
    private static final String LEVEL_ID = "level-1";

    /// Attempt id used by replay submit tests.
    private static final String ATTEMPT_ID = "attempt-1";

    /// Request used by replay submit tests.
    private static final ReplayRequestDTO REPLAY_REQUEST = new ReplayRequestDTO(
            LEVEL_ID,
            ATTEMPT_ID,
            42,
            List.of(new InputFrameDTO(0, true, false, false, false)));

    /// Successful replay response returned by the mocked service.
    private static final ReplayResultDTO REPLAY_RESULT =
            new ReplayResultDTO(true, "level_complete", 42);

    /// Mocked level repository used by start requests.
    @MockitoBean
    private LevelRepository levelRepository;

    /// Mocked replay submission service used by submit requests.
    @MockitoBean
    private ReplaySubmissionService submissionService;

    /// HTTP client bound to MockMvc for controller endpoint checks.
    @Autowired
    private RestTestClient restTestClient;

    /// Tests for POST /replay/start.
    @Nested
    @DisplayName("POST /replay/start")
    class StartRun {

        /// Verifies that a playable level can start replay tracking.
        @Test
        @DisplayName("should return 200 OK when the level is playable")
        void shouldReturnOkWhenLevelIsPlayable() {
            final Level level = new Level("Replay", "desc", new User(
                    ControllerSecurityTestConfig.DEFAULT_USER_ID,
                    ControllerSecurityTestConfig.DEFAULT_USER_NAME));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            restTestClient.post().uri("/replay/start")
                    .body(new ReplayController.StartRequest(LEVEL_ID))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody().isEmpty();
        }

        /// Verifies that a missing level maps to 404 Not Found.
        @Test
        @DisplayName("should return 404 Not Found when the level does not exist")
        void shouldReturnNotFoundWhenLevelDoesNotExist() {
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.empty());

            restTestClient.post().uri("/replay/start")
                    .body(new ReplayController.StartRequest(LEVEL_ID))
                    .exchange()
                    .expectStatus().isNotFound();
        }

        /// Verifies that an unpublished non-owned level is rejected.
        @Test
        @DisplayName("should return 403 Forbidden when the user cannot play the level")
        void shouldReturnForbiddenWhenUserCannotPlayLevel() {
            final Level level = new Level("Replay", "desc", new User("other-user", "Luigi"));
            Mockito.when(levelRepository.findById(LEVEL_ID)).thenReturn(Optional.of(level));

            restTestClient.post().uri("/replay/start")
                    .body(new ReplayController.StartRequest(LEVEL_ID))
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    /// Tests for POST /replay/submit.
    @Nested
    @DisplayName("POST /replay/submit")
    class SubmitRun {

        /// Verifies that replay submit delegates the authenticated user id and
        /// returns the replay result body.
        @Test
        @DisplayName("should return 200 OK and the replay result")
        void shouldReturnReplayResult() {
            Mockito.when(submissionService.submitRun(
                    ControllerSecurityTestConfig.DEFAULT_USER_ID,
                    REPLAY_REQUEST)).thenReturn(REPLAY_RESULT);

            restTestClient.post().uri("/replay/submit")
                    .body(REPLAY_REQUEST)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ReplayResultDTO.class)
                    .isEqualTo(REPLAY_RESULT);

            Mockito.verify(submissionService).submitRun(
                    ControllerSecurityTestConfig.DEFAULT_USER_ID,
                    REPLAY_REQUEST);
        }

        /// Verifies that submission-level missing resources surface as 404.
        @Test
        @DisplayName("should return 404 Not Found when submission level is missing")
        void shouldReturnNotFoundWhenSubmissionLevelIsMissing() {
            Mockito.when(submissionService.submitRun(
                    ControllerSecurityTestConfig.DEFAULT_USER_ID,
                    REPLAY_REQUEST)).thenThrow(new LevelNotFoundException());

            restTestClient.post().uri("/replay/submit")
                    .body(REPLAY_REQUEST)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        /// Verifies that forbidden submissions surface as 403.
        @Test
        @DisplayName("should return 403 Forbidden when submission is not allowed")
        void shouldReturnForbiddenWhenSubmissionIsNotAllowed() {
            Mockito.when(submissionService.submitRun(
                    ControllerSecurityTestConfig.DEFAULT_USER_ID,
                    REPLAY_REQUEST)).thenThrow(new ForbiddenUserException("Forbidden"));

            restTestClient.post().uri("/replay/submit")
                    .body(REPLAY_REQUEST)
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }
}
