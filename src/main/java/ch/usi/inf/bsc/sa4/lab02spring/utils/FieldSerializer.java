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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

///
/// Jackson serializers and deserializers for map-based level fields.
///
public class FieldSerializer {

    ///
    /// Serializes the object layer of a level DTO as a JSON object keyed by position.
    ///
    static public class LevelDTOObjectLayerSerializer extends StdSerializer<Map<Position, GameObject>> {

        ///
        /// Creates a serializer for object-layer maps.
        ///
        public LevelDTOObjectLayerSerializer() {
            this(Map.class);
        }

        ///
        /// Creates a serializer for object-layer maps.
        /// @param t the handled type
        ///
        protected LevelDTOObjectLayerSerializer(final Class<?> t) {
            super(t);
        }

        @Override
        public void serialize(final Map<Position, GameObject> value, final JsonGenerator jgen, final SerializationContext provider) throws JacksonException {
            jgen.writeStartObject();
            for (final Map.Entry<Position, GameObject> element: value.entrySet()) {
                final var key = element.getKey().compactString();
                jgen.writeName(key);
                provider.writeValue(jgen, element.getValue());
            }
            jgen.writeEndObject();
        }
    }

    ///
    /// Serializes the world layer of a level DTO as a JSON object keyed by position.
    ///
    static public class LevelDTOWorldLayerSerializer extends StdSerializer<Map<Position, GroundObject>> {

        ///
        /// Creates a serializer for world-layer maps.
        ///
        public LevelDTOWorldLayerSerializer() {
            this(Map.class);
        }

        ///
        /// Creates a serializer for world-layer maps.
        /// @param t the handled type
        ///
        protected LevelDTOWorldLayerSerializer(final Class<?> t) {
            super(t);
        }

        @Override
        public void serialize(final Map<Position, GroundObject> value, final JsonGenerator jgen, final SerializationContext provider) throws JacksonException {
            jgen.writeStartObject();
            for (final Map.Entry<Position, GroundObject> element: value.entrySet()) {
                final var key = element.getKey().compactString();
                jgen.writeName(key);
                provider.writeValue(jgen, element.getValue());
            }
            jgen.writeEndObject();
        }
    }

    ///
    /// Deserializes the world layer of a level DTO.
    ///
    static public class WorldLayerDeserializer extends StdDeserializer<Map<Position, GroundObject>> {

        ///
        /// Creates a deserializer for world-layer maps.
        ///
        public WorldLayerDeserializer() {
            this(Map.class);
        }

        ///
        /// Creates a deserializer for world-layer maps.
        /// @param t the handled type
        ///
        protected WorldLayerDeserializer(final Class<?> t) {
            super(t);
        }

        @Override
        public Map<Position, GroundObject> deserialize(final JsonParser p, DeserializationContext context) {
            final Map<Position, GroundObject> map = new HashMap<>();

            while (p.nextToken() != JsonToken.END_OBJECT) {
                final String name = p.currentName();
                p.nextToken();

                final String[] coords = name.split(",");
                final Position pos = new Position(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));

                // This assumes GameObject has @JsonTypeInfo or similar setup for polymorphism.
                // If not, you might need to read it as a JsonNode and use your factory.
                final GroundObject obj = p.readValueAs(GroundObject.class);
                map.put(pos, obj);
            }
            return map;
        }
    }

    ///
    /// Deserializes a "x,y" JSON map key into a Position.
    ///
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
