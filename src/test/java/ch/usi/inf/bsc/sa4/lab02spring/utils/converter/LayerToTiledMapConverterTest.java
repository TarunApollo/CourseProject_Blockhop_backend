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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

/// Unit tests for the LayerToTiledMapConverter top-level pipeline.
/// Verifies static metadata, level-derived metadata, ClearCondition properties, and composition.
@DisplayName("LayerToTiledMapConverter.convertPipeline")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "PMD.AtLeastOneConstructor"})
class LayerToTiledMapConverterTest {

    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String VALUE_KEY = "value";
    private static final String LAYERS_KEY = "layers";
    private static final String TILESETS_KEY = "tilesets";
    private static final String PROPERTIES_KEY = "properties";
    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";
    private static final String DOOR_OPEN_KEY = "doorOpen";
    private static final String INFINITE_KEY = "infinite";
    private static final String NEXT_LAYER_ID_KEY = "nextlayerid";
    private static final String NEXT_OBJECT_ID_KEY = "nextobjectid";
    private static final String CLEAR_CONDITION_TYPE_NAME = "ClearConditionType";
    private static final String CLEAR_CONDITION_AMOUNT_NAME = "ClearConditionAmount";
    private static final String NONE_VALUE = "NONE";
    private static final String ATLAS_NAME = "atlas";
    private static final String FLAG_TYPE = "flag";
    private static final String LEVEL_TITLE = "title";
    private static final String LEVEL_DESC = "description";
    private static final String OWNER_ID = "user-1";
    private static final String OWNER_NAME = "Mario";
    private static final int TILE_DIM = 128;
    private static final int IMG_DIM = 1024;

    @Mock private TileSetService tileSetService;

    /// Builds a default level for tests.
    private static Level newLevel() {
        return new Level(LEVEL_TITLE, LEVEL_DESC, new User(OWNER_ID, OWNER_NAME));
    }

    /// Builds a default tileset for tests.
    private static TileSet newTileSet() {
        return new TileSet(
            1, ATLAS_NAME, TILE_DIM, TILE_DIM, 0, 8,
            "atlas.png", IMG_DIM, IMG_DIM, 0, 0, List.of()
        );
    }

    /// Casts the properties entry of the result map to a typed list of property maps.
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> propertiesOf(final Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get(PROPERTIES_KEY);
    }

    /// Casts the layers entry of the result map to a typed list of layer maps.
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> layersOf(final Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get(LAYERS_KEY);
    }

    /// Casts the tilesets entry of the result map to a typed list of tileset maps.
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> tilesetsOf(final Map<String, Object> result) {
        return (List<Map<String, Object>>) result.get(TILESETS_KEY);
    }

    /// Sanity test so static analyzers see at least one top-level @Test on the class.
    @Test
    @DisplayName("the pipeline returns a non-null payload for the default fixtures")
    void pipelineReturnsNonNull() {
        assertNotNull(
            LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService));
    }

    // ====================================================================
    // Static metadata fields (from MAP_METADATA constant)
    // ====================================================================

    /// Tests for the static MAP_METADATA contribution to the output map.
    @Nested
    @DisplayName("static map metadata")
    class StaticMetadata {

        /// Helper to invoke the pipeline with the default fixtures.
        private Map<String, Object> invokePipeline() {
            return LayerToTiledMapConverter.convertPipeline(
                newLevel(), newTileSet(), tileSetService);
        }

        /// Tiled map type should always be "map".
        @Test
        @DisplayName("emits type='map'")
        void mapType() {
            assertEquals("map", invokePipeline().get(TYPE_KEY));
        }

        /// Tiled orientation should always be "orthogonal".
        @Test
        @DisplayName("emits orientation='orthogonal'")
        void orientation() {
            assertEquals("orthogonal", invokePipeline().get("orientation"));
        }

        /// Tiled render order should always be "right-down".
        @Test
        @DisplayName("emits renderorder='right-down'")
        void renderOrder() {
            assertEquals("right-down", invokePipeline().get("renderorder"));
        }

        /// Tile width should always be 128.
        @Test
        @DisplayName("emits tilewidth=128")
        void tileWidth() {
            assertEquals(TILE_DIM, invokePipeline().get("tilewidth"));
        }

        /// Tile height should always be 128.
        @Test
        @DisplayName("emits tileheight=128")
        void tileHeight() {
            assertEquals(TILE_DIM, invokePipeline().get("tileheight"));
        }

        /// Tiled spec version should always be "1.10".
        @Test
        @DisplayName("emits version='1.10'")
        void version() {
            assertEquals("1.10", invokePipeline().get("version"));
        }

        /// Tiled editor version should always be "1.10.1".
        @Test
        @DisplayName("emits tiledversion='1.10.1'")
        void tiledVersion() {
            assertEquals("1.10.1", invokePipeline().get("tiledversion"));
        }

        /// Compression level should always be -1.
        @Test
        @DisplayName("emits compressionlevel=-1")
        void compressionLevel() {
            assertEquals(-1, invokePipeline().get("compressionlevel"));
        }
    }

    // ====================================================================
    // Dynamic metadata derived from level
    // ====================================================================

    /// Tests for dynamic metadata fields derived from the source level.
    @Nested
    @DisplayName("level-derived metadata")
    class LevelMetadata {

        /// width should be copied from level.getWidth().
        @Test
        @DisplayName("copies width from level.getWidth()")
        void copiesWidth() {
            final Level level = newLevel();

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(level.getWidth(), result.get(WIDTH_KEY));
        }

        /// height should be copied from level.getHeight().
        @Test
        @DisplayName("copies height from level.getHeight()")
        void copiesHeight() {
            final Level level = newLevel();

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(level.getHeight(), result.get(HEIGHT_KEY));
        }

        /// nextlayerid should always be 3.
        @Test
        @DisplayName("emits a fixed nextlayerid of 3")
        void nextLayerId() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(3, result.get(NEXT_LAYER_ID_KEY));
        }

        /// nextobjectid should be 1 for an empty object layer.
        @Test
        @DisplayName("computes nextobjectid as 1 for an empty level")
        void nextObjectIdEmpty() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(1, result.get(NEXT_OBJECT_ID_KEY));
        }

        /// nextobjectid should be objectLayer size + 1 when objects are present.
        @Test
        @DisplayName("computes nextobjectid as objectLayer size plus 1 when objects are present")
        void nextObjectIdWithObjects() {
            lenient().when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Level level = newLevel();
            final Position p1 = new Position(1, 1);
            final Position p2 = new Position(2, 2);
            level.putObjectLayer(p1, new StartFlag(1, p1));
            level.putObjectLayer(p2, new StartFlag(2, p2));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(3, result.get(NEXT_OBJECT_ID_KEY));
        }

        /// infinite should always be FALSE.
        @Test
        @DisplayName("always emits infinite=false")
        void notInfinite() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(Boolean.FALSE, result.get(INFINITE_KEY));
        }
    }

    // ====================================================================
    // doorOpen flag (depends on clear condition)
    // ====================================================================

    /// Tests for the doorOpen flag derived from the level's clear condition.
    @Nested
    @DisplayName("doorOpen flag")
    class DoorOpenFlag {

        /// NoClearCondition (default) should set doorOpen=true.
        @Test
        @DisplayName("is true when the level has a NoClearCondition (default)")
        void doorOpenWhenNoClearCondition() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(Boolean.TRUE, result.get(DOOR_OPEN_KEY));
        }

        /// SomeClearCondition should set doorOpen=false.
        @Test
        @DisplayName("is false when the level has a SomeClearCondition")
        void doorClosedWhenSomeClearCondition() {
            final Level level = newLevel();
            level.setClearCondition(new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 3));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(Boolean.FALSE, result.get(DOOR_OPEN_KEY));
        }
    }

    // ====================================================================
    // properties (ClearConditionType + ClearConditionAmount)
    // ====================================================================

    /// Tests for the ClearCondition-derived properties.
    @Nested
    @DisplayName("ClearCondition properties")
    class ClearConditionProperties {

        /// The first property should always describe ClearConditionType.
        @Test
        @DisplayName("the first property has name='ClearConditionType' and type='string'")
        void clearConditionTypePropertyShape() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            final Map<String, Object> typeProp = propertiesOf(result).get(0);
            assertEquals(CLEAR_CONDITION_TYPE_NAME, typeProp.get(NAME_KEY));
            assertEquals("string", typeProp.get(TYPE_KEY));
        }

        /// NoClearCondition should map to value="NONE".
        @Test
        @DisplayName("emits value='NONE' for a NoClearCondition")
        void noneValueForNoClearCondition() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(NONE_VALUE, propertiesOf(result).get(0).get(VALUE_KEY));
        }

        /// SomeClearCondition should map to its target name.
        @Test
        @DisplayName("emits the target's name as value for a SomeClearCondition")
        void someTypeFromTarget() {
            final Level level = newLevel();
            level.setClearCondition(new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.COIN), 5));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(ClearConditionType.COIN.name(),
                propertiesOf(result).get(0).get(VALUE_KEY));
        }

        /// The second property should always describe ClearConditionAmount.
        @Test
        @DisplayName("the second property has name='ClearConditionAmount' and type='int'")
        void clearConditionAmountPropertyShape() {
            final Level level = newLevel();
            level.setClearCondition(new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 7));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            final Map<String, Object> amountProp = propertiesOf(result).get(1);
            assertEquals(CLEAR_CONDITION_AMOUNT_NAME, amountProp.get(NAME_KEY));
            assertEquals("int", amountProp.get(TYPE_KEY));
        }

        /// The amount property should carry the level's targetAmount value.
        @Test
        @DisplayName("the second property carries the integer targetAmount value")
        void clearConditionAmountValue() {
            final Level level = newLevel();
            level.setClearCondition(new ClearCondition(
                new Condition.SomeClearCondition(ClearConditionType.SLIME), 7));

            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(level, newTileSet(), tileSetService);

            assertEquals(7, propertiesOf(result).get(1).get(VALUE_KEY));
        }

        /// There should always be exactly two ClearCondition properties.
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

    /// Tests for the layers and tilesets composition entries.
    @Nested
    @DisplayName("layers and tilesets composition")
    class Composition {

        /// layers should contain exactly two entries.
        @Test
        @DisplayName("layers contains exactly two entries")
        void layersHasTwoEntries() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(2, layersOf(result).size());
        }

        /// First layer entry should be the world (tilelayer) layer.
        @Test
        @DisplayName("first layer entry is the world tilelayer")
        void firstLayerIsWorld() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals("tilelayer", layersOf(result).get(0).get(TYPE_KEY));
        }

        /// Second layer entry should be the object (objectgroup) layer.
        @Test
        @DisplayName("second layer entry is the object objectgroup")
        void secondLayerIsObjectGroup() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals("objectgroup", layersOf(result).get(1).get(TYPE_KEY));
        }

        /// tilesets should contain exactly one entry.
        @Test
        @DisplayName("tilesets contains exactly the converted tileset")
        void tilesetsHasOneEntry() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(1, tilesetsOf(result).size());
        }

        /// The single tileset should carry the source name.
        @Test
        @DisplayName("the tileset carries the source name")
        void tilesetCarriesName() {
            final Map<String, Object> result =
                LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

            assertEquals(ATLAS_NAME, tilesetsOf(result).get(0).get(NAME_KEY));
        }
    }

    // ====================================================================
    // Top-level key set
    // ====================================================================

    /// The top-level key set should match the Tiled-expected names.
    @Test
    @DisplayName("emits exactly the Tiled-expected top-level keys")
    void emitsExpectedTopLevelKeys() {
        final Map<String, Object> result =
            LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

        assertEquals(
            Set.of(
                TYPE_KEY, "orientation", "renderorder", "tilewidth", "tileheight",
                "version", "tiledversion", "compressionlevel",
                WIDTH_KEY, HEIGHT_KEY, NEXT_LAYER_ID_KEY, NEXT_OBJECT_ID_KEY,
                INFINITE_KEY, DOOR_OPEN_KEY, PROPERTIES_KEY,
                LAYERS_KEY, TILESETS_KEY
            ),
            result.keySet()
        );
    }

    // ====================================================================
    // Sanity: tileset's tiles list is part of the result
    // ====================================================================

    /// The tileset entry should expose a tiles list (from the delegated tileset mapper).
    @Test
    @DisplayName("delegates tileset conversion so the tileset map exposes a tiles list")
    void tilesetExposesTilesList() {
        final Map<String, Object> result =
            LayerToTiledMapConverter.convertPipeline(newLevel(), newTileSet(), tileSetService);

        assertInstanceOf(List.class, tilesetsOf(result).get(0).get("tiles"));
    }
}
