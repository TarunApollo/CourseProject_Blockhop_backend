package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.AttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.CreateAttemptDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils.getUserIdFromAuth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/// REST controller for managing level play attempts.
/// Provides endpoints for recording attempts and retrieving attempt history.
@RestController
@RequestMapping("/attempts")
public class AttemptController {

    private final AttemptService attemptService;
    private final UserService userService;

    /// Constructs a new AttemptController with the given dependencies.
    /// @param attemptService the service for managing attempt operations
    /// @param userService the service for accessing user data
    @Autowired
    public AttemptController(AttemptService attemptService, UserService userService) {
        this.attemptService = attemptService;
        this.userService = userService;
    }

    /// Records a new attempt for the authenticated user on a level.
    /// @spec.requires authentication and createAttemptDTO are not null.
    /// @spec.effects saves a new Attempt to the repository.
    /// @param authentication abstract token for authentication
    /// @param createAttemptDTO the DTO containing the level ID, completion status, and time taken
    /// @return a 200 OK response containing the created attempt as an AttemptDTO
    /// @throws UserNotFoundException if the authenticated user does not exist
    @PostMapping
    public ResponseEntity<AttemptDTO> createAttempt(
            Authentication authentication,
            @RequestBody CreateAttemptDTO createAttemptDTO) {
        String userId = getUserIdFromAuth(authentication);
        User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        var attempt = attemptService.createAttempt(user, createAttemptDTO);
        return ResponseEntity.ok(new AttemptDTO(attempt));
    }

    /// Returns all attempts for the authenticated user.
    /// @param authentication abstract token for authentication
    /// @return a list of all attempts for the user as AttemptDTOs
    /// @throws UserNotFoundException if the authenticated user does not exist
    @GetMapping
    public List<AttemptDTO> getMyAttempts(Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return attemptService.getAttemptsByUser(user).stream()
                .map(AttemptDTO::new)
                .toList();
    }

    /// Returns all attempts for the authenticated user on a specific level.
    /// @param authentication abstract token for authentication
    /// @param levelId the ID of the level to filter attempts by
    /// @return a list of attempts for the user on the given level as AttemptDTOs
    /// @throws UserNotFoundException if the authenticated user does not exist
    @GetMapping("/levels/{levelId}")
    public List<AttemptDTO> getMyAttemptsForLevel(
            Authentication authentication,
            @PathVariable String levelId) {
        String userId = getUserIdFromAuth(authentication);
        User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return attemptService.getAttemptsByUserAndLevel(user, levelId).stream()
                .map(AttemptDTO::new)
                .toList();
    }

    /// Returns a specific attempt by ID, if it belongs to the authenticated user.
    /// @param authentication abstract token for authentication
    /// @param attemptId the ID of the attempt to retrieve
    /// @return a 200 OK response with the attempt as an AttemptDTO, or 404 if not found
    /// @throws UserNotFoundException if the authenticated user does not exist
    @GetMapping("/{attemptId}")
    public ResponseEntity<AttemptDTO> getAttempt(
            Authentication authentication,
            @PathVariable String attemptId) {
        String userId = getUserIdFromAuth(authentication);
        User user = userService.getById(userId).orElseThrow(UserNotFoundException::new);
        return attemptService.getAttemptByIdAndUser(attemptId, user)
                .map(AttemptDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}