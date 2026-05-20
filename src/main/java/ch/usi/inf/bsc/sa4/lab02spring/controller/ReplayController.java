package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayRequestDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.ReplayResultDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.repository.LevelRepository;
import ch.usi.inf.bsc.sa4.lab02spring.service.anticheat.AntiCheatLog;
import ch.usi.inf.bsc.sa4.lab02spring.service.anticheat.ReplaySubmissionService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.OAuth2UserUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// Controller exposing replay-based anti-cheat endpoints for level attempts.
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring injects shared collaborators that stay private to this controller."
)
@RestController
@RequestMapping("/replay")
public class ReplayController {

    /// OAuth2 subject attribute name.
    private static final String OAUTH_SUB_ATTRIBUTE = "sub";

    /// Repository used to load levels submitted for replay verification.
    private final LevelRepository levelRepository;

    /// Service that handles replay submission and anti-cheat persistence.
    private final ReplaySubmissionService submissionService;

    /// Creates the replay controller.
    public ReplayController(
            final LevelRepository levelRepository,
            final ReplaySubmissionService submissionService) {
        this.levelRepository = levelRepository;
        this.submissionService = submissionService;
    }

    /// Validates that a player can start a replay-tracked run on a level.
    ///
    /// @param oauth2User authenticated OAuth2 user principal
    /// @param request    start request containing the level id
    /// @return an empty success response when the level can be played
    @PostMapping("/start")
    public ResponseEntity<Void> startRun(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @RequestBody final StartRequest request) {

        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, OAUTH_SUB_ATTRIBUTE);
        final Level level = levelRepository.findById(request.levelId())
                .orElseThrow(LevelNotFoundException::new);
        level.ensurePlayable(userId);

        AntiCheatLog.levelStarted(userId, request.levelId());
        return ResponseEntity.ok().build();
    }

    /// Submits a completed run for replay execution and anti-cheat classification.
    ///
    /// @param oauth2User authenticated OAuth2 user principal
    /// @param request    replay request containing attempt, level, frame, and input
    ///                   data
    /// @return the replay result produced by the anti-cheat simulation
    @PostMapping("/submit")
    public ResponseEntity<ReplayResultDTO> submitRun(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @RequestBody final ReplayRequestDTO request) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, OAUTH_SUB_ATTRIBUTE);
        return ResponseEntity.ok(submissionService.submitRun(userId, request));
    }

    /// Request body used when a player starts a replay-tracked level run.
    ///
    /// @param levelId id of the level being started
    public record StartRequest(String levelId) {
    }
}
