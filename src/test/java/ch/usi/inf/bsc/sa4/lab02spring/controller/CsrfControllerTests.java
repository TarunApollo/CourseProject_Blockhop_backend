package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Objects;

/// Black-box tests for [CsrfController].
/// Verifies that the CSRF token is correctly exposed to clients.
@WebMvcTest(controllers = CsrfController.class)
@AutoConfigureRestTestClient
@Import(ControllerSecurityTestConfig.class)
@DisplayName("The Csrf Controller")
class CsrfControllerTests {

    /// Local DTO to represent the JSON structure of a CSRF token.
    private record CsrfResponse(String token, String headerName, String parameterName) {
    }

    /// The fake authenticated user ID.
    private static final String USER_ID = "userid1";

    /// The fake authenticated user name.
    private static final String USER_NAME = "Test User";

    /// Mocked decoder used by the resource-server security filter.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    /// The RestTestClient for performing requests.
    @Autowired
    private RestTestClient restTestClient;

    /// Configures the mocked JWT decoder before each test.
    @BeforeEach
    void setupJwt() {
        ControllerSecurityTestSupport.mockJwtDecoder(this.jwtDecoder, USER_ID, USER_NAME);
    }

    /// Tests for GET /csrf.
    @Nested
    @DisplayName("GET /csrf")
    class GetCsrf {

        /// Verifies that the endpoint returns 200 OK and a valid CSRF token structure.
        @Test
        @DisplayName("should return 200 OK and a valid CSRF token structure")
        void returnsCsrfToken() {
            final CsrfResponse body = ControllerSecurityTestSupport.withAuthAndCsrf(restTestClient.get().uri("/csrf"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CsrfResponse.class)
                    .returnResult()
                    .getResponseBody();

            final CsrfResponse checkedBody = Objects.requireNonNull(body, "Response body should not be null");

            Assertions.assertAll(
                    () -> Assertions.assertEquals("X-CSRF-TOKEN", checkedBody.headerName()),
                    () -> Assertions.assertEquals("_csrf", checkedBody.parameterName()),
                    () -> Assertions.assertNotNull(checkedBody.token(), "CSRF token should not be null")
            );
        }
    }
}
