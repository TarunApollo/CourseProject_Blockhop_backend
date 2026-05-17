package ch.usi.inf.bsc.sa4.lab02spring.controller.level;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelFavoriteService;
import ch.usi.inf.bsc.sa4.lab02spring.service.level.LevelService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import org.mockito.Mockito;

import java.util.Optional;

/// Black-box tests for [LevelFavoriteController] endpoints.
@SpringBootTest
@AutoConfigureMockMvc
@Import(ControllerSecurityTestConfig.class)
@SuppressWarnings({ "PMD.UnitTestShouldIncludeAssert", "PMD.ExcessiveImports" })
@DisplayName("The Level Favorite Controller")
class LevelFavoriteControllerTests {

    /// A level ID used across tests.
    private static final String LEVEL_ID = "level-1";

    /// The endpoint URI template for favorite actions.
    private static final String FAVORITE_URI = "/levels/{levelId}/favorite";

    /// The mocked favorite service.
    @MockitoBean
    private LevelFavoriteService levelFavoriteService;

    /// The mocked user service.
    @MockitoBean
    private UserService userService;

    /// The mocked level service.
    @MockitoBean
    private LevelService levelService;

    /// HTTP client bound to MockMvc.
    @Autowired
    private RestTestClient restTestClient;

    /// Test user, rebuilt per test.
    private User testUser;

    /// Test level, rebuilt per test.
    private Level testLevel;

    /// Sets up reusable fixtures before each test.
    @BeforeEach
    void setup() {
        this.testUser = new User(
                ControllerSecurityTestConfig.DEFAULT_USER_ID,
                ControllerSecurityTestConfig.DEFAULT_USER_NAME);
        this.testLevel = new Level("Test Level", "desc", this.testUser);
    }

    @Test
    @DisplayName("PUT /levels/{id}/favorite should return 204")
    void shouldAddFavorite() {
        Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                .thenReturn(Optional.of(testUser));
        Mockito.when(levelService.getById(LEVEL_ID)).thenReturn(Optional.of(testLevel));

        restTestClient.put()
                .uri(FAVORITE_URI, LEVEL_ID)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(levelFavoriteService).addFavorite(testUser, testLevel);
    }

    @Test
    @DisplayName("DELETE /levels/{id}/favorite should return 204")
    void shouldRemoveFavorite() {
        Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                .thenReturn(Optional.of(testUser));

        restTestClient.delete()
                .uri(FAVORITE_URI, LEVEL_ID)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(levelFavoriteService).removeFavorite(testUser, LEVEL_ID);
    }

    @Test
    @DisplayName("PUT /levels/{id}/favorite should return 404 when user not found")
    void shouldReturnNotFoundWhenAddFavoriteUserMissing() {
        Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                .thenReturn(Optional.empty());

        restTestClient.put()
                .uri(FAVORITE_URI, LEVEL_ID)
                .exchange()
                .expectStatus().isNotFound();

        Mockito.verify(levelFavoriteService, Mockito.never())
                .addFavorite(Mockito.any(User.class), Mockito.any(Level.class));
    }

    @Test
    @DisplayName("PUT /levels/{id}/favorite should return 404 when level not found")
    void shouldReturnNotFoundWhenAddFavoriteLevelMissing() {
        Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                .thenReturn(Optional.of(testUser));
        Mockito.when(levelService.getById(LEVEL_ID)).thenReturn(Optional.empty());

        restTestClient.put()
                .uri(FAVORITE_URI, LEVEL_ID)
                .exchange()
                .expectStatus().isNotFound();

        Mockito.verify(levelFavoriteService, Mockito.never())
                .addFavorite(Mockito.any(User.class), Mockito.any(Level.class));
    }

    @Test
    @DisplayName("DELETE /levels/{id}/favorite should return 404 when user not found")
    void shouldReturnNotFoundWhenRemoveFavoriteUserMissing() {
        Mockito.when(userService.getById(ControllerSecurityTestConfig.DEFAULT_USER_ID))
                .thenReturn(Optional.empty());

        restTestClient.delete()
                .uri(FAVORITE_URI, LEVEL_ID)
                .exchange()
                .expectStatus().isNotFound();

        Mockito.verify(levelFavoriteService, Mockito.never())
                .removeFavorite(Mockito.any(User.class), Mockito.anyString());
    }
}
