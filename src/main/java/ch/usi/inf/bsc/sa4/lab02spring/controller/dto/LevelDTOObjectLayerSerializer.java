package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Map;

public class LevelDTOObjectLayerSerializer extends StdSerializer<Map<Position, GameObject>> {

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
