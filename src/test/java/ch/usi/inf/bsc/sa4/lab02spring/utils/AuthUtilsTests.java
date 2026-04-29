package ch.usi.inf.bsc.sa4.lab02spring.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import org.mockito.Mockito;

/// Unit tests for [AuthUtils].
@DisplayName("In the AuthUtils class")
@SuppressWarnings("NullAway")
class AuthUtilsTests {

    /// The subject claim used in test users.
    private static final String TEST_USER_ID = "user-123";

    /// The name claim used in test users.
    private static final String TEST_USER_NAME = "Test User";

    /// Authority required by DefaultOAuth2User.
    private static final String TEST_AUTHORITY = "ROLE_USER";

    /// Attribute key for the subject claim.
    private static final String SUB_KEY = "sub";

    /// Non-OAuth2 principal used in negative tests.
    private static final String NON_OAUTH_PRINCIPAL = "non-oauth2-principal";

    /// Creates an OAuth2User with the given attributes.
    private static DefaultOAuth2User createOAuth2User(final Map<String, Object> attributes) {
        return new DefaultOAuth2User(Collections.singleton(() -> TEST_AUTHORITY), attributes, SUB_KEY);
    }

    /// Tests for the utility class constructor.
    @DisplayName("constructor")
    @Nested
    class ConstructorTest {

        /// Verifies that the private constructor throws an exception when invoked
        /// via reflection.
        @Test
        @DisplayName("should be private and throw exception on instantiation")
        void testConstructorIsPrivate() throws Exception {
            final Constructor<AuthUtils> constructor = AuthUtils.class.getDeclaredConstructor();
            Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
            constructor.setAccessible(true);
            Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);
        }
    }

    /// Tests for the static method [getUserIdFromAuth].
    @DisplayName("method getUserIdFromAuth")
    @Nested
    class GetUserIdFromAuth {

        /// Verifies that a 401 UNAUTHORIZED is thrown when auth is null.
        @DisplayName("should throw UNAUTHORIZED when authentication is null")
        @Test
        void nullAuthenticationThrows() {
            final Executable call = () -> AuthUtils.getUserIdFromAuth(null);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }

        /// Verifies that the correct user ID is returned for a valid OAuth2 user.
        @DisplayName("should return the sub attribute from a valid OAuth2 user")
        @Test
        void validOAuth2User() {
            final DefaultOAuth2User oAuth2User = createOAuth2User(
                    Map.of(SUB_KEY, TEST_USER_ID, "name", TEST_USER_NAME));
            final Authentication authentication = new TestingAuthenticationToken(oAuth2User, null);

            final String result = AuthUtils.getUserIdFromAuth(authentication);
            Assertions.assertEquals(TEST_USER_ID, result);
        }

        /// Verifies that the correct user ID is returned for a valid Jwt.
        @DisplayName("should return the sub claim from a valid Jwt")
        @Test
        void validJwt() {
            final Jwt jwt = Mockito.mock(Jwt.class);
            Mockito.when(jwt.getClaimAsString("sub")).thenReturn(TEST_USER_ID);
            final Authentication authentication = new TestingAuthenticationToken(jwt, null);

            final String result = AuthUtils.getUserIdFromAuth(authentication);
            Assertions.assertEquals(TEST_USER_ID, result);
        }

        /// Verifies that a 401 is thrown when OAuth2 user has a null sub.
        @DisplayName("should throw UNAUTHORIZED when OAuth2 user has null sub attribute")
        @Test
        void nullSubAttributeThrows() {
            final OAuth2User oAuth2User = Mockito.mock(OAuth2User.class);
            Mockito.when(oAuth2User.getAttribute(SUB_KEY)).thenReturn(null);
            final Authentication authentication = new TestingAuthenticationToken(oAuth2User, null);

            final Executable call = () -> AuthUtils.getUserIdFromAuth(authentication);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }

        /// Verifies that a 501 is thrown when principal is not an OAuth2User or Jwt.
        @DisplayName("should throw NOT_IMPLEMENTED when principal is unsupported")
        @Test
        void nonOAuth2PrincipalThrows() {
            final Authentication authentication = new TestingAuthenticationToken(NON_OAUTH_PRINCIPAL, null);

            final Executable call = () -> AuthUtils.getUserIdFromAuth(authentication);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }
    }

    /// Tests for the static method [getUserNameFromAuth].
    @DisplayName("method getUserNameFromAuth")
    @Nested
    class GetUserNameFromAuth {

        /// Verifies that the correct name is returned for a valid OAuth2 user.
        @DisplayName("should return the name attribute from a valid OAuth2 user")
        @Test
        void validOAuth2User() {
            final DefaultOAuth2User oAuth2User = createOAuth2User(
                    Map.of(SUB_KEY, TEST_USER_ID, "name", TEST_USER_NAME));
            final Authentication authentication = new TestingAuthenticationToken(oAuth2User, null);

            final String result = AuthUtils.getUserNameFromAuth(authentication);
            Assertions.assertEquals(TEST_USER_NAME, result);
        }

        /// Verifies that the correct name is returned for a valid Jwt.
        @DisplayName("should return the name claim from a valid Jwt")
        @Test
        void validJwt() {
            final Jwt jwt = Mockito.mock(Jwt.class);
            Mockito.when(jwt.getClaimAsString("name")).thenReturn(TEST_USER_NAME);
            final Authentication authentication = new TestingAuthenticationToken(jwt, null);

            final String result = AuthUtils.getUserNameFromAuth(authentication);
            Assertions.assertEquals(TEST_USER_NAME, result);
        }

        /// Verifies that a 401 is thrown when OAuth2 user has no name attribute.
        @DisplayName("should throw UNAUTHORIZED when OAuth2 user has no name attribute")
        @Test
        void missingNameAttributeThrows() {
            final DefaultOAuth2User oAuth2User = createOAuth2User(Map.of(SUB_KEY, TEST_USER_ID));
            final Authentication authentication = new TestingAuthenticationToken(oAuth2User, null);

            final Executable call = () -> AuthUtils.getUserNameFromAuth(authentication);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }

        /// Verifies that a 401 is thrown when principal is not an OAuth2User or Jwt.
        @DisplayName("should throw UNAUTHORIZED when principal is unsupported")
        @Test
        void nonOAuth2PrincipalThrows() {
            final Authentication authentication = new TestingAuthenticationToken(NON_OAUTH_PRINCIPAL, null);

            final Executable call = () -> AuthUtils.getUserNameFromAuth(authentication);
            Assertions.assertThrows(ResponseStatusException.class, call);
        }
    }
}
