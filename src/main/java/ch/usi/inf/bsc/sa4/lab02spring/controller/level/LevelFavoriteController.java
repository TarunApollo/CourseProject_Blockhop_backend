package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelFavoriteService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;
import ch.usi.inf.bsc.sa4.lab02spring.utils.LevelNotFoundException;
import ch.usi.inf.bsc.sa4.lab02spring.utils.UserNotFoundException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// REST endpoints for marking and unmarking levels as favorites.
@RestController
@RequestMapping("/levels")
public class LevelFavoriteController {

    /// Service for managing user favorites.
    private final LevelFavoriteService levelFavoriteService;
    /// Resolves authenticated users for controller actions.
    private final UserService userService;
    /// Resolves levels by id.
    private final LevelService levelService;

    /// Constructs a new LevelFavoriteController.
    /// @param levelFavoriteService the service for managing favorites
    /// @param userService          the service for accessing user data
    /// @param levelService         the service for accessing level data
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring-managed singleton; injected services are intentionally shared")
    @Autowired
    public LevelFavoriteController(
            final LevelFavoriteService levelFavoriteService,
            final UserService userService,
            final LevelService levelService) {
        this.levelFavoriteService = levelFavoriteService;
        this.userService = userService;
        this.levelService = levelService;
    }

    /// Adds the specified level to the authenticated user's favorites.
    ///
    /// @spec.requires authentication and levelId are not null.
    /// @spec.effects creates a new LevelFavorite for the authenticated user
    ///               and the given level if no such favorite exists yet;
    ///               otherwise no operation is performed.
    /// @param authentication abstract token for authentication
    /// @param levelId        the id of the level to favorite
    /// @return a 204 No Content response on success
    /// @throws UserNotFoundException if no user with the authenticated id exists
    /// @throws LevelNotFoundException if no level with the given levelId exists
    @PutMapping("/{levelId}/favorite")
    public ResponseEntity<Void> addFavorite(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        final Level level = this.levelService.getById(levelId).orElseThrow(LevelNotFoundException::new);
        this.levelFavoriteService.addFavorite(user, level);
        return ResponseEntity.noContent().build();
    }

    /// Removes the specified level from the authenticated user's favorites.
    /// Does not require the underlying level to still exist; users can
    /// remove favorites whose levels have been deleted.
    ///
    /// @spec.requires authentication and levelId are not null.
    /// @spec.effects deletes the LevelFavorite for the authenticated user
    ///               and the given levelId if one exists; otherwise no
    ///               operation is performed (idempotent).
    /// @param authentication abstract token for authentication
    /// @param levelId        the id of the level to unfavorite
    /// @return a 204 No Content response on success
    /// @throws UserNotFoundException if no user with the authenticated id exists
    @DeleteMapping("/{levelId}/favorite")
    public ResponseEntity<Void> removeFavorite(
            final Authentication authentication,
            @PathVariable final String levelId) {
        final String userId = AuthUtils.getUserIdFromAuth(authentication);
        final User user = this.userService.getById(userId).orElseThrow(UserNotFoundException::new);
        this.levelFavoriteService.removeFavorite(user, levelId);
        return ResponseEntity.noContent().build();
    }
}