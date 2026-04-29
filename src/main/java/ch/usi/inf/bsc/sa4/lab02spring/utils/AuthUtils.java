package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import com.mongodb.lang.Nullable;

/// Authentication helper methods for extracting user information.
public final class AuthUtils {

    private AuthUtils() {
    }

    /// Returns the authenticated user's id from the security context. Supports both
    /// JWT and OAuth2 user principals.
    /// 
    /// @param authentication the current authentication object
    /// @return the authenticated user's subject/id
    /// @throws ResponseStatusException if authentication is missing or unsupported
    public static String getUserIdFromAuth(@Nullable final Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else {
            final Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt jwt) {
                return jwt.getClaimAsString("sub");
            } else if (principal instanceof OAuth2User oauth2User) {
                if (oauth2User.getAttribute("sub") == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }
                return oauth2User.getAttribute("sub");
            }
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /// Returns the authenticated user's display name from the security context.
    /// 
    /// @param authentication the current authentication object
    /// @return the authenticated user's name
    /// @throws ResponseStatusException if the name is unavailable
    public static String getUserNameFromAuth(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            final String name = jwt.getClaimAsString("name");
            if (name != null) {
                return name;
            }
        } else if (principal instanceof OAuth2User oAuth2User) {
            final String name = oAuth2User.getAttribute("name");
            if (name != null) {
                return name;
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user name not available");
    }

}
