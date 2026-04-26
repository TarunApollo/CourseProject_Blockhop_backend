package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import com.mongodb.MongoClientSettings;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test configuration that overrides production beans for integration tests.
 *
 * <p>Replaces the production SecurityConfiguration with a permissive
 * filter chain that does not require OAuth2. Also provides a
 * MongoClientSettings bean that Flapdoodle's EmbeddedMongoAutoConfiguration
 * requires but Spring Boot 4.x no longer exposes automatically.</p>
 */
@TestConfiguration
/* package */ public class TestSecurityConfig {

    /**
     * Permits all requests without OAuth2, overriding the production
     * SecurityConfiguration which requires OAuth2 client beans.
     */
    @Bean
    @Primary
    /* default */ SecurityFilterChain filterChain(final HttpSecurity http)
            throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll())
                .build();
    }

    /**
     * Provides a MongoClientSettings bean required by Flapdoodle's
     * EmbeddedMongoAutoConfiguration in Spring Boot 4.x, which no
     * longer auto-exposes this bean.
     */
    @Bean
    @Primary
    /* default */ MongoClientSettings mongoClientSettings() {
        return MongoClientSettings.builder().build();
    }
}
