package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.CoinType;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Entry;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileCatalogService;

/// Tests for [TiledLayerMapper].
@SpringBootTest
@DisplayName("Tiled Layer Mapper")
@SuppressWarnings("PMD.TooManyStaticImports")
class TiledLayerMapperTests {

    /// Test tile id for boxes.
    private static final String BOX_TILE_ID = "box.id";

    /// Mock tile catalog used by the mapper.
    @MockitoBean
    private TileCatalogService tileCatalogService;

    /// Shared grid position for test objects.
    private static final Position POS = new Position(1, 2);

    /// Key used for the data array.
    private static final String DATA_KEY = "data";
    /// Key used for the objects array.
    private static final String OBJECTS_KEY = "objects";
    /// Key used for the properties list.
    private static final String PROPERTIES_KEY = "properties";

    /// Catalog entry returned by the mock service.
    private Entry mockEntry;

    @BeforeEach
    void setup() {
        this.mockEntry = new Entry(
                "test.id", "TestType", "category", "object");
    }

    /// World tiles are written into the right grid slot.
    @Test
    @DisplayName("transforms world layer into 1D array")
    @SuppressWarnings("unchecked")
    void buildsWorldLayer() {
        final Map<Position, GroundObject> worldLayer = Map.of(POS, new GroundObject("ground.tile"));
        final int width = 3;
        final int height = 3;

        final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(worldLayer, width, height,
                tileCatalogService);
        final List<String> data = (List<String>) result.get(DATA_KEY);

        assertNotNull(data);
        assertEquals(width * height, data.size());
        assertEquals("ground.tile", data.get(2 * width + 1));
    }

    /// World tiles outside the map boundaries are ignored.
    @Test
    @DisplayName("ignores world tiles outside bounds")
    @SuppressWarnings("unchecked")
    void ignoresOutOfBoundsTiles() {
        final Map<Position, GroundObject> worldLayer = Map.of(
            new Position(-1, 0), new GroundObject("out.left"),
            new Position(0, -1), new GroundObject("out.top"),
            new Position(3, 0), new GroundObject("out.right"),
            new Position(0, 3), new GroundObject("out.bottom")
        );
        final int width = 3;
        final int height = 3;

        final Map<String, Object> result = TiledLayerMapper.buildWorldLayer(worldLayer, width, height, tileCatalogService);
        final List<String> data = (List<String>) result.get(DATA_KEY);

        assertNotNull(data);
        assertEquals(width * height, data.size());
        for (final String tileId : data) {
            assertEquals("", tileId);
        }
    }

    /// Objects include their tile id and catalog type.
    @Test
    @DisplayName("maps objects with catalog data")
    @SuppressWarnings("unchecked")
    void buildsObjectLayer() {
        final GameObject box = new Box(BOX_TILE_ID, POS, new Content.NoContent());
        Mockito.when(tileCatalogService.requireTile(BOX_TILE_ID)).thenReturn(mockEntry);

        final Map<String, Object> result = TiledLayerMapper.buildObjectLayer(Map.of(POS, box), tileCatalogService);
        final List<Map<String, Object>> objects = (List<Map<String, Object>>) result.get(OBJECTS_KEY);

        assertNotNull(objects);
        assertEquals(1, objects.size());
        final Map<String, Object> tiledObj = objects.get(0);
        assertEquals(BOX_TILE_ID, tiledObj.get("tileId"));
        assertEquals(128, tiledObj.get("x")); // 1 tile from the left
        assertEquals(384, tiledObj.get("y")); // 3 tiles from the top
        assertEquals("TestType", tiledObj.get("type"));
    }

    /// Boxes keep their content property.
    @Test
    @DisplayName("includes properties for boxes with content")
    @SuppressWarnings("unchecked")
    void exportsBoxProperties() {
        final Content content = new Content.SomeContent(CoinType.GOLD_COIN);
        final GameObject box = new Box(BOX_TILE_ID, POS, content);
        Mockito.when(tileCatalogService.requireTile(BOX_TILE_ID)).thenReturn(mockEntry);

        final Map<String, Object> result = TiledLayerMapper.buildObjectLayer(Map.of(POS, box), tileCatalogService);
        final List<Map<String, Object>> objects = (List<Map<String, Object>>) result.get(OBJECTS_KEY);

        assertNotNull(objects);
        final List<Map<String, Object>> properties = (List<Map<String, Object>>) objects.get(0).get(PROPERTIES_KEY);

        assertNotNull(properties);
        assertTrue(properties.stream().anyMatch(p -> "Content".equals(p.get("name"))));
    }

    /// Objects without SomeContent do not have a properties field.
    @Test
    @DisplayName("does not include properties for boxes without content")
    @SuppressWarnings("unchecked")
    void excludesBoxPropertiesWithoutContent() {
        final GameObject box = new Box(BOX_TILE_ID, POS, new Content.NoContent());
        Mockito.when(tileCatalogService.requireTile(BOX_TILE_ID)).thenReturn(mockEntry);

        final Map<String, Object> result = TiledLayerMapper.buildObjectLayer(Map.of(POS, box), tileCatalogService);
        final List<Map<String, Object>> objects = (List<Map<String, Object>>) result.get(OBJECTS_KEY);

        assertNotNull(objects);
        final Map<String, Object> tiledObj = objects.get(0);
        assertTrue(!tiledObj.containsKey(PROPERTIES_KEY));
    }
}
