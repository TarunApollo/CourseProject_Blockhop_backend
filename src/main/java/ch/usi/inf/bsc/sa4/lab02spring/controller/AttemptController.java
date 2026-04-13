package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateAttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/// REST controller for managing level play attempts.
/// Provides endpoints for recording attempts and retrieving attempt history.
@RestController
@RequestMapping("/attempts")
public class AttemptController {

    /// Service handling attempt persistence and lookup.
    private final AttemptService attemptService;

    /// Service providing access to the authenticated user.
    private final UserService userService;

    /// Constructs a new AttemptController with the given dependencies.
    ///
    /// @param attemptService the service for managing attempt operations
    /// @param userService    the service for accessing user data
    @Autowired
    public AttemptController(final AttemptService attemptService, final UserService userService) {
        this.attemptService = attemptService;
        this.userService = userService;
    }

    /// Records a new attempt for the authenticated user.
    ///
    /// @spec.requires authentication and createAttemptDTO are not null.
    /// @spec.effects saves a new Attempt to the repository.
    /// @param authentication abstract token for authentication
    /// @param createAttemptDTO contains the level id, completion status, and
    ///                         elapsed time
    /// @return a 200 OK response containing the created attempt
    /// @throws UserNotFoundException if the authenticated user does not exist
    @PostMapping
    public ResponseEntity<AttemptResponseDTO> createAttempt(
            final Authentication authentication,
            @RequestBody final CreateAttemptDTO createAttemptDTO) {
        final String userId = getUserIdFromAuth(authentication);
        final User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        final Attempt attempt = attemptService.createAttempt(user, createAttemptDTO);
        return ResponseEntity.ok(new AttemptResponseDTO(attempt));
    }

    /// Returns all attempts for the authenticated user.
    ///
    /// @param authentication abstract token for authentication
    /// @return a list of all attempts for the user as AttemptResponseDTOs
    /// @throws UserNotFoundException if the authenticated user does not exist
    @GetMapping
    public List<AttemptResponseDTO> getMyAttempts(final Authentication authentication) {
        final String userId = getUserIdFromAuth(authentication);
        final User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return attemptService.getAttemptsByUser(user).stream()
                .map(AttemptResponseDTO::new)
                .toList();
    }

    /// Returns all attempts for the authenticated user on a specific level.
    ///
    /// @param authentication abstract token for authentication
    /// @param levelId        the ID of the level to filter attempts by
    /// @return a list of attempts for the user on the given level
    /// @throws UserNotFoundException if the authenticated user does not exist
    @GetMapping("/levels/{levelId}")
    public List<AttemptResponseDTO> getMyAttemptsForLevel(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = getUserIdFromAuth(authentication);
        final User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return attemptService.getAttemptsByUserAndLevel(user, levelId).stream()
                .map(AttemptResponseDTO::new)
                .toList();
    }

    /// Returns a specific attempt belonging to the authenticated user.
    ///
    /// @param authentication abstract token for authentication
    /// @param attemptId      the ID of the attempt to retrieve
    /// @return a 200 OK response with the attempt, or 404 if not found
    /// @throws UserNotFoundException if the authenticated user does not exist
    @GetMapping("/{attemptId}")
    public ResponseEntity<AttemptResponseDTO> getAttempt(
            final Authentication authentication,
            @PathVariable final String attemptId) {
        final String userId = getUserIdFromAuth(authentication);
        final User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return attemptService.getAttemptByIdAndUser(attemptId, user)
                .map(AttemptResponseDTO::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
