package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import ch.usi.inf.bsc.sa4.lab02spring.service.SpriteCatalogService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;
import java.util.Optional;

/// Tests for the [AssetController].
@SpringBootTest
@AutoConfigureMockMvc
@Import(ControllerSecurityTestConfig.class)
@SuppressWarnings({ "PMD.UnitTestShouldIncludeAssert" })
@DisplayName("The Asset Controller")
class AssetControllerTests {

    /// Mocked SpriteCatalogService.
    @MockitoBean
    private SpriteCatalogService spriteCatalogService;

    /// The RestTestClient.
    @Autowired
    private RestTestClient restTestClient;

    /// Valid existing spritesheet type.
    private static final String TYPE_CHARACTERS = "characters";
    
    /// Unknown spritesheet type.
    private static final String TYPE_UNKNOWN = "unknown";
    
    /// Header name for cache control.
    private static final String CACHE_CONTROL_HEADER = "Cache-Control";
    
    /// Header value for cache control.
    private static final String CACHE_CONTROL_VALUE = "max-age=3600, public";

    /// Tests for fetching spritesheets.
    @Nested
    @DisplayName("GET /assets/spritesheets")
    class GetSpritesheets {

        /// Tests when the spritesheet type exists.
        @Test
        @DisplayName("should return 200 OK and payload with cache headers")
        void typeExists() {
            final Map<String, Object> payload = Map.of("frames", Map.of("sprite1", Map.of()));
            Mockito.when(spriteCatalogService.getPayload(TYPE_CHARACTERS)).thenReturn(Optional.of(payload));

            restTestClient.get()
                    .uri("/assets/spritesheets?type={type}", TYPE_CHARACTERS)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().valueEquals(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE)
                    .expectBody(Map.class).isEqualTo(payload);
        }

        /// Tests when the spritesheet type does not exist.
        @Test
        @DisplayName("should return 404 Not Found when type does not exist")
        void typeDoesNotExist() {
            Mockito.when(spriteCatalogService.getPayload(TYPE_UNKNOWN)).thenReturn(Optional.empty());

            restTestClient.get()
                    .uri("/assets/spritesheets?type={type}", TYPE_UNKNOWN)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }
}
