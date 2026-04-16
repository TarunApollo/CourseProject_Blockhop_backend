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

public class FieldSerializer {

    static public class LevelDTOObjectLayerSerializer extends StdSerializer<Map<Position, GameObject>> {

        public LevelDTOObjectLayerSerializer() {
            this(Map.class);
        }

        protected LevelDTOObjectLayerSerializer(Class<?> t) {
            super(t);
        }

        @Override
        public void serialize(Map<Position, GameObject> value, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
            jgen.writeStartObject();
            for (Map.Entry<Position, GameObject> element: value.entrySet()) {
                var key = element.getKey().compactString();
                jgen.writeName(key);
                provider.writeValue(jgen, element.getValue());
            }
            jgen.writeEndObject();
        }
    }

    static public class LevelDTOWorldLayerSerializer extends StdSerializer<Map<Position, GroundObject>> {

        public LevelDTOWorldLayerSerializer() {
            this(Map.class);
        }

        protected LevelDTOWorldLayerSerializer(Class<?> t) {
            super(t);
        }

        @Override
        public void serialize(Map<Position, GroundObject> value, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
            jgen.writeStartObject();
            for (Map.Entry<Position, GroundObject> element: value.entrySet()) {
                var key = element.getKey().compactString();
                jgen.writeName(key);
                provider.writeValue(jgen, element.getValue());
            }
            jgen.writeEndObject();
        }
    }

    static public class WorldLayerDeserializer extends StdDeserializer<Map<Position, GroundObject>> {

        public WorldLayerDeserializer() {
            this(Map.class);
        }

        protected WorldLayerDeserializer(Class<?> t) {
            super(t);
        }

        @Override
        public Map<Position, GroundObject> deserialize(JsonParser p, DeserializationContext context) {
            Map<Position, GroundObject> map = new HashMap<>();

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String name = p.currentName();
                p.nextToken();

                String[] coords = name.split(",");
                Position pos = new Position(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));

                // This assumes GameObject has @JsonTypeInfo or similar setup for polymorphism.
                // If not, you might need to read it as a JsonNode and use your factory.
                GroundObject obj = p.readValueAs(GroundObject.class);
                map.put(pos, obj);
            }
            return map;
        }
    }
}
