package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

// TODO: ask for idiomatic Spring way of doing this
public final class AuthUtils {
    ///
    /// TODO: javadoc!
    ///
    ///
    public static String getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else {
            Object principal = authentication.getPrincipal();
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

    public static String getUserNameFromAuth(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String name = jwt.getClaimAsString("name");
            if (name != null) {
                return name;
            }
        } else if (principal instanceof OAuth2User oAuth2User) {
            String name = oAuth2User.getAttribute("name");
            if (name != null) {
                return name;
            }
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user name not available");
    }


}
