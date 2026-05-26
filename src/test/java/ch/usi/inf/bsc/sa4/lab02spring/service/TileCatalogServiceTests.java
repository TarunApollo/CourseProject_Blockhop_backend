package ch.usi.inf.bsc.sa4.lab02spring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.usi.inf.bsc.sa4.lab02spring.model.Entry;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileCatalog;

/// Tests for [TileCatalogService].
@SuppressWarnings("PMD.TooManyStaticImports")
@SpringBootTest
@DisplayName("Tile Catalog Service")
class TileCatalogServiceTests {

    /// Tile ID used for missing tile lookups.
    private static final String MISSING_TILE = "missing.tile";

    /// The service under test.
    @Autowired
    private TileCatalogService service;

    /// Verifies catalog loads at startup.
    @Test
    @DisplayName("loads catalog successfully")
    void loadsCatalog() {
        final TileCatalog catalog = service.getCatalog();
        assertNotNull(catalog);
        assertNotNull(catalog.tiles());
        assertFalse(catalog.tiles().isEmpty());
    }

    /// Verifies tile lists are populated.
    @Test
    @DisplayName("provides loaded tiles")
    void providesLoadedTiles() {
        assertFalse(service.getTiles().isEmpty());
    }

    /// Verifies looking up an existing tile works.
    @Test
    @DisplayName("looks up existing tile")
    void looksUpExistingTile() {
        final Entry firstTile = service.getTiles().get(0);
        final String tileId = firstTile.id();

        final Entry entry = service.requireTile(tileId);
        assertNotNull(entry);
        assertEquals(tileId, entry.id());
    }

    /// Verifies looking up a missing tile fails.
    @Test
    @DisplayName("fails looking up missing tile")
    void failsLookingUpMissingTile() {
        assertThrows(IllegalArgumentException.class, () -> service.requireTile("missing.tile.123"));
    }

    /// Verifies identifying world tiles.
    @Test
    @DisplayName("identifies world tiles")
    void identifiesWorldTiles() {
        // Attempt to find a world tile to test
        final String worldId = service.getTiles().stream()
                .filter(t -> "world".equals(t.layer()))
                .findFirst().map(Entry::id).orElse(null);

        if (worldId != null) {
            assertTrue(service.isWorldTile(worldId));
            assertFalse(service.isObjectTile(worldId));
        }

        assertFalse(service.isWorldTile(MISSING_TILE));
    }

    /// Verifies identifying object tiles.
    @Test
    @DisplayName("identifies object tiles")
    void identifiesObjectTiles() {
        // Attempt to find an object tile to test
        final String objectId = service.getTiles().stream()
                .filter(t -> "object".equals(t.layer()))
                .findFirst().map(Entry::id).orElse(null);

        if (objectId != null) {
            assertTrue(service.isObjectTile(objectId));
            assertFalse(service.isWorldTile(objectId));
        }

        assertFalse(service.isObjectTile(MISSING_TILE));
    }

    /// Verifies getting tile type.
    @Test
    @DisplayName("gets tile type")
    void getsTileType() {
        final Entry firstTile = service.getTiles().get(0);
        final String type = service.getType(firstTile.id());

        assertEquals(firstTile.type(), type);
    }
}
