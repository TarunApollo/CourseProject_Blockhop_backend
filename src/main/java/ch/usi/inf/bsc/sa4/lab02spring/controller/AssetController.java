package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.service.SpriteCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/// REST controller for serving game assets.
@RestController
@RequestMapping("/assets")
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
    ///
    /// @spec.requires type is not null.
    /// @param type the type of spritesheet to load (e.g., characters, enemies)
    /// @return a 200 OK response containing the spritesheet metadata with cache headers if found,
    ///         or a 404 Not Found response otherwise
    @GetMapping("/spritesheets")
    public ResponseEntity<Map<String, Object>> getSpritesheet(@RequestParam final String type) {
        return spriteCatalogService.getPayload(type)
                .map(payload -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                        .body(payload))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
