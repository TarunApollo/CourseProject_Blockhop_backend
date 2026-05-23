package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.GhostDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.GhostService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPlayService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.OAuth2UserUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenLevelActionException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/// REST endpoints for player interaction with a level (play and submit).
@RestController
@RequestMapping("/levels")
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring-managed singleton; injected services are intentionally shared")
public class LevelPlayController {

    /// Handles playable map retrieval and attempt submission.
    private final LevelPlayService levelPlayService;
    /// Resolves authenticated users for controller actions.
    private final UserService userService;
    /// Resolves the ghost replay for a level.
    private final GhostService ghostService;

    /// Constructs a new LevelPlayController.
    /// @param levelPlayService the service for play flow and submissions
    /// @param userService      the service for accessing user data
    /// @param ghostService     the service for resolving the ghost replay
    @Autowired
    public LevelPlayController(
            final LevelPlayService levelPlayService,
            final UserService userService,
            final GhostService ghostService) {
        this.levelPlayService = levelPlayService;
        this.userService = userService;
        this.ghostService = ghostService;
    }

    /// Submits an attempt for the specified level on behalf of the authenticated user.
    ///
    /// @spec.requires authentication, levelId, and dto are not null.
    /// @spec.effects validates the attempt against the level state; if the level
    ///               is unpublished and owned by the authenticated user and the
    ///               attempt is successful, marks the level as publish eligible;
    ///               records the attempt through the attempt service.
    /// @param user           the authenticated OAuth2 user
    /// @param levelId        the id of the level to submit an attempt for
    /// @param dto            the DTO containing the attempt details
    /// @return a 200 OK response containing the result of the attempt
    /// @throws UserNotFoundException if no user with the authenticated id exists
    /// @throws LevelNotFoundException if no level with the given levelId exists
    /// @throws ForbiddenLevelActionException
    ///         if the level is unpublished and the authenticated user is not its creator
    /// @throws ForbiddenUserException if the authenticated user is not the
    ///         owner of the level when marking it as publish eligible
    @PostMapping("/{levelId}/submit")
    public ResponseEntity<AttemptResponseDTO> submitAttempt(
            @AuthenticationPrincipal final OAuth2User user,
            @PathVariable final String levelId,
            @RequestBody final AttemptDTO dto) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(user, "sub");
        return ResponseEntity.ok(new AttemptResponseDTO(this.levelPlayService.handleLevelSubmission(levelId, userId, dto)));
    }

    /// Returns a Tiled/Phaser-compatible map for the requested playable level.
    ///
    /// @spec.requires authentication and levelId are not null.
    /// @spec.effects resolves the authenticated user and delegates to the level
    ///               play service to validate access and export the requested
    ///               level as a frontend-consumable map structure.
    /// @param oauth2User the authenticated OAuth2 user
    /// @param levelId        the id of the level to load for play
    /// @return a 200 OK response containing a Tiled/Phaser-compatible map payload
    /// @throws UserNotFoundException if the authenticated user does not exist
    @GetMapping("/play/{levelId}/map")
    public ResponseEntity<Map<String, Object>> getMap(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @PathVariable final String levelId) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, "sub");
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return ResponseEntity.ok(this.levelPlayService.getPlayableMap(user, levelId));
    }

    /// Returns the ghost replay (fastest replayable attempt) for the given
    /// published level on behalf of the authenticated user.
    ///
    /// @spec.requires authentication and levelId are not null.
    /// @spec.effects resolves the authenticated user and delegates to the
    ///               ghost service; returns 204 when no eligible ghost exists
    ///               (player has not completed the level, or no completed
    ///               attempt with a replayable input log is available) and
    ///               404 when the level does not exist or is not published.
    /// @param oauth2User the authenticated OAuth2 user
    /// @param levelId    the id of the level whose ghost is requested
    /// @return a 200 OK with a `GhostDTO` body when a ghost is available,
    ///         a 204 No Content when none is eligible
    /// @throws UserNotFoundException  if the authenticated user does not exist
    /// @throws LevelNotFoundException if the level does not exist or is not
    ///         published
    @GetMapping("/{levelId}/ghost")
    public ResponseEntity<GhostDTO> getGhost(
            @AuthenticationPrincipal final OAuth2User oauth2User,
            @PathVariable final String levelId) {
        final String userId = OAuth2UserUtils.getRequiredAttribute(oauth2User, "sub");
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return this.ghostService.getGhostForLevel(levelId, user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
