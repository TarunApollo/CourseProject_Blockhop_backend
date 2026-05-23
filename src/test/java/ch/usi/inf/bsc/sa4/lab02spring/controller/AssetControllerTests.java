package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.service.SpriteCatalogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import ch.usi.inf.bsc.sa4.lab02spring.configuration.ControllerSecurityTestConfig;
import org.springframework.context.annotation.Import;
import java.util.Map;

/// Tests for [AssetController].
@SpringBootTest
@AutoConfigureMockMvc
@Import(ControllerSecurityTestConfig.class)
@DisplayName("AssetController Tests")
class AssetControllerTests {

    /// Mocked sprite catalog service.
    @MockitoBean
    private SpriteCatalogService spriteCatalogService;

    /// Client for testing REST endpoints.
    @Autowired
    private RestTestClient restTestClient;

    @Test
    @DisplayName("returns 200 OK with cache headers for existing spritesheet")
    void returnsSpritesheet() {
        final Map<String, Object> payload = Map.of("frames", Map.of("sprite1", Map.of()));
        Mockito.when(spriteCatalogService.getPayload("characters")).thenReturn(payload);

        restTestClient.get()
                .uri("/api/assets/spritesheets/characters")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Cache-Control", "max-age=3600, public")
                .expectBody().jsonPath("$.frames.sprite1").exists();
    }

    @Test
    @DisplayName("returns 404 Not Found for non-existing spritesheet")
    void returnsNotFound() {
        Mockito.when(spriteCatalogService.getPayload("unknown")).thenReturn(null);

        restTestClient.get()
                .uri("/api/assets/spritesheets/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }
}
