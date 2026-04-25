package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/// Unit tests for the TiledLayerMapper utility.
/// Verifies world-layer placement and object-layer construction including service-driven type lookup.
@DisplayName("TiledLayerMapper")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "PMD.AtLeastOneConstructor"})
class TiledLayerMapperTest {

    /// Pixel size of a Tiled tile.
    private static final int TILE_SIZE = 128;
    /// JSON key for the Tiled `name` field.
    private static final String NAME_KEY = "name";
    /// JSON key for the Tiled `type` field.
    private static final String TYPE_KEY = "type";
    /// JSON key for the Tiled `visible` field.
    private static final String VISIBLE_KEY = "visible";
    /// JSON key for the Tiled `objects` array on object layers.
    private static final String OBJECTS_KEY = "objects";
    /// JSON key for the Tiled `properties` array on tile objects.
    private static final String PROPERTIES_KEY = "properties";
    /// JSON key for the Tiled `data` array on tile layers.
    private static final String DATA_KEY = "data";
    /// JSON key for the Tiled `id` field.
    private static final String ID_KEY = "id";
    /// JSON key for the Tiled `gid` field.
    private static final String GID_KEY = "gid";
    /// JSON key for the Tiled `width` field.
    private static final String WIDTH_KEY = "width";
    /// JSON key for the Tiled `height` field.
    private static final String HEIGHT_KEY = "height";
    /// JSON key for the Tiled `opacity` field.
    private static final String OPACITY_KEY = "opacity";
    /// JSON key for the Tiled `x` field.
    private static final String X_KEY = "x";
    /// JSON key for the Tiled `y` field.
    private static final String Y_KEY = "y";
    /// JSON key for the Tiled `rotation` field.
    private static final String ROTATION_KEY = "rotation";
    /// JSON key for the Tiled `draworder` field on object layers.
    private static final String DRAWORDER_KEY = "draworder";
    /// JSON key for the Tiled `value` field on properties.
    private static final String VALUE_KEY = "value";
    /// Sample object-tile type used by start-flag fixtures.
    private static final String FLAG_TYPE = "flag";
    /// Sample object-tile type used by box fixtures.
    private static final String BOX_TYPE = "box";
    /// Tiled-layer name produced for the world layer.
    private static final String WORLD_LAYER_NAME = "World";
    /// Tiled-layer name produced for the object layer.
    private static final String OBJECT_LAYER_NAME = "QMLayer";
    /// Tiled `type` value emitted for tile layers.
    private static final String TILELAYER_TYPE = "tilelayer";
    /// Tiled `type` value emitted for object layers.
    private static final String OBJECTGROUP_TYPE = "objectgroup";
    /// Tiled `draworder` value emitted on object layers.
    private static final String DRAWORDER_TOPDOWN = "topdown";

    /// Casts the objects entry of an object-layer map to a typed list of object maps.
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> objectsOf(final Map<String, Object> layer) {
        return (List<Map<String, Object>>) layer.get(OBJECTS_KEY);
    }

    /// Casts the data entry of a world-layer map to a list of integer cells.
    @SuppressWarnings("unchecked")
    private static List<Integer> dataOf(final Map<String, Object> layer) {
        return (List<Integer>) layer.get(DATA_KEY);
    }

    /// Sanity test so static analyzers see at least one top-level @Test on the class.
    @Test
    @DisplayName("buildWorldLayer of an empty layer returns a non-null result")
    void emptyWorldLayerReturnsNonNull() {
        assertNotNull(TiledLayerMapper.buildWorldLayer(Map.of(), 1, 1));
    }

    // ====================================================================
    // buildWorldLayer (no service dependency)
    // ====================================================================

    /// Tests for the buildWorldLayer entry point.
    @Nested
    @DisplayName("buildWorldLayer")
    class BuildWorldLayer {

        /// Empty layer should produce a data list of width*height length.
        @Test
        @DisplayName("data list size equals width*height for an empty world layer")
        void emptyWorldLayerSize() {
            final Map<Position, GroundObject> input = Map.of();

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 3, 2);

            assertEquals(6, dataOf(result).size());
        }

        /// Empty layer cells should all be zero.
        @Test
        @DisplayName("all data cells are zero for an empty world layer")
        void emptyWorldLayerZeros() {
            final Map<Position, GroundObject> input = Map.of();

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 3, 2);

            for (final Integer cell : dataOf(result)) {
                assertEquals(0, cell);
            }
        }

        /// Single object placement should land at index y*width + x.
        @Test
        @DisplayName("places a single GroundObject's gid at the y*width+x index")
        void singleGroundPlacement() {
            final int targetX = 2;
            final int targetY = 1;
            final int width = 4;
            final int height = 3;
            final int expectedIndex = targetY * width + targetX;
            final Map<Position, GroundObject> input = Map.of(
                new Position(targetX, targetY), new GroundObject(42)
            );

            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(input, width, height);

            assertEquals(42, dataOf(result).get(expectedIndex));
        }

        /// Cells outside the placed index should remain zero.
        @Test
        @DisplayName("leaves all other cells at zero when a single GroundObject is placed")
        void otherCellsRemainZero() {
            final int targetX = 2;
            final int targetY = 1;
            final int width = 4;
            final int height = 3;
            final int expectedIndex = targetY * width + targetX;
            final Map<Position, GroundObject> input = Map.of(
                new Position(targetX, targetY), new GroundObject(42)
            );

            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(input, width, height);

            final List<Integer> data = dataOf(result);
            for (int i = 0; i < data.size(); i++) {
                if (i == expectedIndex) {
                    continue;
                }
                assertEquals(0, data.get(i));
            }
        }

        /// Multiple grounds should land at their respective indices.
        @Test
        @DisplayName("places multiple GroundObjects at their respective y*width+x indices")
        void multipleGroundsPlaced() {
            final Map<Position, GroundObject> input = Map.of(
                new Position(0, 0), new GroundObject(1),
                new Position(1, 0), new GroundObject(2),
                new Position(0, 1), new GroundObject(3)
            );

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 2, 2);

            final List<Integer> data = dataOf(result);
            assertEquals(1, data.get(0));
            assertEquals(2, data.get(1));
            assertEquals(3, data.get(2));
            assertEquals(0, data.get(3));
        }

        /// A negative x must not appear in the data.
        @Test
        @DisplayName("ignores GroundObjects whose x is negative")
        void ignoresNegativeX() {
            final Map<Position, GroundObject> input = Map.of(
                new Position(-1, 0), new GroundObject(99)
            );

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 2, 2);

            for (final Integer cell : dataOf(result)) {
                assertEquals(0, cell);
            }
        }

        /// An x at or beyond the width bound must not appear in the data.
        @Test
        @DisplayName("ignores GroundObjects whose x is at or beyond the width")
        void ignoresXBeyondWidth() {
            final Map<Position, GroundObject> input = Map.of(
                new Position(2, 0), new GroundObject(99)
            );

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 2, 2);

            for (final Integer cell : dataOf(result)) {
                assertEquals(0, cell);
            }
        }

        /// A negative y must not appear in the data.
        @Test
        @DisplayName("ignores GroundObjects whose y is negative")
        void ignoresNegativeY() {
            final Map<Position, GroundObject> input = Map.of(
                new Position(0, -1), new GroundObject(99)
            );

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 2, 2);

            for (final Integer cell : dataOf(result)) {
                assertEquals(0, cell);
            }
        }

        /// A y at or beyond the height bound must not appear in the data.
        @Test
        @DisplayName("ignores GroundObjects whose y is at or beyond the height")
        void ignoresYBeyondHeight() {
            final Map<Position, GroundObject> input = Map.of(
                new Position(0, 2), new GroundObject(99)
            );

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 2, 2);

            for (final Integer cell : dataOf(result)) {
                assertEquals(0, cell);
            }
        }

        /// Layer id should always be 1 for the world layer.
        @Test
        @DisplayName("emits id=1 for the world layer")
        void worldLayerIdIsOne() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 5, 7);

            assertEquals(1, result.get(ID_KEY));
        }

        /// Layer name should always be "World".
        @Test
        @DisplayName("emits name='World' for the world layer")
        void worldLayerNameIsWorld() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 5, 7);

            assertEquals(WORLD_LAYER_NAME, result.get(NAME_KEY));
        }

        /// Layer type should always be "tilelayer".
        @Test
        @DisplayName("emits type='tilelayer' for the world layer")
        void worldLayerTypeIsTilelayer() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 5, 7);

            assertEquals(TILELAYER_TYPE, result.get(TYPE_KEY));
        }

        /// Width and height should be propagated from arguments.
        @Test
        @DisplayName("propagates width and height from the arguments")
        void worldLayerDimensions() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 5, 7);

            assertEquals(5, result.get(WIDTH_KEY));
            assertEquals(7, result.get(HEIGHT_KEY));
        }

        /// Visible should default to TRUE for the world layer.
        @Test
        @DisplayName("emits opacity=1 and visible=true for the world layer")
        void worldLayerOpacityAndVisible() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 5, 7);

            assertEquals(1, result.get(OPACITY_KEY));
            assertEquals(Boolean.TRUE, result.get(VISIBLE_KEY));
        }

        /// World layer x and y should be 0.
        @Test
        @DisplayName("emits x=0 and y=0 for the world layer")
        void worldLayerOriginIsZero() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 5, 7);

            assertEquals(0, result.get(X_KEY));
            assertEquals(0, result.get(Y_KEY));
        }

        /// The data entry should always be present.
        @Test
        @DisplayName("always emits a data entry")
        void worldLayerHasData() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 5, 7);

            assertNotNull(result.get(DATA_KEY));
        }

        /// Top-level key set should match the Tiled-expected names.
        @Test
        @DisplayName("emits exactly the Tiled-expected world layer keys")
        void emitsExpectedKeys() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 2, 2);

            assertEquals(
                Set.of(ID_KEY, NAME_KEY, TYPE_KEY, WIDTH_KEY, HEIGHT_KEY,
                       OPACITY_KEY, VISIBLE_KEY, X_KEY, Y_KEY, DATA_KEY),
                result.keySet()
            );
        }
    }

    // ====================================================================
    // buildObjectLayer (depends on TileSetService)
    // ====================================================================

    /// Tests for the buildObjectLayer entry point.
    @Nested
    @DisplayName("buildObjectLayer")
    @ExtendWith(MockitoExtension.class)
    class BuildObjectLayer {

        /// Mocked tileset service used to look up object-tile types.
        @Mock private TileSetService tileSetService;

        /// Empty input should yield an empty objects list.
        @Test
        @DisplayName("returns an empty objects list for an empty input layer")
        void emptyObjectLayer() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(List.of(), objectsOf(result));
        }

        /// Object layer id should always be 2.
        @Test
        @DisplayName("emits id=2 for the object layer")
        void objectLayerIdIsTwo() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(2, result.get(ID_KEY));
        }

        /// Object layer name should always be "QMLayer".
        @Test
        @DisplayName("emits name='QMLayer' for the object layer")
        void objectLayerName() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(OBJECT_LAYER_NAME, result.get(NAME_KEY));
        }

        /// Object layer type should always be "objectgroup".
        @Test
        @DisplayName("emits type='objectgroup' for the object layer")
        void objectLayerType() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(OBJECTGROUP_TYPE, result.get(TYPE_KEY));
        }

        /// draworder should always be "topdown".
        @Test
        @DisplayName("emits draworder='topdown' for the object layer")
        void objectLayerDrawOrder() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(DRAWORDER_TOPDOWN, result.get(DRAWORDER_KEY));
        }

        /// opacity and visible should default to 1 / TRUE.
        @Test
        @DisplayName("emits opacity=1 and visible=true for the object layer")
        void objectLayerOpacityAndVisible() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(1, result.get(OPACITY_KEY));
            assertEquals(Boolean.TRUE, result.get(VISIBLE_KEY));
        }

        /// Object layer origin x and y should be 0.
        @Test
        @DisplayName("emits x=0 and y=0 for the object layer")
        void objectLayerOriginIsZero() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(0, result.get(X_KEY));
            assertEquals(0, result.get(Y_KEY));
        }

        /// Top-level key set should match the Tiled-expected names.
        @Test
        @DisplayName("emits exactly the Tiled-expected object layer keys")
        void emitsExpectedKeys() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(
                Set.of(ID_KEY, NAME_KEY, TYPE_KEY, DRAWORDER_KEY, OPACITY_KEY,
                       VISIBLE_KEY, X_KEY, Y_KEY, OBJECTS_KEY),
                result.keySet()
            );
        }

        /// Three input objects should produce three output objects.
        @Test
        @DisplayName("emits one output object per input")
        void countsObjects() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Map<Position, GameObject> input = Map.of(
                new Position(0, 0), new StartFlag(1, new Position(0, 0)),
                new Position(1, 0), new StartFlag(1, new Position(1, 0)),
                new Position(2, 0), new StartFlag(1, new Position(2, 0))
            );

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertEquals(3, objectsOf(result).size());
        }

        /// The set of emitted ids should be {1, 2, 3} regardless of input order.
        @Test
        @DisplayName("assigns the contiguous id set {1..N} to emitted objects")
        void contiguousIdSet() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Map<Position, GameObject> input = Map.of(
                new Position(0, 0), new StartFlag(1, new Position(0, 0)),
                new Position(1, 0), new StartFlag(1, new Position(1, 0)),
                new Position(2, 0), new StartFlag(1, new Position(2, 0))
            );

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final Set<Object> ids = objectsOf(result).stream()
                .map(o -> o.get(ID_KEY))
                .collect(Collectors.toSet());
            assertEquals(Set.of(1, 2, 3), ids);
        }

        /// x coordinate should be pos.x() * TILE_SIZE.
        @Test
        @DisplayName("transforms position x to pos.x() * TILE_SIZE")
        void xPixelTransform() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(3, 4);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(7, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertEquals(3 * TILE_SIZE, objectsOf(result).get(0).get(X_KEY));
        }

        /// y coordinate should be (pos.y() + 1) * TILE_SIZE because Tiled anchors at the bottom-left.
        @Test
        @DisplayName("transforms position y to (pos.y() + 1) * TILE_SIZE")
        void yPixelTransform() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(3, 4);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(7, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertEquals((4 + 1) * TILE_SIZE, objectsOf(result).get(0).get(Y_KEY));
        }

        /// The gid should be copied from the source GameObject.
        @Test
        @DisplayName("copies the gid from the source GameObject")
        void copiesGid() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(42, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertEquals(42, objectsOf(result).get(0).get(GID_KEY));
        }

        /// Width and height should equal TILE_SIZE for every object.
        @Test
        @DisplayName("emits width and height equal to TILE_SIZE for every object")
        void emitsTileSizedDimensions() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(42, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final Map<String, Object> obj = objectsOf(result).get(0);
            assertEquals(TILE_SIZE, obj.get(WIDTH_KEY));
            assertEquals(TILE_SIZE, obj.get(HEIGHT_KEY));
        }

        /// Object type should be the value returned by the TileSetService for that gid.
        @Test
        @DisplayName("uses the type returned by TileSetService.getObjectTileType for each object")
        void typeFromService() {
            when(tileSetService.getObjectTileType(50)).thenReturn("door");
            when(tileSetService.getObjectTileType(60)).thenReturn("coin");
            final Map<Position, GameObject> input = Map.of(
                new Position(0, 0), new ExitDoor(50, new Position(0, 0)),
                new Position(1, 0), new Coin(60, new Position(1, 0), CoinType.GOLD_COIN)
            );

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final Set<Object> types = objectsOf(result).stream()
                .map(o -> o.get(TYPE_KEY))
                .collect(Collectors.toSet());
            assertEquals(Set.of("door", "coin"), types);
        }

        /// rotation should always be 0.
        @Test
        @DisplayName("emits rotation=0 for every object")
        void rotationIsZero() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(1, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertEquals(0, objectsOf(result).get(0).get(ROTATION_KEY));
        }

        /// name should always be empty string.
        @Test
        @DisplayName("emits name='' for every object")
        void nameIsEmpty() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(1, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertEquals("", objectsOf(result).get(0).get(NAME_KEY));
        }

        /// visible should always be TRUE.
        @Test
        @DisplayName("emits visible=true for every object")
        void visibleIsTrue() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(1, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertEquals(Boolean.TRUE, objectsOf(result).get(0).get(VISIBLE_KEY));
        }

        /// Non-Box GameObjects should not have a properties key.
        @Test
        @DisplayName("omits properties key for non-Box GameObjects")
        void nonBoxHasNoProperties() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(1, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertFalse(objectsOf(result).get(0).containsKey(PROPERTIES_KEY));
        }

        /// A Box with NoContent should not have a properties key.
        @Test
        @DisplayName("omits properties key for a Box with NoContent")
        void boxWithNoContentHasNoProperties() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(BOX_TYPE);
            final Position pos = new Position(0, 0);
            final Box box = new Box(99, pos, new Content.NoContent());
            final Map<Position, GameObject> input = Map.of(pos, box);

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertFalse(objectsOf(result).get(0).containsKey(PROPERTIES_KEY));
        }

        /// A Box with SomeContent should produce a properties list of size 1.
        @Test
        @DisplayName("emits a single property for a Box with SomeContent")
        @SuppressWarnings("unchecked")
        void boxWithSomeContentEmitsOneProperty() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(BOX_TYPE);
            final Position pos = new Position(0, 0);
            final Box box = new Box(99, pos, new Content.SomeContent(CoinType.GOLD_COIN));
            final Map<Position, GameObject> input = Map.of(pos, box);

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final List<Map<String, Object>> properties =
                (List<Map<String, Object>>) objectsOf(result).get(0).get(PROPERTIES_KEY);
            assertEquals(1, properties.size());
        }

        /// The emitted Content property should carry name, type, and the coin type value.
        @Test
        @DisplayName("the emitted Content property carries name='Content', type='string', and the coin value")
        @SuppressWarnings("unchecked")
        void boxContentPropertyFields() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(BOX_TYPE);
            final Position pos = new Position(0, 0);
            final Box box = new Box(99, pos, new Content.SomeContent(CoinType.GOLD_COIN));
            final Map<Position, GameObject> input = Map.of(pos, box);

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final Map<String, Object> property =
                ((List<Map<String, Object>>) objectsOf(result).get(0).get(PROPERTIES_KEY)).get(0);
            assertEquals("Content", property.get(NAME_KEY));
            assertEquals("string", property.get(TYPE_KEY));
            assertEquals(CoinType.GOLD_COIN.value(), property.get(VALUE_KEY));
        }

        /// A non-Box object should expose exactly the Tiled-expected key set.
        @Test
        @DisplayName("emits exactly the Tiled-expected keys on a non-Box object")
        void nonBoxObjectKeys() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn(FLAG_TYPE);
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(1, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertEquals(
                Set.of(ID_KEY, GID_KEY, X_KEY, Y_KEY, WIDTH_KEY, HEIGHT_KEY,
                       VISIBLE_KEY, ROTATION_KEY, NAME_KEY, TYPE_KEY),
                objectsOf(result).get(0).keySet()
            );
        }
    }
}
