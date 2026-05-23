package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.service.SpriteCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/// REST controller for serving game assets.
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    /// Service used to retrieve sprite catalog data.
    private final SpriteCatalogService spriteCatalogService;

    /// Creates a new AssetController.
    ///
    /// @param spriteCatalogService the service to use
    @Autowired
    public AssetController(final SpriteCatalogService spriteCatalogService) {
        this.spriteCatalogService = spriteCatalogService;
    }

    /// Serves spritesheet metadata as JSON format compatible with Phaser 3's atlas loaders.
    @GetMapping("/spritesheets/{type}")
    public ResponseEntity<Map<String, Object>> getSpritesheet(@PathVariable("type") final String type) {
        final Map<String, Object> payload = spriteCatalogService.getPayload(type);

        if (payload == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(payload);
    }
}
