package ch.usi.inf.bsc.sa4.lab02spring.controller;

import ch.usi.inf.bsc.sa4.lab02spring.service.SpriteCatalogService;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileCatalogService;
import ch.usi.inf.bsc.sa4.lab02spring.service.EditorPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/// REST controller for serving game assets.
@RestController
@RequestMapping("/assets")
public class AssetController {

    /// Service used to retrieve sprite catalog data.
    private final SpriteCatalogService spriteCatalogService;

    /// Service used to retrieve tile catalog data.
    private final TileCatalogService tileCatalogService;

    /// Service used to retrieve the editor policy data.
    private final EditorPolicyService editorPolicyService;

    /// Creates a new AssetController.
    ///
    /// @param spriteCatalogService the service to use
    /// @param tileCatalogService   the tile catalog service to use
    /// @param editorPolicyService  the editor policy service to use
    @Autowired
    public AssetController(
            final SpriteCatalogService spriteCatalogService,
            final TileCatalogService tileCatalogService,
            final EditorPolicyService editorPolicyService) {
        this.spriteCatalogService = spriteCatalogService;
        this.tileCatalogService = tileCatalogService;
        this.editorPolicyService = editorPolicyService;
    }

    /// Serves spritesheet metadata in the atlas format used by the frontend game
    /// engine.
    ///
    /// @spec.requires type is not null.
    /// @param type the type of spritesheet to load (e.g., characters, enemies,
    ///             tiles)
    /// @return a 200 OK response containing the spritesheet metadata if found, or a
    ///         404 Not Found response otherwise
    @GetMapping("/spritesheets")
    public ResponseEntity<Map<String, Object>> getSpritesheet(@RequestParam final String type) {
        return spriteCatalogService.getPayload(type)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /// Serves the backend tile catalog used by the editor as source of truth.
    ///
    /// @spec.modifies nothing.
    /// @spec.effects returns tile_catalog.json entries wrapped as { tiles: [...] }.
    /// @return a 200 OK response containing the tile catalog entries
    @GetMapping("/tile-catalog")
    public ResponseEntity<Map<String, Object>> getTileCatalog() {
        return tileCatalogService.getPayload()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /// Serves the backend editor policy rules and validation criteria.
    ///
    /// @spec.modifies nothing.
    /// @spec.effects returns editor_policy.json entries.
    /// @return a 200 OK response containing the editor policy entries
    @GetMapping("/editor-policy")
    public ResponseEntity<Map<String, Object>> getEditorPolicy() {
        return editorPolicyService.getPayload()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
