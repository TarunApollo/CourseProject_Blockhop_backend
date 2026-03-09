package ch.usi.inf.bsc.sa4.lab02spring.utils;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

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
            } else if (principal instanceof
                    OAuth2User oauth2User) {
                if (oauth2User.getAttribute("sub") == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }
                return oauth2User.getAttribute("sub");
            }
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    public static String getLevelIdFromAuth(Authenticaion authentication)
}
