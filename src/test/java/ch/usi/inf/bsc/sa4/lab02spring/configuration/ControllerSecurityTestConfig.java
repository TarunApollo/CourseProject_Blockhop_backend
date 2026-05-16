package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

/// Shared security add-on for `@WebMvcTest` controller tests.
///
/// Configures MockMvc with a default OAuth2 user so
/// MockMvc-backed `RestTestClient` requests are authenticated by default.
/// CSRF is handled by the production `CookieCsrfTokenRepository` from
/// `SecurityConfiguration`. A test-only argument resolver ensures
/// `@AuthenticationPrincipal OAuth2User` is consistently injected.
@TestConfiguration
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "PMD.TooManyStaticImports" })
public class ControllerSecurityTestConfig {

    /// The default authenticated user ID used by controller tests.
    public static final String DEFAULT_USER_ID = "userid1";

    /// The default authenticated user display name used by controller tests.
    public static final String DEFAULT_USER_NAME = "Test User";

    /// Configures MockMvc with a default CSRF token and OAuth2 login state.
    /// I believe that this is what Prof. Mocci was showing me in class.
    /// However, the addition of CSRF complicates things considerably due to missing
    ///
    /// @return a customizer that applies the default security post-processors
    @Bean
    /* default */ 
    MockMvcBuilderCustomizer securityDefaults() {
        return builder -> builder.defaultRequest(get("/")
                .with(csrf())
                .with(oauth2Login()
                        .attributes(attrs -> attrs.putAll(Map.of("sub", DEFAULT_USER_ID, "name", DEFAULT_USER_NAME)))));
    }

    /// Provides a `RestTestClient` backed by the auto-configured `MockMvc`.
    /// Tests can inject this directly with `@Autowired` instead of building
    /// it manually via `RestTestClient.bindTo(mockMvc).build()`.
    @Bean
    /* default */ 
    RestTestClient restTestClient(final MockMvc mockMvc) {
        return RestTestClient.bindTo(mockMvc).build();
    }

}
