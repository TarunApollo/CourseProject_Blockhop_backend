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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@DisplayName("TiledLayerMapper")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class TiledLayerMapperTest {

    private static final int TILE_SIZE = 128;
    private static final String NAME_KEY = "name";
    private static final String TYPE_KEY = "type";
    private static final String VISIBLE_KEY = "visible";
    private static final String OBJECTS_KEY = "objects";
    private static final String PROPERTIES_KEY = "properties";
    private static final String DATA_KEY = "data";
    private static final String ID_KEY = "id";

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> objectsOf(final Map<String, Object> layer) {
        return (List<Map<String, Object>>) layer.get(OBJECTS_KEY);
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> dataOf(final Map<String, Object> layer) {
        return (List<Integer>) layer.get(DATA_KEY);
    }

    // ====================================================================
    // buildWorldLayer (no service dependency)
    // ====================================================================

    @Nested
    @DisplayName("buildWorldLayer")
    class BuildWorldLayer {

        @Test
        @DisplayName("returns a data list of width*height zeros for an empty world layer")
        void emptyWorldLayer() {
            final Map<Position, GroundObject> input = Map.of();

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 3, 2);

            final List<Integer> data = dataOf(result);
            assertEquals(6, data.size());
            for (final Integer cell : data) {
                assertEquals(0, cell);
            }
        }

        @Test
        @DisplayName("places a single GroundObject's gid at the y*width+x index")
        void singleGroundPlacement() {
            final Map<Position, GroundObject> input = Map.of(
                new Position(2, 1), new GroundObject(42)
            );

            final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(input, 4, 3);

            final List<Integer> data = dataOf(result);
            assertEquals(12, data.size());
            // index = y*width + x = 1*4 + 2 = 6
            assertEquals(42, data.get(6));
            // all other cells stay 0
            for (int i = 0; i < data.size(); i++) {
                if (i != 6) {
                    assertEquals(0, data.get(i));
                }
            }
        }

        @Test
        @DisplayName("places multiple GroundObjects at their respective indices")
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

        @Test
        @DisplayName("emits all required Tiled top-level layer fields with expected values")
        void topLevelFields() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 5, 7);

            assertEquals(1, result.get(ID_KEY));
            assertEquals("World", result.get(NAME_KEY));
            assertEquals("tilelayer", result.get(TYPE_KEY));
            assertEquals(5, result.get("width"));
            assertEquals(7, result.get("height"));
            assertEquals(1, result.get("opacity"));
            assertEquals(Boolean.TRUE, result.get(VISIBLE_KEY));
            assertEquals(0, result.get("x"));
            assertEquals(0, result.get("y"));
            assertNotNull(result.get(DATA_KEY));
        }

        @Test
        @DisplayName("emits exactly the Tiled-expected world layer keys")
        void emitsExpectedKeys() {
            final Map<String, Object> result =
                TiledLayerMapper.buildWorldLayer(Map.of(), 2, 2);

            assertEquals(
                Set.of(ID_KEY, NAME_KEY, TYPE_KEY, "width", "height",
                       "opacity", VISIBLE_KEY, "x", "y", DATA_KEY),
                result.keySet()
            );
        }
    }

    // ====================================================================
    // buildObjectLayer (depends on TileSetService)
    // ====================================================================

    @Nested
    @DisplayName("buildObjectLayer")
    @ExtendWith(MockitoExtension.class)
    class BuildObjectLayer {

        @Mock private TileSetService tileSetService;

        @Test
        @DisplayName("returns an empty objects list for an empty input layer")
        void emptyObjectLayer() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(List.of(), objectsOf(result));
        }

        @Test
        @DisplayName("emits all required Tiled top-level fields")
        void topLevelFields() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(2, result.get(ID_KEY));
            assertEquals("QMLayer", result.get(NAME_KEY));
            assertEquals("objectgroup", result.get(TYPE_KEY));
            assertEquals("topdown", result.get("draworder"));
            assertEquals(1, result.get("opacity"));
            assertEquals(Boolean.TRUE, result.get(VISIBLE_KEY));
            assertEquals(0, result.get("x"));
            assertEquals(0, result.get("y"));
        }

        @Test
        @DisplayName("emits exactly the Tiled-expected object layer keys")
        void emitsExpectedKeys() {
            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(Map.of(), tileSetService);

            assertEquals(
                Set.of(ID_KEY, NAME_KEY, TYPE_KEY, "draworder", "opacity",
                       VISIBLE_KEY, "x", "y", OBJECTS_KEY),
                result.keySet()
            );
        }

        @Test
        @DisplayName("assigns sequential ids starting from 1 to emitted objects")
        void sequentialIds() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");
            // LinkedHashMap to keep deterministic iteration order
            final Map<Position, GameObject> input = new LinkedHashMap<>();
            input.put(new Position(0, 0), new StartFlag(1, new Position(0, 0)));
            input.put(new Position(1, 0), new StartFlag(1, new Position(1, 0)));
            input.put(new Position(2, 0), new StartFlag(1, new Position(2, 0)));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final List<Map<String, Object>> objects = objectsOf(result);
            assertEquals(3, objects.size());
            assertEquals(1, objects.get(0).get(ID_KEY));
            assertEquals(2, objects.get(1).get(ID_KEY));
            assertEquals(3, objects.get(2).get(ID_KEY));
        }

        @Test
        @DisplayName("transforms position into pixel coordinates with the y offset by one tile")
        void pixelCoordinateTransform() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");
            final Position pos = new Position(3, 4);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(7, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final Map<String, Object> obj = objectsOf(result).get(0);
            assertEquals(3 * TILE_SIZE, obj.get("x"));
            assertEquals((4 + 1) * TILE_SIZE, obj.get("y"));
        }

        @Test
        @DisplayName("copies the gid and emits TILE_SIZE-sized width and height")
        void gidAndSize() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(42, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final Map<String, Object> obj = objectsOf(result).get(0);
            assertEquals(42, obj.get("gid"));
            assertEquals(TILE_SIZE, obj.get("width"));
            assertEquals(TILE_SIZE, obj.get("height"));
        }

        @Test
        @DisplayName("uses the type returned by TileSetService.getObjectTileType for each object")
        void typeFromService() {
            when(tileSetService.getObjectTileType(50)).thenReturn("door");
            when(tileSetService.getObjectTileType(60)).thenReturn("coin");
            final Map<Position, GameObject> input = new LinkedHashMap<>();
            input.put(new Position(0, 0), new ExitDoor(50, new Position(0, 0)));
            input.put(new Position(1, 0), new Coin(60, new Position(1, 0), CoinType.GOLD_COIN));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final List<Map<String, Object>> objects = objectsOf(result);
            assertEquals("door", objects.get(0).get(TYPE_KEY));
            assertEquals("coin", objects.get(1).get(TYPE_KEY));
        }

        @Test
        @DisplayName("emits common static fields rotation=0 and name='' on every object")
        void staticFields() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(1, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final Map<String, Object> obj = objectsOf(result).get(0);
            assertEquals(0, obj.get("rotation"));
            assertEquals("", obj.get(NAME_KEY));
            assertEquals(Boolean.TRUE, obj.get(VISIBLE_KEY));
        }

        @Test
        @DisplayName("omits properties key for non-Box GameObjects")
        void nonBoxHasNoProperties() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(1, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertFalse(objectsOf(result).get(0).containsKey(PROPERTIES_KEY));
        }

        @Test
        @DisplayName("omits properties key for a Box with NoContent")
        void boxWithNoContentHasNoProperties() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("box");
            final Position pos = new Position(0, 0);
            final Box box = new Box(99, pos, new Content.NoContent());
            final Map<Position, GameObject> input = Map.of(pos, box);

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            assertFalse(objectsOf(result).get(0).containsKey(PROPERTIES_KEY));
        }

        @Test
        @DisplayName("emits a single Content property when Box has SomeContent")
        @SuppressWarnings("unchecked")
        void boxWithSomeContentEmitsProperty() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("box");
            final Position pos = new Position(0, 0);
            final Box box = new Box(99, pos, new Content.SomeContent(CoinType.GOLD_COIN));
            final Map<Position, GameObject> input = Map.of(pos, box);

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final List<Map<String, Object>> properties =
                (List<Map<String, Object>>) objectsOf(result).get(0).get(PROPERTIES_KEY);
            assertEquals(1, properties.size());
            assertEquals("Content", properties.get(0).get(NAME_KEY));
            assertEquals("string", properties.get(0).get(TYPE_KEY));
            assertEquals(CoinType.GOLD_COIN.value(), properties.get(0).get("value"));
        }

        @Test
        @DisplayName("emits exactly the Tiled-expected keys on a non-Box object")
        void nonBoxObjectKeys() {
            when(tileSetService.getObjectTileType(anyInt())).thenReturn("flag");
            final Position pos = new Position(0, 0);
            final Map<Position, GameObject> input = Map.of(pos, new StartFlag(1, pos));

            final Map<String, Object> result =
                TiledLayerMapper.buildObjectLayer(input, tileSetService);

            final Map<String, Object> obj = objectsOf(result).get(0);
            assertEquals(
                Set.of(ID_KEY, "gid", "x", "y", "width", "height",
                       VISIBLE_KEY, "rotation", NAME_KEY, TYPE_KEY),
                obj.keySet()
            );
        }
    }
}
