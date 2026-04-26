package ch.usi.inf.bsc.sa4.lab02spring.utils;

import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.Map;

/** Black-box tests for FieldSerializer inner classes. */
@SuppressWarnings({ "NullAway" })
/* package */ final class FieldSerializerTests {

    /** Position used across tests. */
    private static final Position TEST_POS = new Position(3, 7);

    /** Compact string representation of TEST_POS. */
    private static final String COMPACT_KEY = "3,7";

    /** Ground object used across tests. */
    private static final GroundObject TEST_GROUND = new GroundObject(42);

    /** Game object (StartFlag) used across tests. */
    private static final StartFlag TEST_OBJECT = new StartFlag(68, TEST_POS);

    /** JSON input for world layer deserialization tests. */
    private static final String WORLD_JSON = "{\"3,7\":{\"gid\":42}}";

    /** ObjectMapper configured with the object layer serializer. */
    private static ObjectMapper objectLayerMapper;

    /** ObjectMapper configured with the world layer serializer. */
    private static ObjectMapper worldLayerMapper;

    /** ObjectMapper configured with the world layer deserializer. */
    private static ObjectMapper worldLayerDeserMapper;

    /** PositionKeyDeserializer instance for direct testing. */
    private static FieldSerializer.PositionKeyDeserializer keyDeserializer;

    /** Prevents instantiation. */
    private FieldSerializerTests() {
    }

    /** Configures ObjectMapper instances with custom serializers. */
    @BeforeAll
    static void setupMappers() {
        final SimpleModule objectSerModule = new SimpleModule();
        objectSerModule.addSerializer(
                new FieldSerializer.LevelDTOObjectLayerSerializer());
        objectLayerMapper = JsonMapper.builder()
                .addModule(objectSerModule).build();

        final SimpleModule worldSerModule = new SimpleModule();
        worldSerModule.addSerializer(
                new FieldSerializer.LevelDTOWorldLayerSerializer());
        worldLayerMapper = JsonMapper.builder()
                .addModule(worldSerModule).build();

        final SimpleModule worldDeserModule = new SimpleModule();
        worldDeserModule.addDeserializer(Map.class,
                new FieldSerializer.WorldLayerDeserializer());
        worldLayerDeserMapper = JsonMapper.builder()
                .addModule(worldDeserModule).build();

        keyDeserializer = new FieldSerializer.PositionKeyDeserializer();
    }

    /** Tests for LevelDTOObjectLayerSerializer. */
    @Nested
    @DisplayName("LevelDTOObjectLayerSerializer")
    /* default */ class ObjectLayerSerializerTests {

        @Test
        @DisplayName("should serialize a non-empty map containing the compact position key")
        void serializeNonEmptyMap() throws Exception {
            final Map<Position, GameObject> map = Map.of(TEST_POS, TEST_OBJECT);
            final String json = objectLayerMapper.writeValueAsString(map);
            Assertions.assertTrue(json.contains(COMPACT_KEY));
        }
    }

    /** Tests for LevelDTOWorldLayerSerializer. */
    @Nested
    @DisplayName("LevelDTOWorldLayerSerializer")
    /* default */ class WorldLayerSerializerTests {

        @Test
        @DisplayName("should serialize a non-empty map containing the compact position key")
        void serializeNonEmptyMap() throws Exception {
            final Map<Position, GroundObject> map = Map.of(TEST_POS, TEST_GROUND);
            final String json = worldLayerMapper.writeValueAsString(map);
            Assertions.assertTrue(json.contains(COMPACT_KEY));
        }
    }

    /** Tests for WorldLayerDeserializer. */
    @Nested
    @DisplayName("WorldLayerDeserializer")
    /* default */ class WorldLayerDeserializerTests {

        @Test
        @DisplayName("should deserialize a JSON object with one entry into a map of size 1")
        void deserializeNonEmptyObject() throws Exception {
            final Map<?, ?> result = worldLayerDeserMapper.readValue(WORLD_JSON, Map.class);
            Assertions.assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should produce a map containing the deserialized position as key")
        void deserializeCorrectPosition() throws Exception {
            final Map<?, ?> result = worldLayerDeserMapper.readValue(WORLD_JSON, Map.class);
            Assertions.assertTrue(result.containsKey(TEST_POS));
        }

        @Test
        @DisplayName("should produce a map containing the correct ground object value")
        void deserializeCorrectValue() throws Exception {
            final Map<?, ?> result = worldLayerDeserMapper.readValue(WORLD_JSON, Map.class);
            Assertions.assertEquals(TEST_GROUND, result.get(TEST_POS));
        }
    }

    /** Tests for PositionKeyDeserializer. */
    @Nested
    @DisplayName("PositionKeyDeserializer")
    /* default */ class PositionKeyDeserializerTests {

        @Test
        @DisplayName("should parse a valid compact key into the correct position")
        void validKey() {
            final Position result = (Position) keyDeserializer.deserializeKey(COMPACT_KEY, null);
            Assertions.assertEquals(TEST_POS, result);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when the key is null")
        void nullKey() {
            final Executable call = () -> keyDeserializer.deserializeKey(null, null);
            Assertions.assertThrows(IllegalArgumentException.class, call);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when the key is empty")
        void emptyKey() {
            final Executable call = () -> keyDeserializer.deserializeKey("", null);
            Assertions.assertThrows(IllegalArgumentException.class, call);
        }
    }
}
