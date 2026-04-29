package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;

/// Shared helpers for controller tests that use real Spring Security filters.
public final class ControllerSecurityTestSupport {

    /// Some fake bearer token value accepted by mocked JWT decoders.
    public static final String TOKEN = "test-token";

    /// Some fake authorization header value used for authenticated requests.
    public static final String AUTHORIZATION = "Bearer " + TOKEN;

    /// Some fake CSRF token value sent in both the cookie and header.
    private static final String CSRF_TOKEN = "csrf-token";

    /// Some fake name of the CSRF cookie used by `CookieCsrfTokenRepository`.
    private static final String CSRF_COOKIE = "XSRF-TOKEN";

    /// Some fake name of the CSRF header used by `CookieCsrfTokenRepository`.
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";

    /// JWT "issued at" timestamp used for test
    /// tokens. https://docs.oracle.com/javase/8/docs/api/java/time/Instant.html#EPOCH
    private static final Instant ISSUED_AT = Instant.EPOCH;

    /// JWT exipration timestamp used for test tokens. Add ample time to
    /// complete tests.
    private static final Instant EXPIRES_AT = Instant.EPOCH.plusSeconds(3600);

    private ControllerSecurityTestSupport() {
        throw new UnsupportedOperationException("utility class");
    }

    /// Configures a mocked JWT decoder to accept the shared bearer token.
    ///
    /// @param jwtDecoder mocked decoder used by the security filter
    /// @param userId     authenticated user's subject claim
    /// @param userName   authenticated user's display name claim
    public static void mockJwtDecoder(final JwtDecoder jwtDecoder,
            final String userId, final String userName) {
        final Jwt jwt = Jwt.withTokenValue(TOKEN)
                .header("alg", "none")
                .claim("sub", userId)
                .claim("name", userName)
                .issuedAt(ISSUED_AT)
                .expiresAt(EXPIRES_AT)
                .build();
        Mockito.when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
    }

    /// Adds authentication and CSRF data to a request that can carry a body.
    ///
    /// @param request request being prepared
    /// @return the same request with auth and CSRF data added
    public static RestTestClient.RequestBodySpec withAuthAndCsrf(
            final RestTestClient.RequestBodySpec request) {
        return request.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION)
                .cookie(CSRF_COOKIE, CSRF_TOKEN)
                .header(CSRF_HEADER, CSRF_TOKEN);
    }

    /// Adds authentication and CSRF data to a request without a body.
    ///
    /// @param request request being prepared
    /// @return the same request with auth and CSRF data added
    public static RestTestClient.RequestHeadersSpec<?> withAuthAndCsrf(
            final RestTestClient.RequestHeadersSpec<?> request) {
        return request.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION)
                .cookie(CSRF_COOKIE, CSRF_TOKEN)
                .header(CSRF_HEADER, CSRF_TOKEN);
    }
}
