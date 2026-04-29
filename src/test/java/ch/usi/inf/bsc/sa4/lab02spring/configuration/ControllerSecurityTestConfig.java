package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;

/// Shared security add-on for `@WebMvcTest` controller tests.
///
/// Why this exists: `@WebMvcTest` is a *slice test*. Spring Boot intentionally
/// loads only a restricted subset of the **application context** so controller
/// tests stay fast and focused on the MVC layer. The Spring Boot testing
/// reference explicitly says that slice annotations load a "very restricted set
/// of auto-configured classes" and that additional autoconf can be added with
/// `@ImportAutoConfiguration`.
///
/// In this project, that matters because these controller tests do not just call
/// controller methods directly. They send requests through Spring Security's
/// real servlet filter chain using `RestTestClient`, a bearer token, and CSRF
/// cookie/header values. For that to work, the MVC slice must include the
/// servlet-side web security infrastructure that creates `HttpSecurity` and
/// allows a `SecurityFilterChain` bean to be built inside the test context.
///
/// What we endu up with is still much leaner than a full `@SpringBootTest`.
///
/// The result is that controller tests can exercise:
/// - Spring MVC request handling
/// - Spring Security servlet filters
/// - JWT bearer-token authentication via a mocked `JwtDecoder`
/// - CSRF validation via `CookieCsrfTokenRepository`
///
/// References and interesting reads:
/// - Mockmvc vs Webmvctest and their inner workings:
///   https://www.baeldung.com/spring-mockmvc-vs-webmvctest
/// - Spring Boot testing reference on slice tests and
///   `@ImportAutoConfiguration`:
///   <https://docs.spring.io/spring-boot/4.0-SNAPSHOT/reference/testing/spring-boot-applications.html>
/// - Spring Boot API docs for `@WebMvcTest`:
///   <https://docs.spring.io/spring-boot/4.0-SNAPSHOT/api/java/org/springframework/boot/webmvc/test/autoconfigure/WebMvcTest.html>
/// - Spring Boot API docs for
///   `ServletWebSecurityAutoConfiguration`: <https://docs.spring.io/spring-boot/4.0-SNAPSHOT/api/java/org/springframework/boot/security/autoconfigure/web/servlet/ServletWebSecurityAutoConfiguration.html>
@SuppressWarnings("PMD.AtLeastOneConstructor")
@TestConfiguration
@ImportAutoConfiguration({
        ServletWebSecurityAutoConfiguration.class
})
public class ControllerSecurityTestConfig {
}
