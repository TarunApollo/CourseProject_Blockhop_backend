package ch.usi.inf.bsc.sa4.lab02spring.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

///
/// Exposes the current CSRF token to clients.
///
@RestController
public class CsrfController {
    /// Returns the CSRF token associated with the current request.
    /// @param csrfToken the injected CSRF token
    /// @return the current CSRF token
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}
