package ch.usi.inf.bsc.sa4.lab02spring.utils;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Black-box tests for AuthUtils.
@DisplayName(" In the AuthUtils class ")
@SuppressWarnings("NullAway")
/* package */ class AuthUtilsTests {

    /** The subject claim used in test OAuth2 users. */
    private static final String TEST_USER_ID = "user-123";

    /** The name claim used in test OAuth2 users. */
    private static final String TEST_USER_NAME = "Test User";

    /** Authority required by DefaultOAuth2User. */
    private static final String TEST_AUTHORITY = "ROLE_USER";

    /** Attribute key for the subject claim. */
    private static final String SUB_KEY = "sub";

    // Creates an OAuth2User with the given attributes.
    private static DefaultOAuth2User createOAuth2User(final Map<String, Object> attributes) {
        return new DefaultOAuth2User(Collections.singleton(() -> TEST_AUTHORITY), attributes, SUB_KEY);
    }

    /** Tests for the static method getUserIdFromAuth. */
    @DisplayName(" method getUserIdFromAuth ")
    @Nested
    class GetUserIdFromAuth {

        // Verifies that a 401 UNAUTHORIZED is thrown when authentication is null.
        @DisplayName("should throw UNAUTHORIZED when authentication is null")
        @Test
        void nullAuthenticationThrows() {
            final Executable call = () -> AuthUtils.getUserIdFromAuth(null);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }

        // Verifies the status code of the exception when auth is null.
        @DisplayName("should return 401 status when authentication is null")
        @Test
        void nullAuthenticationStatus() {
            final Executable call = () -> AuthUtils.getUserIdFromAuth(null);
            final ResponseStatusException exception =
                    Assertions.assertThrows(ResponseStatusException.class, call);
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }

        // Verifies that the correct user ID is returned for a valid OAuth2 user.
        @DisplayName("should return the sub attribute from a valid OAuth2 user")
        @Test
        void validOAuth2User() {
            final DefaultOAuth2User oAuth2User =
                    createOAuth2User(Map.of(SUB_KEY, TEST_USER_ID, "name", TEST_USER_NAME));
            final Authentication authentication =
                    new TestingAuthenticationToken(oAuth2User, null);

            final String result = AuthUtils.getUserIdFromAuth(authentication);
            Assertions.assertEquals(TEST_USER_ID, result);
        }

        // Verifies that a 401 UNAUTHORIZED is thrown when the OAuth2 user
        // has a null "sub" attribute.
        @DisplayName("should throw UNAUTHORIZED when OAuth2 user has null sub attribute")
        @Test
        void nullSubAttributeThrows() {
            final OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute(SUB_KEY)).thenReturn(null);
            final Authentication authentication =
                    new TestingAuthenticationToken(oAuth2User, null);

            final Executable call = () -> AuthUtils.getUserIdFromAuth(authentication);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }

        // Verifies the status code when OAuth2 user has a null sub.
        @DisplayName("should return 401 status when OAuth2 user has null sub")
        @Test
        void nullSubAttributeStatus() {
            final OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute(SUB_KEY)).thenReturn(null);
            final Authentication authentication =
                    new TestingAuthenticationToken(oAuth2User, null);

            final Executable call = () -> AuthUtils.getUserIdFromAuth(authentication);
            final ResponseStatusException exception =
                    Assertions.assertThrows(ResponseStatusException.class, call);
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }

        // Verifies that a 501 NOT_IMPLEMENTED is thrown when the principal
        // is not an OAuth2User.
        @DisplayName("should throw NOT_IMPLEMENTED when principal is not an OAuth2User")
        @Test
        void nonOAuth2PrincipalThrows() {
            final Authentication authentication =
                    new TestingAuthenticationToken("non-oauth2-principal", null);

            final Executable call = () -> AuthUtils.getUserIdFromAuth(authentication);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }

        // Verifies the status code when principal is not an OAuth2User.
        @DisplayName("should return 501 status when principal is not an OAuth2User")
        @Test
        void nonOAuth2PrincipalStatus() {
            final Authentication authentication =
                    new TestingAuthenticationToken("non-oauth2-principal", null);

            final Executable call = () -> AuthUtils.getUserIdFromAuth(authentication);
            final ResponseStatusException exception =
                    Assertions.assertThrows(ResponseStatusException.class, call);
            Assertions.assertEquals(HttpStatus.NOT_IMPLEMENTED, exception.getStatusCode());
        }
    }

    /** Tests for the static method getUserNameFromAuth. */
    @DisplayName(" method getUserNameFromAuth ")
    @Nested
    class GetUserNameFromAuth {

        // Verifies that the correct name is returned for a valid OAuth2 user.
        @DisplayName("should return the name attribute from a valid OAuth2 user")
        @Test
        void validOAuth2User() {
            final DefaultOAuth2User oAuth2User =
                    createOAuth2User(Map.of(SUB_KEY, TEST_USER_ID, "name", TEST_USER_NAME));
            final Authentication authentication =
                    new TestingAuthenticationToken(oAuth2User, null);

            final String result = AuthUtils.getUserNameFromAuth(authentication);
            Assertions.assertEquals(TEST_USER_NAME, result);
        }

        // Verifies that a 401 UNAUTHORIZED is thrown when the OAuth2 user
        // has no "name" attribute.
        @DisplayName("should throw UNAUTHORIZED when OAuth2 user has no name attribute")
        @Test
        void missingNameAttributeThrows() {
            final DefaultOAuth2User oAuth2User =
                    createOAuth2User(Map.of(SUB_KEY, TEST_USER_ID));
            final Authentication authentication =
                    new TestingAuthenticationToken(oAuth2User, null);

            final Executable call = () -> AuthUtils.getUserNameFromAuth(authentication);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }

        // Verifies the status code when OAuth2 user has no name attribute.
        @DisplayName("should return 401 status when OAuth2 user has no name attribute")
        @Test
        void missingNameAttributeStatus() {
            final DefaultOAuth2User oAuth2User =
                    createOAuth2User(Map.of(SUB_KEY, TEST_USER_ID));
            final Authentication authentication =
                    new TestingAuthenticationToken(oAuth2User, null);

            final Executable call = () -> AuthUtils.getUserNameFromAuth(authentication);
            final ResponseStatusException exception =
                    Assertions.assertThrows(ResponseStatusException.class, call);
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }

        // Verifies that a 401 UNAUTHORIZED is thrown when the principal
        // is not an OAuth2User.
        @DisplayName("should throw UNAUTHORIZED when principal is not an OAuth2User")
        @Test
        void nonOAuth2PrincipalThrows() {
            final Authentication authentication =
                    new TestingAuthenticationToken("non-oauth2-principal", null);

            final Executable call = () -> AuthUtils.getUserNameFromAuth(authentication);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }

        // Verifies the status code when principal is not an OAuth2User.
        @DisplayName("should return 401 status when principal is not an OAuth2User")
        @Test
        void nonOAuth2PrincipalStatus() {
            final Authentication authentication =
                    new TestingAuthenticationToken("non-oauth2-principal", null);

            final Executable call = () -> AuthUtils.getUserNameFromAuth(authentication);
            final ResponseStatusException exception =
                    Assertions.assertThrows(ResponseStatusException.class, call);
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }
    }
}
