package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.ClearConditionType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

@DisplayName("LayerToTiledMapConverter.convertPipeline")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class LayerToTiledMapConverterTest {

    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String VALUE_KEY = "value";
    private static final String LAYERS_KEY = "layers";
    private static final String TILESETS_KEY = "tilesets";
    private static final String PROPERTIES_KEY = "properties";

    @Mock private TileSetService tileSetService;

    private static Level newLevel() {
        return new Level("title", "description", new User("user-1", "Mario"));
    }

    private static TileSet newTileSet() {
        return new TileSet(
            1, "atlas", 128, 128, 0, 8,
            "atlas.png", 1024, 1024, 0, 0, List.of()
        );
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> propertiesOf(final Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get(PROPERTIES_KEY);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> layersOf(final Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get(LAYERS_KEY);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> tilesetsOf(final Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get(TILESETS_KEY);
    }

    // ====================================================================
    // Static metadata fields (from MAP_METADATA constant)
    // ====================================================================

    @Nested
    @DisplayName("static map metadata")
    class StaticMetadata {

        @Test
        @DisplayName("emits the Tiled map type and orientation constants")
        void mapTypeConstants() {
            final Map<String, Object> result = TiledLayerMapper_invoke();

            assertEquals("map", result.get(TYPE_KEY));
            assertEquals("orthogonal", result.get("orientation"));
            assertEquals("right-down", result.get("renderorder"));
        }

        @Test
        @DisplayName("emits Tiled tile dimensions of 128x128")
        void tileDimensions() {
            final Map<String, Object> result = TiledLayerMapper_invoke();

            assertEquals(128, result.get("tilewidth"));
            assertEquals(128, result.get("tileheight"));
        }

        @Test
        @DisplayName("emits Tiled version metadata and a -1 compression level")
        void versionMetadata() {
            final Map<String, Object> result = TiledLayerMapper_invoke();

            assertEquals("1.10", result.get("version"));
            assertEquals("1.10.1", result.get("tiledversion"));
            assertEquals(-1, result.get("compressionlevel"));
        }

        private Map<String, Object> TiledLayerMapper_invoke() {
            return LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);
        }
    }

    // ====================================================================
    // Dynamic metadata derived from level
    // ====================================================================

    @Nested
    @DisplayName("level-derived metadata")
    class LevelMetadata {

        @Test
        @DisplayName("copies width and height from the level")
        void widthAndHeight() {
            final Level level = newLevel();

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(level.getWidth(), result.get("width"));
            assertEquals(level.getHeight(), result.get("height"));
        }

        @Test
        @DisplayName("emits a fixed nextlayerid of 3")
        void nextLayerId() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(3, result.get("nextlayerid"));
        }

        @Test
        @DisplayName("computes nextobjectid as objectLayer size plus 1 for an empty level")
        void nextObjectIdEmpty() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(1, result.get("nextobjectid"));
        }

        @Test
        @DisplayName("computes nextobjectid as objectLayer size plus 1 when objects are present")
        void nextObjectIdWithObjects() {
            lenient().when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");
            final Level level = newLevel();
            final Position p1 = new Position(1, 1);
            final Position p2 = new Position(2, 2);
            level.putObjectLayer(p1, new StartFlag(1, p1));
            level.putObjectLayer(p2, new StartFlag(2, p2));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(3, result.get("nextobjectid"));
        }

        @Test
        @DisplayName("always emits infinite=false")
        void notInfinite() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(Boolean.FALSE, result.get("infinite"));
        }
    }

    // ====================================================================
    // doorOpen flag (depends on clear condition)
    // ====================================================================

    @Nested
    @DisplayName("doorOpen flag")
    class DoorOpenFlag {

        @Test
        @DisplayName("is true when the level has a NoClearCondition (default)")
        void doorOpenWhenNoClearCondition() {
            // Level is created with NoClearCondition by default.
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(Boolean.TRUE, result.get("doorOpen"));
        }

        @Test
        @DisplayName("is false when the level has a SomeClearCondition")
        void doorClosedWhenSomeClearCondition() {
            final Level level = newLevel();
            level.setClearCondition(new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3
            ));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(Boolean.FALSE, result.get("doorOpen"));
        }
    }

    // ====================================================================
    // properties (ClearConditionType + ClearConditionAmount)
    // ====================================================================

    @Nested
    @DisplayName("ClearCondition properties")
    class ClearConditionProperties {

        @Test
        @DisplayName("emits ClearConditionType=NONE for a NoClearCondition")
        void noneTypeForNoClearCondition() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            final Map<String, Object> typeProp = propertiesOf(result).get(0);
            assertEquals("ClearConditionType", typeProp.get(NAME_KEY));
            assertEquals("string", typeProp.get(TYPE_KEY));
            assertEquals("NONE", typeProp.get(VALUE_KEY));
        }

        @Test
        @DisplayName("emits ClearConditionType matching the target name for a SomeClearCondition")
        void someTypeFromTarget() {
            final Level level = newLevel();
            level.setClearCondition(new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.COIN), 5
            ));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            final Map<String, Object> typeProp = propertiesOf(result).get(0);
            assertEquals(ClearConditionType.COIN.name(), typeProp.get(VALUE_KEY));
        }

        @Test
        @DisplayName("emits ClearConditionAmount with the integer targetAmount value")
        void clearConditionAmount() {
            final Level level = newLevel();
            level.setClearCondition(new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 7
            ));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            final Map<String, Object> amountProp = propertiesOf(result).get(1);
            assertEquals("ClearConditionAmount", amountProp.get(NAME_KEY));
            assertEquals("int", amountProp.get(TYPE_KEY));
            assertEquals(7, amountProp.get(VALUE_KEY));
        }

        @Test
        @DisplayName("always emits exactly two properties (type and amount)")
        void exactlyTwoProperties() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(2, propertiesOf(result).size());
        }
    }

    // ====================================================================
    // layers + tilesets composition
    // ====================================================================

    @Nested
    @DisplayName("layers and tilesets composition")
    class Composition {

        @Test
        @DisplayName("layers contains exactly two entries: world layer first, object layer second")
        void layersOrder() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            final List<Map<String, Object>> layers = layersOf(result);
            assertEquals(2, layers.size());
            // world layer has type "tilelayer", object layer has type "objectgroup"
            assertEquals("tilelayer", layers.get(0).get(TYPE_KEY));
            assertEquals("objectgroup", layers.get(1).get(TYPE_KEY));
        }

        @Test
        @DisplayName("tilesets contains exactly the converted tileset")
        void tilesetsContent() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            final List<Map<String, Object>> tilesets = tilesetsOf(result);
            assertEquals(1, tilesets.size());
            assertEquals("atlas", tilesets.get(0).get(NAME_KEY));
        }

        @Test
        @DisplayName("returns a non-null result for any valid level and tileset")
        void resultIsNonNull() {
            assertNotNull(
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService)
            );
        }
    }

    // ====================================================================
    // Top-level key order (Tiled JSON contract)
    // ====================================================================

    @Test
    @DisplayName("emits exactly the Tiled-expected top-level keys")
    void emitsExpectedTopLevelKeys() {
        final Map<String, Object> result =
            LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

        assertEquals(
            java.util.Set.of(
                "type", "orientation", "renderorder", "tilewidth", "tileheight",
                "version", "tiledversion", "compressionlevel",
                "width", "height", "nextlayerid", "nextobjectid",
                "infinite", "doorOpen", PROPERTIES_KEY,
                LAYERS_KEY, TILESETS_KEY
            ),
            result.keySet()
        );
    }

    // ====================================================================
    // Sanity: tileset's tiles list is part of the result
    // ====================================================================

    @Test
    @DisplayName("delegates tileset conversion so the tileset map exposes a tiles list")
    void tilesetExposesTilesList() {
        final Map<String, Object> result =
            LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

        final Map<String, Object> tileset = tilesetsOf(result).get(0);
        assertInstanceOf(List.class, tileset.get("tiles"));
    }
}
