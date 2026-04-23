package ch.usi.inf.bsc.sa4.lab02spring.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/// Exports a level and tileset as a Tiled-compatible map payload.
/// Delegates layer and tileset conversion to dedicated helpers.
@Component
public class LayerToTiledMapConverter {
    /// Converts world and object layers into Tiled structures.
    private final TiledLayerMapper tiledLayerMapper;

    /// Creates a map converter with its layer conversion helper.
    ///
    /// @param tiledLayerMapper converts level layers into Tiled structures
    public LayerToTiledMapConverter(final TiledLayerMapper tiledLayerMapper) {
        this.tiledLayerMapper = tiledLayerMapper;
    }

    private static final Map<String, Object> MAP_METADATA = Map.of(
        "type", "map",
        "orientation", "orthogonal",
        "renderorder", "right-down",
        "tilewidth", 128,
        "tileheight", 128,
        "version", "1.10",
        "tiledversion", "1.10.1",
        "compressionlevel", -1
);

    /// Converts a level and tileset into the map payload expected by the
    /// frontend.
    ///
    /// @param level the level to export
    /// @param tileSet the tileset metadata referenced by the level
    /// @return a Tiled-compatible map payload
    public Map<String, Object> convertPipeline(
            final Level level,
            final TileSet tileSet) {
        final Map<String, Object> worldLayer =
                this.tiledLayerMapper.buildWorldLayer(level.getWorldLayer(), level.getWidth(), level.getHeight());
        final Map<String, Object> objectLayer =
                this.tiledLayerMapper.buildObjectLayer(level.getObjectLayer());
        final Map<String, Object> tilesetMap = TiledTilesetMapper.buildTileset(tileSet);
        final Map<String, Object> res = new LinkedHashMap<>();
        res.putAll(buildMapMetadata(level));
        res.put("layers", List.of(worldLayer, objectLayer));
        res.put("tilesets", List.of(tilesetMap));
        return res;
    }

    private static Map<String, Object> buildMapMetadata(final Level level) {
        final Map<String, Object> metadata = new LinkedHashMap<>(MAP_METADATA);
        metadata.put("width", level.getWidth());
        metadata.put("height", level.getHeight());
        metadata.put("nextlayerid", 3);
        metadata.put("nextobjectid", level.getObjectLayer().size() + 1);
        metadata.put("infinite", Boolean.FALSE);
        metadata.put("doorOpen", level.getClearCondition().condition() instanceof Condition.NoClearCondition);
        metadata.put("properties", buildMapProperties(level));
        return metadata;
    }

    private static List<Map<String, Object>> buildMapProperties(final Level level) {
        final List<Map<String, Object>> properties = new ArrayList<>();

        final Map<String, Object> clearConditionType = new LinkedHashMap<>();
        clearConditionType.put("name", "ClearConditionType");
        clearConditionType.put("type", "string");

        String conditionType = "NONE";
        if (level.getClearCondition().condition() instanceof Condition.SomeClearCondition some) {
            conditionType = some.target().name();
        }
        clearConditionType.put("value", conditionType);
        properties.add(clearConditionType);

        final Map<String, Object> clearConditionAmount = new LinkedHashMap<>();
        clearConditionAmount.put("name", "ClearConditionAmount");
        clearConditionAmount.put("type", "int");
        clearConditionAmount.put("value", level.getClearCondition().targetAmount());
        properties.add(clearConditionAmount);

        return properties;
    }
}
