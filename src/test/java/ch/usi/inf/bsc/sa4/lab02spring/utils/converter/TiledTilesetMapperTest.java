package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TiledTilesetMapper.buildTileset")
@SuppressWarnings("NullAway")
class TiledTilesetMapperTest {

    private static final String FIRSTGID = "firstgid";
    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String TILES_KEY = "tiles";
    private static final String ID_KEY = "id";
    private static final String PROPERTIES_KEY = "properties";
    private static final String OBJECTGROUP_KEY = "objectgroup";

    /// Builds a minimal valid TileSet with the given tile count and supplied tile data.
    private static TileSet tileSet(final int tilecount, final List<TileSet.TileData> tiles) {
        return new TileSet(
            1,                  // firstgid
            "atlas",            // name
            128,                // tilewidth
            128,                // tileheight
            tilecount,          // tilecount
            8,                  // columns
            "atlas.png",        // image
            1024,               // imagewidth
            1024,               // imageheight
            0,                  // margin
            0,                  // spacing
            tiles
        );
    }

    private static TileSet.TileData tile(final int id, final String type) {
        return new TileSet.TileData(id, type, null, null);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> tilesOf(final Map<String, Object> tileset) {
        return (List<Map<String, Object>>) tileset.get(TILES_KEY);
    }

    // --------------------------------------------------------------------
    // Boundary: empty tileset
    // --------------------------------------------------------------------

    @Test
    @DisplayName("returns empty tiles list when tilecount is 0 and no tiles are provided")
    void emptyTileset() {
        final TileSet input = tileSet(0, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(List.of(), tilesOf(result));
    }

    // --------------------------------------------------------------------
    // Normal: id mapping (present vs default fill)
    // --------------------------------------------------------------------

    @Test
    @DisplayName("returns the input tile when its id matches the only output slot")
    void singleTileMatchingId() {
        final TileSet input = tileSet(1, List.of(tile(0, "ground")));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        final List<Map<String, Object>> tiles = tilesOf(result);
        assertEquals(1, tiles.size());
        assertEquals(0, tiles.get(0).get(ID_KEY));
        assertEquals("ground", tiles.get(0).get(TYPE_KEY));
    }

    @Test
    @DisplayName("preserves order of input tiles when ids are contiguous")
    void contiguousIds() {
        final TileSet input = tileSet(3, List.of(
            tile(0, "a"),
            tile(1, "b"),
            tile(2, "c")
        ));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        final List<Map<String, Object>> tiles = tilesOf(result);
        assertEquals(3, tiles.size());
        assertEquals("a", tiles.get(0).get(TYPE_KEY));
        assertEquals("b", tiles.get(1).get(TYPE_KEY));
        assertEquals("c", tiles.get(2).get(TYPE_KEY));
    }

    @Test
    @DisplayName("fills missing ids with default tiles having id and empty type")
    void gapsAreFilledWithDefaults() {
        final TileSet input = tileSet(3, List.of(
            tile(0, "a"),
            tile(2, "c")
        ));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        final List<Map<String, Object>> tiles = tilesOf(result);
        assertEquals(3, tiles.size());
        assertEquals("a", tiles.get(0).get(TYPE_KEY));
        // id=1 was missing → expect default
        assertEquals(1, tiles.get(1).get(ID_KEY));
        assertEquals("", tiles.get(1).get(TYPE_KEY));
        assertEquals("c", tiles.get(2).get(TYPE_KEY));
    }

    @Test
    @DisplayName("emits only default tiles when tilecount is positive but no tiles are supplied")
    void allDefaults() {
        final TileSet input = tileSet(2, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        final List<Map<String, Object>> tiles = tilesOf(result);
        assertEquals(2, tiles.size());
        for (int i = 0; i < tiles.size(); i++) {
            assertEquals(i, tiles.get(i).get(ID_KEY));
            assertEquals("", tiles.get(i).get(TYPE_KEY));
        }
    }

    // --------------------------------------------------------------------
    // Top-level field mapping
    // --------------------------------------------------------------------

    @Test
    @DisplayName("copies all top-level fields from the source TileSet")
    void topLevelFieldsAreCopied() {
        final TileSet input = tileSet(0, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(1, result.get(FIRSTGID));
        assertEquals("atlas", result.get(NAME_KEY));
        assertEquals(128, result.get("tilewidth"));
        assertEquals(128, result.get("tileheight"));
        assertEquals(0, result.get("tilecount"));
        assertEquals(8, result.get("columns"));
        assertEquals("atlas.png", result.get("image"));
        assertEquals(1024, result.get("imagewidth"));
        assertEquals(1024, result.get("imageheight"));
        assertEquals(0, result.get("margin"));
        assertEquals(0, result.get("spacing"));
        assertNotNull(result.get(TILES_KEY));
    }

    // --------------------------------------------------------------------
    // toTiledTile branches: type / properties / objectgroup
    // --------------------------------------------------------------------

    @Nested
    @DisplayName("tile field mapping")
    class TileFieldMapping {

        @Test
        @DisplayName("maps null tile type to an empty string")
        void nullTypeBecomesEmpty() {
            final TileSet input = tileSet(1, List.of(tile(0, null)));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertEquals("", tilesOf(result).get(0).get(TYPE_KEY));
        }

        @Test
        @DisplayName("preserves a non-null tile type as-is")
        void nonNullTypePreserved() {
            final TileSet input = tileSet(1, List.of(tile(0, "spawn")));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertEquals("spawn", tilesOf(result).get(0).get(TYPE_KEY));
        }

        @Test
        @DisplayName("omits properties key when tile properties are null")
        void nullPropertiesOmitted() {
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, "x", null, null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertFalse(tilesOf(result).get(0).containsKey(PROPERTIES_KEY));
        }

        @Test
        @DisplayName("omits properties key when tile properties are empty")
        void emptyPropertiesOmitted() {
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, "x", List.of(), null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertFalse(tilesOf(result).get(0).containsKey(PROPERTIES_KEY));
        }

        @Test
        @DisplayName("emits properties as a list of name/type/value maps when supplied")
        @SuppressWarnings("unchecked")
        void propertiesEmittedWhenPresent() {
            final TileSet.Property prop = new TileSet.Property("solid", "bool", true);
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, "wall", List.of(prop), null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            final List<Map<String, Object>> properties =
                (List<Map<String, Object>>) tilesOf(result).get(0).get(PROPERTIES_KEY);
            assertEquals(1, properties.size());
            assertEquals("solid", properties.get(0).get(NAME_KEY));
            assertEquals("bool", properties.get(0).get(TYPE_KEY));
            assertEquals(true, properties.get(0).get("value"));
        }

        @Test
        @DisplayName("omits objectgroup key when tile has no objectgroup")
        void noObjectGroupOmitted() {
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, "x", null, null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertFalse(tilesOf(result).get(0).containsKey(OBJECTGROUP_KEY));
        }

        @Test
        @DisplayName("emits objectgroup with all fields when supplied")
        void objectGroupEmittedWhenPresent() {
            final TileSet.ObjectGroup group = new TileSet.ObjectGroup(
                "topdown", "collision", List.of(), 1, "objectgroup", true, 0, 0
            );
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, "wall", null, group)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertNotNull(tilesOf(result).get(0).get(OBJECTGROUP_KEY));
        }
    }

    // --------------------------------------------------------------------
    // toTiledTileObject branches: polygon
    // --------------------------------------------------------------------

    @Nested
    @DisplayName("tile-object polygon mapping")
    class PolygonMapping {

        private static TileSet tilesetWithObject(final TileSet.TileObject object) {
            final TileSet.ObjectGroup group = new TileSet.ObjectGroup(
                "topdown", "collision", List.of(object), 1, "objectgroup", true, 0, 0
            );
            return tileSet(1, List.of(
                new TileSet.TileData(0, "wall", null, group)
            ));
        }

        @SuppressWarnings("unchecked")
        private static Map<String, Object> firstTileObject(final Map<String, Object> result) {
            final Map<String, Object> objectGroup =
                (Map<String, Object>) tilesOf(result).get(0).get(OBJECTGROUP_KEY);
            return ((List<Map<String, Object>>) objectGroup.get("objects")).get(0);
        }

        @Test
        @DisplayName("omits polygon key when tile object has no polygon")
        void nullPolygonOmitted() {
            final TileSet.TileObject obj = new TileSet.TileObject(
                1, "o", "t", true, 0.0, 0.0, 0.0, 0.0, 0.0, null
            );

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(tilesetWithObject(obj));

            assertFalse(firstTileObject(result).containsKey("polygon"));
        }

        @Test
        @DisplayName("omits polygon key when polygon list is empty")
        void emptyPolygonOmitted() {
            final TileSet.TileObject obj = new TileSet.TileObject(
                1, "o", "t", true, 0.0, 0.0, 0.0, 0.0, 0.0, List.of()
            );

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(tilesetWithObject(obj));

            assertFalse(firstTileObject(result).containsKey("polygon"));
        }

        @Test
        @DisplayName("emits polygon as a list of x/y point maps when supplied")
        @SuppressWarnings("unchecked")
        void polygonEmittedWhenPresent() {
            final TileSet.TileObject obj = new TileSet.TileObject(
                1, "o", "t", true, 0.0, 0.0, 0.0, 0.0, 0.0,
                List.of(new TileSet.Point(1.5, 2.5), new TileSet.Point(3.5, 4.5))
            );

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(tilesetWithObject(obj));

            final List<Map<String, Object>> polygon =
                (List<Map<String, Object>>) firstTileObject(result).get("polygon");
            assertEquals(2, polygon.size());
            assertEquals(1.5, polygon.get(0).get("x"));
            assertEquals(2.5, polygon.get(0).get("y"));
            assertEquals(3.5, polygon.get(1).get("x"));
            assertEquals(4.5, polygon.get(1).get("y"));
        }
    }

    // --------------------------------------------------------------------
    // JSON field-order contract (frontend consumes ordered output)
    // --------------------------------------------------------------------

    @Test
    @DisplayName("emits exactly the Tiled-expected tileset keys")
    void emitsExpectedKeys() {
        final TileSet input = tileSet(0, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(
            java.util.Set.of(
                FIRSTGID, NAME_KEY, "tilewidth", "tileheight", "tilecount",
                "columns", "image", "imagewidth", "imageheight",
                "margin", "spacing", TILES_KEY
            ),
            result.keySet()
        );
    }

    // --------------------------------------------------------------------
    // Sanity: result is non-null
    // --------------------------------------------------------------------

    @Test
    @DisplayName("never returns null for a valid TileSet input")
    void resultIsNonNull() {
        assertNotNull(TiledTilesetMapper.buildTileset(tileSet(0, List.of())));
    }

    // --------------------------------------------------------------------
    // Suppress-related sanity check (should be removed if assert moves)
    // --------------------------------------------------------------------

    @Test
    @DisplayName("default tile contains exactly the id and type keys")
    void defaultTileHasOnlyIdAndType() {
        final TileSet input = tileSet(1, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        final Map<String, Object> defaultTile = tilesOf(result).get(0);
        assertEquals(2, defaultTile.size());
        assertTrue(defaultTile.containsKey(ID_KEY));
        assertTrue(defaultTile.containsKey(TYPE_KEY));
        assertNull(defaultTile.get("dummy"));
    }
}
