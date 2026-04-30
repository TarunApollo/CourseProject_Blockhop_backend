package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.method.annotation.CsrfTokenArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/// Shared security add-on for `@WebMvcTest` controller tests.
///
/// Configures MockMvc with a default OAuth2 user so
/// MockMvc-backed `RestTestClient` requests are authenticated by default.
/// CSRF is handled by the production `CookieCsrfTokenRepository` from
/// `SecurityConfiguration`. A test-only argument resolver ensures
/// `@AuthenticationPrincipal OAuth2User` is consistently injected.
@TestConfiguration
@ImportAutoConfiguration(exclude = OAuth2ClientWebSecurityAutoConfiguration.class)
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class ControllerSecurityTestConfig {

    /// The default authenticated user ID used by controller tests.
    public static final String DEFAULT_USER_ID = "userid1";

    /// The default authenticated user display name used by controller tests.
    public static final String DEFAULT_USER_NAME = "Test User";

    /// Header containing the test user id.
    public static final String USER_ID_HEADER = "X-Test-User-Id";

    /// Header containing the test user name.
    public static final String USER_NAME_HEADER = "X-Test-User-Name";

    /// Configures MockMvc with a default CSRF token and OAuth2 login state.
    /// I believe that this is what Prof. Mocci was showing me in class.
    /// However, the addition of CSRF complicates things considerably due to missing 
    ///
    /// @return a customizer that applies the default security post-processors
    @Bean
    MockMvcBuilderCustomizer securityDefaults() {
        return builder -> builder.defaultRequest(get("/")
                .with(csrf())
                .with(oauth2Login().oauth2User(new DefaultOAuth2User(
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                        Map.of("sub", DEFAULT_USER_ID, "name", DEFAULT_USER_NAME),
                        "sub"))));
    }

    /// Adds MVC resolvers that the `@WebMvcTest` slice does not register itself.
    /// This was needed because patching MockMvc requests alone fixed CSRF submission
    /// for unsafe methods, but `CsrfController#csrf(CsrfToken)` still failed since
    /// the slice had no resolver for `CsrfToken` method arguments.
    /// The OAuth2 resolver reads from test headers when present, falling back to defaults.
    /// Sources:
    /// - Spring Security MVC integration documents `CsrfToken` controller arguments:
    ///   https://docs.spring.io/spring-security/reference/servlet/integrations/mvc.html
    /// - The same page documents `@AuthenticationPrincipal` MVC argument resolution.
    /// 
    /// TODO: Do further research on alternatives to this approach.
    ///
    /// @return MVC configurer for CSRF and OAuth2 principal arguments
    @Bean
    WebMvcConfigurer authenticationPrincipalConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new CsrfTokenArgumentResolver());
                resolvers.add(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(final MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                                && OAuth2User.class.isAssignableFrom(parameter.getParameterType());
                    }

                    @Override
                    public Object resolveArgument(final MethodParameter parameter,
                            final @Nullable ModelAndViewContainer mavContainer,
                            final NativeWebRequest webRequest,
                            final @Nullable WebDataBinderFactory binderFactory) {
                        final String userId = Objects.requireNonNullElse(
                                webRequest.getHeader(USER_ID_HEADER), DEFAULT_USER_ID);
                        final String userName = Objects.requireNonNullElse(
                                webRequest.getHeader(USER_NAME_HEADER), DEFAULT_USER_NAME);
                        return new DefaultOAuth2User(
                                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                                Map.of("sub", userId, "name", userName),
                                "sub");
                    }
                });
            }
        };
    }
}
