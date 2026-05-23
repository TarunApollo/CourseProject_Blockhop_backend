package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.KEY_NAME;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.KEY_TYPE;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.KEY_VALUE;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.TYPE_INT;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.TYPE_STRING;

import ch.usi.inf.bsc.sa4.lab02spring.model.Condition;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileCatalogService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// Exports a level as map data for the frontend.
@SuppressWarnings({ "PMD.TooManyStaticImports" , "PMD.AtLeastOneConstructor"})
public final class LayerToTiledMapConverter {
    /// Shared boxed false value for metadata entries.
    private static final Boolean MAP_FLAG_FALSE = Boolean.FALSE;

    /// Static map fields shared by every exported level.
    private static final List<Map.Entry<String, Object>> MAP_METADATA = List.of(
            Map.entry(KEY_TYPE, "map"),
            Map.entry("orientation", "orthogonal"),
            Map.entry("renderorder", "right-down"),
            Map.entry("tilewidth", 128),
            Map.entry("tileheight", 128),
            Map.entry("version", "1.10"),
            Map.entry("tiledversion", "1.10.1"),
            Map.entry("compressionlevel", -1));

    /// Converts a level into the map payload expected by the frontend.
    ///
    /// @spec.requires level and tileCatalogService are not null.
    /// @spec.modifies nothing.
    /// @spec.effects builds the map payload sent to the frontend.
    /// @param level              the level to convert
    /// @param tileCatalogService resolves tile ids used by the level
    /// @return map data for the frontend
    public static Map<String, Object> convertPipeline(
            final Level level,
            final TileCatalogService tileCatalogService) {
        final Map<String, Object> worldLayer = TiledLayerMapper.buildWorldLayer(level.getWorldLayer(), level.getWidth(),
                level.getHeight());
        final Map<String, Object> objectLayer = TiledLayerMapper.buildObjectLayer(level.getObjectLayer(),
                tileCatalogService);
        return Stream.concat(
                buildMapMetadata(level).entrySet().stream(),
                Stream.<Map.Entry<String, Object>>of(
                        Map.entry("layers", List.of(worldLayer, objectLayer)),
                        Map.entry("tileCatalog", tileCatalogService.getTiles())))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /// Builds metadata copied to the map root.
    ///
    /// @spec.requires level is not null.
    /// @spec.modifies nothing.
    /// @spec.effects builds root map fields from the level.
    /// @param level the level to read
    /// @return root map fields
    private static Map<String, Object> buildMapMetadata(final Level level) {
        return Stream.concat(
                MAP_METADATA.stream(),
                Stream.<Map.Entry<String, Object>>of(
                        Map.entry("width", level.getWidth()),
                        Map.entry("height", level.getHeight()),
                        Map.entry("nextlayerid", 3),
                        Map.entry("nextobjectid", level.getObjectLayer().size() + 1),
                        Map.entry("infinite", MAP_FLAG_FALSE),
                        Map.entry("doorOpen",
                                level.getClearCondition().condition() instanceof Condition.NoClearCondition),
                        Map.entry("properties", buildMapProperties(level))))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /// Builds clear-condition properties for the exported map.
    ///
    /// @spec.requires level is not null.
    /// @spec.modifies nothing.
    /// @spec.effects builds the clear-condition properties from the level.
    /// @param level the level to read
    /// @return clear-condition properties
    private static List<Map<String, Object>> buildMapProperties(final Level level) {
        final String conditionType = level.getClearCondition().condition() instanceof Condition.SomeClearCondition some
                ? some.target().name()
                : "NONE";
        return List.of(
                Map.of(
                        KEY_NAME, "ClearConditionType",
                        KEY_TYPE, TYPE_STRING,
                        KEY_VALUE, conditionType),
                Map.of(
                        KEY_NAME, "ClearConditionAmount",
                        KEY_TYPE, TYPE_INT,
                        KEY_VALUE, level.getClearCondition().targetAmount()));
    }
}
