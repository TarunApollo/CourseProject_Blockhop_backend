package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

///
/// Configures application security.
///
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    /// Check out application.yml for all ENV vars used for the URLs.
    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);
    private final String frontendBaseUrl;

    public SecurityConfiguration(@Value("${app.frontend-base-url}") final String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
        log.info("OAuth frontend success URL configured as {}", frontendBaseUrl);
    }

    /// Creates the CORS configuration used by the application.
    /// @return the configured CorsConfigurationSource
    ///
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendBaseUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(Boolean.TRUE);  // required for session cookies

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /// Builds the security filter chain for HTTP requests.
    /// @param http the HTTP security configuration
    /// @return the configured security filter chain
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) {
        return http.csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        // Allow unauthenticated CSRF token retrieval for frontend setup.
                        .requestMatchers("/csrf").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(auth ->
                        auth.defaultSuccessUrl(frontendBaseUrl, true))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
    }
}
