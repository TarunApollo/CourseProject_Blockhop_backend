package ch.usi.inf.bsc.sa4.lab02spring.utils;

import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.HashMap;
import java.util.Map;

/** Custom Jackson serializers and deserializers for Level DTO map fields. */
@SuppressWarnings("PMD:UseConcurrentHashMap")
public class FieldSerializer {

    /**
     * Serializes the object layer map ({@link Position} → {@link GameObject}) to
     * JSON.
     */
    public static class LevelDTOObjectLayerSerializer extends StdSerializer<Map<Position, GameObject>> {

        /** Creates a new LevelDTOObjectLayerSerializer. */
        public LevelDTOObjectLayerSerializer() {
            this(Map.class);
        }

        /** Creates a new LevelDTOObjectLayerSerializer for the given raw type. */
        protected LevelDTOObjectLayerSerializer(final Class<?> t) {
            super(t);
        }

        @Override
        public void serialize(final Map<Position, GameObject> value, final JsonGenerator jgen,
                final SerializationContext provider)
                throws JacksonException {
            jgen.writeStartObject();
            for (final Map.Entry<Position, GameObject> element : value.entrySet()) {
                final String key = element.getKey().compactString();
                jgen.writeName(key);
                provider.writeValue(jgen, element.getValue());
            }
            jgen.writeEndObject();
        }
    }

    /**
     * Serializes the world layer map ({@link Position} → {@link GroundObject}) to
     * JSON.
     */
    public static class LevelDTOWorldLayerSerializer extends StdSerializer<Map<Position, GroundObject>> {

        /** Creates a new LevelDTOWorldLayerSerializer. */
        public LevelDTOWorldLayerSerializer() {
            this(Map.class);
        }

        /** Creates a new LevelDTOWorldLayerSerializer for the given raw type. */
        protected LevelDTOWorldLayerSerializer(final Class<?> t) {
            super(t);
        }

        @Override
        public void serialize(final Map<Position, GroundObject> value, final JsonGenerator jgen,
                final SerializationContext provider)
                throws JacksonException {
            jgen.writeStartObject();
            for (final Map.Entry<Position, GroundObject> element : value.entrySet()) {
                final String key = element.getKey().compactString();
                jgen.writeName(key);
                provider.writeValue(jgen, element.getValue());
            }
            jgen.writeEndObject();
        }
    }

    /**
     * Deserializes the world layer map ({@link Position} → {@link GroundObject})
     * from JSON.
     */
    public static class WorldLayerDeserializer extends StdDeserializer<Map<Position, GroundObject>> {

        /** Creates a new WorldLayerDeserializer. */
        public WorldLayerDeserializer() {
            this(Map.class);
        }

        /** Creates a new WorldLayerDeserializer for the given raw type. */
        protected WorldLayerDeserializer(final Class<?> t) {
            super(t);
        }

        /** Parses a compact {@code "x,y"} key string into a {@link Position}. */
        private static Position parsePosition(final String compactKey) {
            final String[] coords = compactKey.split(",");
            return new Position(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
        }

        @Override
        public Map<Position, GroundObject> deserialize(final JsonParser p, final DeserializationContext context) {
            final Map<Position, GroundObject> map = new HashMap<>();

            while (p.nextToken() != JsonToken.END_OBJECT) {
                final String name = p.currentName();
                p.nextToken();

                final Position pos = parsePosition(name);

                // This assumes GameObject has @JsonTypeInfo or similar setup for polymorphism.
                // If not, you might need to read it as a JsonNode and use your factory.
                final GroundObject obj = p.readValueAs(GroundObject.class);
                map.put(pos, obj);
            }
            return Map.copyOf(map);
        }
    }

    /**
     * Deserializes a compact position string (e.g. {@code "x,y"}) into a
     * {@link Position} map key.
     */
    public static class PositionKeyDeserializer extends tools.jackson.databind.KeyDeserializer {
        @Override
        public Object deserializeKey(final String key, final DeserializationContext ctxt) {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("key can't be null or empty");
            }
            final String[] coords = key.split(",");
            return new Position(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
        }
    }
}
