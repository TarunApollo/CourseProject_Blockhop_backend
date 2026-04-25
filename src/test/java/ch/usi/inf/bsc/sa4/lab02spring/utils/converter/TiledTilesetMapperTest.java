package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Unit tests for the TiledTilesetMapper utility.
@DisplayName("TiledTilesetMapper.buildTileset")
@SuppressWarnings({"NullAway", "PMD.AtLeastOneConstructor"})
class TiledTilesetMapperTest {

    /// JSON key for the Tiled `firstgid` field.
    private static final String FIRSTGID = "firstgid";
    /// JSON key for the Tiled `name` field.
    private static final String NAME_KEY = "name";
    /// JSON key for the Tiled `type` field.
    private static final String TYPE_KEY = "type";
    /// JSON key for the Tiled `value` field on properties.
    private static final String VALUE_KEY = "value";
    /// JSON key for the Tiled `visible` field.
    private static final String VISIBLE_KEY = "visible";
    /// JSON key for the Tiled `tiles` array on tilesets.
    private static final String TILES_KEY = "tiles";
    /// JSON key for the Tiled `objects` array on object groups.
    private static final String OBJECTS_KEY = "objects";
    /// JSON key for the Tiled `polygon` array on tile objects.
    private static final String POLYGON_KEY = "polygon";
    /// JSON key for the Tiled `id` field.
    private static final String ID_KEY = "id";
    /// JSON key for the Tiled `x` field.
    private static final String X_KEY = "x";
    /// JSON key for the Tiled `y` field.
    private static final String Y_KEY = "y";
    /// JSON key for the Tiled `properties` array on tiles.
    private static final String PROPERTIES_KEY = "properties";
    /// JSON key for the Tiled `objectgroup` field on tiles.
    private static final String OBJECTGROUP_KEY = "objectgroup";
    /// Default tileset name used by fixtures.
    private static final String ATLAS_NAME = "atlas";
    /// Default tileset image path used by fixtures.
    private static final String ATLAS_IMAGE = "atlas.png";
    /// Default object-group draw order used by fixtures.
    private static final String GROUP_DRAWORDER = "topdown";
    /// Default object-group display name used by fixtures.
    private static final String GROUP_NAME = "collision";
    /// Default object-group type used by fixtures.
    private static final String GROUP_TYPE = "objectgroup";
    /// Sample tile-type string used in fixtures.
    private static final String WALL_TYPE = "wall";
    /// Default tile pixel dimension used by fixtures.
    private static final int TILE_DIM = 128;
    /// Default tileset image pixel dimension used by fixtures.
    private static final int IMG_DIM = 1024;

    /// Builds a minimal valid TileSet with the given tile count and supplied tile data.
    private static TileSet tileSet(final int tilecount, final List<TileSet.TileData> tiles) {
        return new TileSet(
            1,
            ATLAS_NAME,
            TILE_DIM,
            TILE_DIM,
            tilecount,
            8,
            ATLAS_IMAGE,
            IMG_DIM,
            IMG_DIM,
            0,
            0,
            tiles
        );
    }

    /// Builds a TileData with no properties and no objectgroup.
    private static TileSet.TileData tile(final int id, final String type) {
        return new TileSet.TileData(id, type, null, null);
    }

    /// Centralized unchecked cast for the various nested generic Tiled JSON shapes.
    @SuppressWarnings("unchecked")
    private static <T> T cast(final Object value) {
        return (T) value;
    }

    /// Casts the tiles entry of a tileset map to a typed list of tile maps.
    private static List<Map<String, Object>> tilesOf(final Map<String, Object> tileset) {
        return cast(tileset.get(TILES_KEY));
    }

    /// Sanity test so static analyzers see at least one top-level @Test on the class.
    @Test
    @DisplayName("the mapper returns a non-null payload for a minimal valid input")
    void mapperReturnsNonNull() {
        assertNotNull(TiledTilesetMapper.buildTileset(tileSet(0, List.of())));
    }

    // --------------------------------------------------------------------
    // Boundary: empty tileset
    // --------------------------------------------------------------------

    /// Empty tile list with tilecount 0 should produce an empty tiles array.
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

    /// Single input tile occupying the only output slot should be emitted at index 0.
    @Test
    @DisplayName("returns the input tile when its id matches the only output slot")
    void singleTileMatchingId() {
        final TileSet input = tileSet(1, List.of(tile(0, "ground")));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(1, tilesOf(result).size());
    }

    /// The single emitted tile should expose its source type.
    @Test
    @DisplayName("the single emitted tile carries its source type")
    void singleTileCarriesType() {
        final TileSet input = tileSet(1, List.of(tile(0, "ground")));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals("ground", tilesOf(result).get(0).get(TYPE_KEY));
    }

    /// Three contiguous-id tiles should produce one output per input slot.
    @Test
    @DisplayName("emits one output per input when ids are contiguous")
    void contiguousIdsCount() {
        final TileSet input = tileSet(3, List.of(
            tile(0, "a"), tile(1, "b"), tile(2, "c")
        ));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(3, tilesOf(result).size());
    }

    /// Three contiguous-id tiles should be emitted in slot order.
    @Test
    @DisplayName("preserves slot order of contiguous-id input tiles")
    void contiguousIdsOrder() {
        final TileSet input = tileSet(3, List.of(
            tile(0, "a"), tile(1, "b"), tile(2, "c")
        ));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        final List<Map<String, Object>> tiles = tilesOf(result);
        assertAll(
            () -> assertEquals("a", tiles.get(0).get(TYPE_KEY)),
            () -> assertEquals("b", tiles.get(1).get(TYPE_KEY)),
            () -> assertEquals("c", tiles.get(2).get(TYPE_KEY))
        );
    }

    /// Missing slot ids should be filled with default tiles carrying the slot id.
    @Test
    @DisplayName("default tile fills missing id with the slot index")
    void defaultTileHasSlotId() {
        final TileSet input = tileSet(3, List.of(
            tile(0, "a"), tile(2, "c")
        ));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(1, tilesOf(result).get(1).get(ID_KEY));
    }

    /// Default tile must carry an empty type string.
    @Test
    @DisplayName("default tile carries an empty type string")
    void defaultTileHasEmptyType() {
        final TileSet input = tileSet(3, List.of(
            tile(0, "a"), tile(2, "c")
        ));

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals("", tilesOf(result).get(1).get(TYPE_KEY));
    }

    /// Tilecount with no input tiles should yield only default-tile placeholders.
    @Test
    @DisplayName("emits only default tiles when tilecount is positive but no tiles are supplied")
    void allDefaultsCount() {
        final TileSet input = tileSet(2, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(2, tilesOf(result).size());
    }

    /// Each default tile should carry its own slot id.
    @Test
    @DisplayName("each default tile carries its slot id")
    void allDefaultsCarrySlotId() {
        final TileSet input = tileSet(3, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        final List<Map<String, Object>> tiles = tilesOf(result);
        assertAll(
            () -> assertEquals(0, tiles.get(0).get(ID_KEY)),
            () -> assertEquals(1, tiles.get(1).get(ID_KEY)),
            () -> assertEquals(2, tiles.get(2).get(ID_KEY))
        );
    }

    // --------------------------------------------------------------------
    // Top-level field mapping
    // --------------------------------------------------------------------

    /// All scalar top-level fields should be copied from the source TileSet.
    @Test
    @DisplayName("copies firstgid, name, and dimension fields from the source TileSet")
    void topLevelScalarFields() {
        final TileSet input = tileSet(0, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertAll(
            () -> assertEquals(1, result.get(FIRSTGID)),
            () -> assertEquals(ATLAS_NAME, result.get(NAME_KEY)),
            () -> assertEquals(TILE_DIM, result.get("tilewidth")),
            () -> assertEquals(TILE_DIM, result.get("tileheight"))
        );
    }

    /// Image-related fields should be copied from the source TileSet.
    @Test
    @DisplayName("copies image-related fields from the source TileSet")
    void topLevelImageFields() {
        final TileSet input = tileSet(0, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertAll(
            () -> assertEquals(ATLAS_IMAGE, result.get("image")),
            () -> assertEquals(IMG_DIM, result.get("imagewidth")),
            () -> assertEquals(IMG_DIM, result.get("imageheight"))
        );
    }

    /// Layout fields (margin, spacing, columns, tilecount) should be copied.
    @Test
    @DisplayName("copies layout fields from the source TileSet")
    void topLevelLayoutFields() {
        final TileSet input = tileSet(0, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertAll(
            () -> assertEquals(0, result.get("tilecount")),
            () -> assertEquals(8, result.get("columns")),
            () -> assertEquals(0, result.get("margin")),
            () -> assertEquals(0, result.get("spacing"))
        );
    }

    /// The tiles entry should always be present (possibly empty).
    @Test
    @DisplayName("always emits a tiles entry, even if empty")
    void alwaysEmitsTilesEntry() {
        final TileSet input = tileSet(0, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertNotNull(result.get(TILES_KEY));
    }

    // --------------------------------------------------------------------
    // toTiledTile branches: type / properties / objectgroup
    // --------------------------------------------------------------------

    /// Tests for the per-tile field mapping branches.
    @Nested
    @DisplayName("tile field mapping")
    class TileFieldMapping {

        /// A null tile type should be normalized to an empty string.
        @Test
        @DisplayName("maps null tile type to an empty string")
        void nullTypeBecomesEmpty() {
            final TileSet input = tileSet(1, List.of(tile(0, null)));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertEquals("", tilesOf(result).get(0).get(TYPE_KEY));
        }

        /// A non-null tile type should be passed through unchanged.
        @Test
        @DisplayName("preserves a non-null tile type as-is")
        void nonNullTypePreserved() {
            final TileSet input = tileSet(1, List.of(tile(0, "spawn")));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertEquals("spawn", tilesOf(result).get(0).get(TYPE_KEY));
        }

        /// Null properties on the source tile should suppress the properties entry.
        @Test
        @DisplayName("omits properties key when tile properties are null")
        void nullPropertiesOmitted() {
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, "x", null, null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertFalse(tilesOf(result).get(0).containsKey(PROPERTIES_KEY));
        }

        /// An empty properties list on the source tile should suppress the properties entry.
        @Test
        @DisplayName("omits properties key when tile properties are empty")
        void emptyPropertiesOmitted() {
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, "x", List.of(), null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertFalse(tilesOf(result).get(0).containsKey(PROPERTIES_KEY));
        }

        /// A tile with one property should emit a properties list of size 1.
        @Test
        @DisplayName("emits a properties list of size 1 for a single source property")
        void propertiesListSize() {
            final TileSet.Property prop = new TileSet.Property("solid", "bool", true);
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, WALL_TYPE, List.of(prop), null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            final List<Map<String, Object>> properties =
                cast(tilesOf(result).get(0).get(PROPERTIES_KEY));
            assertEquals(1, properties.size());
        }

        /// The emitted property should carry name, type, and value from the source.
        @Test
        @DisplayName("emitted property carries name, type, and value from the source")
        void propertyFieldsCopied() {
            final TileSet.Property prop = new TileSet.Property("solid", "bool", true);
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, WALL_TYPE, List.of(prop), null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            final List<Map<String, Object>> properties =
                cast(tilesOf(result).get(0).get(PROPERTIES_KEY));
            final Map<String, Object> emitted = properties.get(0);
            assertAll(
                () -> assertEquals("solid", emitted.get(NAME_KEY)),
                () -> assertEquals("bool", emitted.get(TYPE_KEY)),
                () -> assertEquals(Boolean.TRUE, emitted.get(VALUE_KEY))
            );
        }

        /// A null objectgroup on the source tile should suppress the objectgroup entry.
        @Test
        @DisplayName("omits objectgroup key when tile has no objectgroup")
        void noObjectGroupOmitted() {
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, "x", null, null)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertFalse(tilesOf(result).get(0).containsKey(OBJECTGROUP_KEY));
        }

        /// A non-null objectgroup should be passed through to the output.
        @Test
        @DisplayName("emits objectgroup when supplied")
        void objectGroupEmittedWhenPresent() {
            final TileSet.ObjectGroup group = new TileSet.ObjectGroup(
                GROUP_DRAWORDER, GROUP_NAME, List.of(), 1, GROUP_TYPE, true, 0, 0
            );
            final TileSet input = tileSet(1, List.of(
                new TileSet.TileData(0, WALL_TYPE, null, group)
            ));

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

            assertNotNull(tilesOf(result).get(0).get(OBJECTGROUP_KEY));
        }
    }

    // --------------------------------------------------------------------
    // toTiledTileObject branches: polygon
    // --------------------------------------------------------------------

    /// Tests for tile-object polygon mapping.
    @Nested
    @DisplayName("tile-object polygon mapping")
    class PolygonMapping {

        /// Helper to build a tileset that wraps a single tile object.
        private static TileSet tilesetWithObject(final TileSet.TileObject object) {
            final TileSet.ObjectGroup group = new TileSet.ObjectGroup(
                GROUP_DRAWORDER, GROUP_NAME, List.of(object), 1, GROUP_TYPE, true, 0, 0
            );
            return tileSet(1, List.of(
                new TileSet.TileData(0, WALL_TYPE, null, group)
            ));
        }

        /// Helper to extract the first tile-object emitted in the result.
        private static Map<String, Object> firstTileObject(final Map<String, Object> result) {
            final Map<String, Object> objectGroup = cast(tilesOf(result).get(0).get(OBJECTGROUP_KEY));
            final List<Map<String, Object>> objects = cast(objectGroup.get(OBJECTS_KEY));
            return objects.get(0);
        }

        /// A null polygon on the source object should suppress the polygon entry.
        @Test
        @DisplayName("omits polygon key when tile object has no polygon")
        void nullPolygonOmitted() {
            final TileSet.TileObject obj = new TileSet.TileObject(
                1, "o", "t", true, 0.0, 0.0, 0.0, 0.0, 0.0, null
            );

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(tilesetWithObject(obj));

            assertFalse(firstTileObject(result).containsKey(POLYGON_KEY));
        }

        /// An empty polygon list on the source object should suppress the polygon entry.
        @Test
        @DisplayName("omits polygon key when polygon list is empty")
        void emptyPolygonOmitted() {
            final TileSet.TileObject obj = new TileSet.TileObject(
                1, "o", "t", true, 0.0, 0.0, 0.0, 0.0, 0.0, List.of()
            );

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(tilesetWithObject(obj));

            assertFalse(firstTileObject(result).containsKey(POLYGON_KEY));
        }
        /// A non-empty polygon should produce a list of the same size.
        @Test
        @DisplayName("polygon list has the same size as the source polygon")
        void polygonHasSameSize() {
            final TileSet.TileObject obj = new TileSet.TileObject(
                1, "o", "t", true, 0.0, 0.0, 0.0, 0.0, 0.0,
                List.of(new TileSet.Point(1.5, 2.5), new TileSet.Point(3.5, 4.5))
            );

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(tilesetWithObject(obj));

            final List<Map<String, Object>> polygon = cast(firstTileObject(result).get(POLYGON_KEY));
            assertEquals(2, polygon.size());
        }

        /// Each polygon point should carry the source x and y values.
        @Test
        @DisplayName("each polygon point carries the source x and y values")
        void polygonPointsCopied() {
            final TileSet.TileObject obj = new TileSet.TileObject(
                1, "o", "t", true, 0.0, 0.0, 0.0, 0.0, 0.0,
                List.of(new TileSet.Point(1.5, 2.5), new TileSet.Point(3.5, 4.5))
            );

            final Map<String, Object> result = TiledTilesetMapper.buildTileset(tilesetWithObject(obj));

            final List<Map<String, Object>> polygon = cast(firstTileObject(result).get(POLYGON_KEY));
            assertAll(
                () -> assertEquals(1.5, polygon.get(0).get(X_KEY)),
                () -> assertEquals(2.5, polygon.get(0).get(Y_KEY)),
                () -> assertEquals(3.5, polygon.get(1).get(X_KEY)),
                () -> assertEquals(4.5, polygon.get(1).get(Y_KEY))
            );
        }
    }

    // --------------------------------------------------------------------
    // Top-level key set
    // --------------------------------------------------------------------

    /// The top-level key set should match the Tiled-expected names exactly.
    @Test
    @DisplayName("emits exactly the Tiled-expected tileset keys")
    void emitsExpectedKeys() {
        final TileSet input = tileSet(0, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(
            Set.of(
                FIRSTGID, NAME_KEY, "tilewidth", "tileheight", "tilecount",
                "columns", "image", "imagewidth", "imageheight",
                "margin", "spacing", TILES_KEY
            ),
            result.keySet()
        );
    }

    // --------------------------------------------------------------------
    // Default tile shape
    // --------------------------------------------------------------------

    /// Default tile should expose exactly two keys: id and type.
    @Test
    @DisplayName("default tile contains exactly two keys: id and type")
    void defaultTileHasExactlyTwoKeys() {
        final TileSet input = tileSet(1, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertEquals(2, tilesOf(result).get(0).size());
    }

    /// Default tile should expose the id key.
    @Test
    @DisplayName("default tile exposes the id key")
    void defaultTileHasIdKey() {
        final TileSet input = tileSet(1, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertTrue(tilesOf(result).get(0).containsKey(ID_KEY));
    }

    /// Default tile should expose the type key.
    @Test
    @DisplayName("default tile exposes the type key")
    void defaultTileHasTypeKey() {
        final TileSet input = tileSet(1, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertTrue(tilesOf(result).get(0).containsKey(TYPE_KEY));
    }

    /// Default tile must not expose unknown keys.
    @Test
    @DisplayName("default tile does not expose unrelated keys")
    void defaultTileLacksUnknownKey() {
        final TileSet input = tileSet(1, List.of());

        final Map<String, Object> result = TiledTilesetMapper.buildTileset(input);

        assertNull(tilesOf(result).get(0).get(VISIBLE_KEY));
    }
}
