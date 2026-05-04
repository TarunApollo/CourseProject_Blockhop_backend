package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.server.ResponseStatusException;

/// Authentication helper methods for extracting OAuth2 user information.
public final class OAuth2UserUtils {

    private OAuth2UserUtils() {
    }

    /// Returns a required string attribute from an authenticated OAuth2 user.
    ///
    /// @param user the authenticated OAuth2 user
    /// @param attribute the required attribute name
    /// @return the attribute value
    /// @throws ResponseStatusException if the attribute is missing
    public static String getRequiredAttribute(final OAuth2User user, final String attribute) {
        // Return unauthorized if no authentication is provided.
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        final String value = user.getAttribute(attribute);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return value;
    }
}
