package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// Exports a level and tileset as a Tiled-compatible map payload.
/// Delegates layer and tileset conversion to dedicated helpers.
public final class LayerToTiledMapConverter {
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

    private LayerToTiledMapConverter() {
    }

    /// Converts a level and tileset into the map payload expected by the
    /// frontend.
    ///
    /// @param level the level to export
    /// @param tileSet the tileset metadata referenced by the level
    /// @param tileSetService resolves tile types referenced by object-layer entries
    /// @return a Tiled-compatible map payload
    public static Map<String, Object> convertPipeline(
            final Level level,
            final TileSet tileSet,
            final TileSetService tileSetService) {
        final Map<String, Object> worldLayer =
                TiledLayerMapper.buildWorldLayer(level.getWorldLayer(), level.getWidth(), level.getHeight());
        final Map<String, Object> objectLayer =
                TiledLayerMapper.buildObjectLayer(level.getObjectLayer(), tileSetService);
        final Map<String, Object> tilesetMap = TiledTilesetMapper.buildTileset(tileSet);
        return Stream.concat(
            buildMapMetadata(level).entrySet().stream(),
            Stream.<Map.Entry<String, Object>>of(
                Map.entry("layers", List.of(worldLayer, objectLayer)),
                Map.entry("tilesets", List.of(tilesetMap))
            )
        ).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, Object> buildMapMetadata(final Level level) {
        return Stream.concat(
            MAP_METADATA.entrySet().stream(),
            Stream.<Map.Entry<String, Object>>of(
                Map.entry("width", level.getWidth()),
                Map.entry("height", level.getHeight()),
                Map.entry("nextlayerid", 3),
                Map.entry("nextobjectid", level.getObjectLayer().size() + 1),
                Map.entry("infinite", Boolean.FALSE),
                Map.entry("doorOpen", level.getClearCondition().condition() instanceof Condition.NoClearCondition),
                Map.entry("properties", buildMapProperties(level))
            )
        ).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static List<Map<String, Object>> buildMapProperties(final Level level) {
        final String conditionType =
            level.getClearCondition().condition() instanceof Condition.SomeClearCondition some
                ? some.target().name()
                : "NONE";
        return List.of(
            Map.of(
                "name", "ClearConditionType",
                "type", "string",
                "value", conditionType
            ),
            Map.of(
                "name", "ClearConditionAmount",
                "type", "int",
                "value", level.getClearCondition().targetAmount()
            )
        );
    }
}
