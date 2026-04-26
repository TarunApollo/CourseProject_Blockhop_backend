package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelPublishService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.ForbiddenUserException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// REST endpoints for publish / unpublish transitions on a level.
@RestController
@RequestMapping("/levels")
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Spring-managed singleton; injected services are intentionally shared")
public class LevelPublishController {

    /// Manages publish and unpublish transitions.
    private final LevelPublishService levelPublishService;

    /// Constructs a new LevelPublishController.
    /// @param levelPublishService the service for publish and unpublish actions
    @Autowired
    public LevelPublishController(final LevelPublishService levelPublishService) {
        this.levelPublishService = levelPublishService;
    }

    /// Unpublishes a level owned by the authenticated user.
    /// @param authentication the current authenticated user
    /// @param levelId the ID of the level to unpublish
    /// @return 204 No Content when the level is unpublished successfully
    /// @throws LevelNotFoundException if the level does not exist
    /// @throws ForbiddenUserException if the authenticated user is not the owner of the level
    @PutMapping("/{levelId}/unpublish")
    public ResponseEntity<Void> unpublishLevel(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        this.levelPublishService.unpublishLevel(userId, levelId);
        return ResponseEntity.noContent().build();
    }

    /// Publishes the specified level.
    ///
    /// @spec.requires authentication and levelId are not null.
    /// @spec.effects forwards the authenticated user and target level id to the
    ///               level service, which publishes the level.
    /// @param authentication the current authenticated user
    /// @param levelId the id of the target level
    /// @return a 204 No Content response when the level is published successfully
    @PutMapping(path = "/{levelId}/publish")
    public ResponseEntity<Void> publishLevel(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        this.levelPublishService.publish(userId, levelId);
        return ResponseEntity.noContent().build();
    }
}
