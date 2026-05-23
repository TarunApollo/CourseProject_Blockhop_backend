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

import ch.usi.inf.bsc.sa4.lab02spring.model.ClearCondition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileCatalogService;

/// Tests for [LayerToTiledMapConverter].
@SpringBootTest
@DisplayName("Layer to Tiled Map Converter")
@SuppressWarnings("PMD.TooManyStaticImports")
class LayerToTiledMapConverterTests {

    /// Mock tile catalog used by the converter.
    @MockitoBean
    private TileCatalogService tileCatalogService;

    /// Level used by the tests.
    private Level testLevel;

    @BeforeEach
    void setup() {
        final User testUser = new User("user-1", "Mario");
        this.testLevel = new Level("Test Level", "Test Description", testUser);
    }

    /// The exported map has the fields the frontend expects.
    @Test
    @DisplayName("exports basic map structure")
    void exportsBasicStructure() {
        Mockito.when(tileCatalogService.getTiles()).thenReturn(List.of());

        final Map<String, Object> result = LayerToTiledMapConverter.convertPipeline(testLevel, tileCatalogService);

        assertEquals("map", result.get("type"));
        assertEquals(testLevel.getWidth(), result.get("width"));
        assertEquals(testLevel.getHeight(), result.get("height"));
        assertNotNull(result.get("layers"));
        assertNotNull(result.get("tileCatalog"));
        assertTrue(result.containsKey("properties"));
    }

    /// Clear condition values are copied to map properties.
    @Test
    @DisplayName("maps clear condition metadata")
    @SuppressWarnings("unchecked")
    void mapsClearConditionProperties() {
        final ClearCondition clearCondition = new ClearCondition(new Condition.NoClearCondition(), 0);
        testLevel = new Level(new User("u1", "n1"), "Title", "Desc", false, clearCondition, Map.of(), Map.of());

        final Map<String, Object> result = LayerToTiledMapConverter.convertPipeline(testLevel, tileCatalogService);
        final List<Map<String, Object>> properties = (List<Map<String, Object>>) result.get("properties");

        assertNotNull(properties);
        final Map<String, Object> typeProp = properties.stream()
                .filter(p -> "ClearConditionType".equals(p.get("name")))
                .findFirst().orElseThrow();
        assertEquals("NONE", typeProp.get("value"));

        final Map<String, Object> amountProp = properties.stream()
                .filter(p -> "ClearConditionAmount".equals(p.get("name")))
                .findFirst().orElseThrow();
        assertEquals(0, amountProp.get("value"));
    }

    /// Clear condition values are copied to map properties with some condition.
    @Test
    @DisplayName("maps clear condition properties with some condition")
    @SuppressWarnings("unchecked")
    void mapsSomeClearConditionProperties() {
        final ClearCondition clearCondition = new ClearCondition(new Condition.SomeClearCondition(ch.usi.inf.bsc.sa4.lab02spring.model.ClearConditionType.SLIME), 5);
        testLevel = new Level(new User("u1", "n1"), "Title", "Desc", false, clearCondition, Map.of(), Map.of());

        final Map<String, Object> result = LayerToTiledMapConverter.convertPipeline(testLevel, tileCatalogService);
        final List<Map<String, Object>> properties = (List<Map<String, Object>>) result.get("properties");

        assertNotNull(properties);
        final Map<String, Object> typeProp = properties.stream()
                .filter(p -> "ClearConditionType".equals(p.get("name")))
                .findFirst().orElseThrow();
        assertEquals("SLIME", typeProp.get("value"));

        final Map<String, Object> amountProp = properties.stream()
                .filter(p -> "ClearConditionAmount".equals(p.get("name")))
                .findFirst().orElseThrow();
        assertEquals(5, amountProp.get("value"));
    }
}
