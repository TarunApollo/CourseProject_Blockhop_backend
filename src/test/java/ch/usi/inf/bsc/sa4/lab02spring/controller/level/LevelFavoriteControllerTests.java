package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelFavoriteService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.utils.AuthUtils;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

/// Black-box tests for LevelFavoriteController.
/// Verifies endpoints for adding and removing favorite levels.
@WebMvcTest(controllers = LevelFavoriteController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureRestTestClient
@DisplayName("Level Favorite Controller Logic Tests")
class LevelFavoriteControllerTests {

    /// The authenticated user ID used across tests.
    private static final String USER_ID = "userid1";

    /// A level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// The mocked favorite service.
    @MockitoBean
    private LevelFavoriteService levelFavoriteService;

    /// The mocked user service.
    @MockitoBean
    private UserService userService;

    /// The mocked level service.
    @MockitoBean
    private LevelService levelService;

    /// The RestTestClient for performing requests.
    @Autowired
    private RestTestClient restTestClient;

    /// Test user, rebuilt per test.
    private User testUser;

    /// Test level, rebuilt per test.
    private Level testLevel;

    /// Sets up reusable fixtures before each test.
    @BeforeEach
    void setup() {
        this.testUser = new User(USER_ID, "Mario");
        this.testLevel = new Level("Test Level", "desc", this.testUser);
    }

    /// Verifies that favoriting a level returns 204 No Content and
    /// delegates to the favorite service with the resolved entities.
    @Test
    @DisplayName("PUT /levels/{id}/favorite should return 204")
    void testAddFavorite() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelService.getById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

            final HttpStatusCode status = restTestClient.put()
                    .uri("/levels/{levelId}/favorite", LEVEL_ID)
                    .exchange()
                    .returnResult(Void.class)
                    .getStatus();

            Assertions.assertEquals(HttpStatus.NO_CONTENT, status);
            Mockito.verify(levelFavoriteService).addFavorite(testUser, testLevel);
        }
    }

    /// Verifies that unfavoriting a level returns 204 No Content and
    /// delegates to the favorite service with the level id (does not
    /// require the level entity to be resolved).
    @Test
    @DisplayName("DELETE /levels/{id}/favorite should return 204")
    void testRemoveFavorite() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));

            final HttpStatusCode status = restTestClient.delete()
                    .uri("/levels/{levelId}/favorite", LEVEL_ID)
                    .exchange()
                    .returnResult(Void.class)
                    .getStatus();

            Assertions.assertEquals(HttpStatus.NO_CONTENT, status);
            Mockito.verify(levelFavoriteService).removeFavorite(testUser, LEVEL_ID);
        }
    }

    /// Verifies that PUT favorite returns 404 when the authenticated
    /// user does not exist, and that the favorite service is not called.
    @Test
    @DisplayName("PUT /levels/{id}/favorite should return 404 when user not found")
    void testAddFavoriteUserNotFound() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.empty());

            restTestClient.put()
                    .uri("/levels/{levelId}/favorite", LEVEL_ID)
                    .exchange()
                    .expectStatus().isNotFound();

            Mockito.verify(levelFavoriteService, Mockito.never())
                    .addFavorite(Mockito.any(User.class), Mockito.any(Level.class));
        }
    }

    /// Verifies that PUT favorite returns 404 when the target level
    /// does not exist, and that the favorite service is not called.
    @Test
    @DisplayName("PUT /levels/{id}/favorite should return 404 when level not found")
    void testAddFavoriteLevelNotFound() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.of(testUser));
            Mockito.when(levelService.getById(LEVEL_ID)).thenReturn(Optional.empty());

            restTestClient.put()
                    .uri("/levels/{levelId}/favorite", LEVEL_ID)
                    .exchange()
                    .expectStatus().isNotFound();

            Mockito.verify(levelFavoriteService, Mockito.never())
                    .addFavorite(Mockito.any(User.class), Mockito.any(Level.class));
        }
    }

    /// Verifies that DELETE favorite returns 404 when the authenticated
    /// user does not exist, and that the favorite service is not called.
    @Test
    @DisplayName("DELETE /levels/{id}/favorite should return 404 when user not found")
    void testRemoveFavoriteUserNotFound() {
        try (MockedStatic<AuthUtils> mockedAuth = Mockito.mockStatic(AuthUtils.class)) {
            mockedAuth.when(() -> AuthUtils.getUserIdFromAuth(Mockito.any())).thenReturn(USER_ID);
            Mockito.when(userService.getById(USER_ID)).thenReturn(Optional.empty());

            restTestClient.delete()
                    .uri("/levels/{levelId}/favorite", LEVEL_ID)
                    .exchange()
                    .expectStatus().isNotFound();

            Mockito.verify(levelFavoriteService, Mockito.never())
                    .removeFavorite(Mockito.any(User.class), Mockito.anyString());
        }
    }
}