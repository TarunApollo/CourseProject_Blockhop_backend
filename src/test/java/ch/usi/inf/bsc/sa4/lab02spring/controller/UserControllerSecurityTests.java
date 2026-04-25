package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.SecurityConfiguration;
import ch.usi.inf.bsc.sa4.lab02spring.service.AttemptService;
import ch.usi.inf.bsc.sa4.lab02spring.service.LevelService;
import ch.usi.inf.bsc.sa4.lab02spring.service.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;

/**
 * Security Slice Tests for the UserController.
 * Goal: Test only the Spring Security filter chain (OAuth2 authentication and CSRF).
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
@DisplayName("User Controller Security Tests")
public class UserControllerSecurityTests {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LevelService levelService;

    @MockitoBean
    private AttemptService attemptService;

    // Infrastructure mocks required by SecurityConfiguration
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvcTester mvc;

    @Test
    @DisplayName("Unauthenticated requests to protected endpoints return 401 Unauthorized")
    public void testUnauthenticatedRequest() {
        mvc.get().uri("/users/me")
           .exchange()
           .assertThat().hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Authenticated GET request passes security filter and returns 200 OK")
    public void testAuthenticatedRequest() {
        mvc.get().uri("/users")
           .with(oauth2Login())
           .exchange()
           .assertThat().hasStatusOk();
    }

    @Test
    @DisplayName("Mutating POST without CSRF token returns 403 Forbidden")
    public void testPostWithoutCsrf() {
        mvc.post().uri("/users")
           .with(oauth2Login())
           .exchange()
           .assertThat().hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Mutating POST with CSRF token passes CSRF filter (does not return 403)")
    public void testPostWithCsrf() {
        // Will pass CSRF filter. Returns 405 Method Not Allowed since POST /users is not explicitly supported 
        // (or similar failure depending on mapping). It specifically does NOT return 403 Forbidden.
        mvc.post().uri("/users")
           .with(oauth2Login())
           .with(csrf())
           .exchange()
           .assertThat().hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }
}
