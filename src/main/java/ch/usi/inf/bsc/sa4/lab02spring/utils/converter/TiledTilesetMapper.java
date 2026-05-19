package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// Maps tileset domain objects into Tiled-compatible structures.
final class TiledTilesetMapper {
    /// JSON key for the Tiled `name` field.
    private static final String KEY_NAME = "name";
    /// JSON key for the Tiled `type` field.
    private static final String KEY_TYPE = "type";
    /// JSON key for the Tiled `visible` field.
    private static final String KEY_VISIBLE = "visible";

    private TiledTilesetMapper() {
    }

    /// Package-private helper that exports the tileset in Tiled format.
    /* package */ static Map<String, Object> buildTileset(final TileSet tileSet) {
        final Map<Integer, Map<String, Object>> tilesById = tileSet.tiles().stream()
                .collect(Collectors.toUnmodifiableMap(
                        TileSet.TileData::id,
                        TiledTilesetMapper::toTiledTile));

        final List<Map<String, Object>> tiles = IntStream.range(0, tileSet.tilecount())
                .<Map<String, Object>>mapToObj(i -> {
                    final Map<String, Object> existing = tilesById.get(i);
                    return existing != null ? existing : Map.of("id", i, KEY_TYPE, "");
                })
                .toList();

        return Map.ofEntries(
                Map.entry("firstgid", tileSet.firstgid()),
                Map.entry(KEY_NAME, tileSet.name()),
                Map.entry("tilewidth", tileSet.tilewidth()),
                Map.entry("tileheight", tileSet.tileheight()),
                Map.entry("tilecount", tileSet.tilecount()),
                Map.entry("columns", tileSet.columns()),
                Map.entry("image", tileSet.image()),
                Map.entry("imagewidth", tileSet.imagewidth()),
                Map.entry("imageheight", tileSet.imageheight()),
                Map.entry("margin", tileSet.margin()),
                Map.entry("spacing", tileSet.spacing()),
                Map.entry("tiles", tiles));
    }

    private static Map<String, Object> toTiledTile(final TileSet.TileData tile) {
        final String type = tile.type() != null ? tile.type() : "";
        final boolean hasProperties = tile.properties() != null && !tile.properties().isEmpty();
        final boolean hasObjectGroup = tile.objectgroup() != null;

        final List<Map.Entry<String, Object>> entries = new ArrayList<>(List.of(
                Map.entry("id", tile.id()),
                Map.entry(KEY_TYPE, type)));

        if (hasObjectGroup) {
            entries.add(Map.entry(
                    "objectgroup", toTiledObjectGroup(tile.objectgroup())));
        }

        if (hasProperties) {
            entries.add(Map.entry(
                    "properties",
                    tile.properties().stream().map(TiledTilesetMapper::toTiledProperty).toList()));
        }

        return Map.ofEntries(entries.toArray(new Map.Entry[0]));
    }

    private static Map<String, Object> toTiledProperty(final TileSet.Property property) {
        return Map.of(
                KEY_NAME, property.name(),
                KEY_TYPE, property.type(),
                "value", property.value());
    }

    private static Map<String, Object> toTiledObjectGroup(final TileSet.ObjectGroup objectGroup) {
        final List<Map<String, Object>> objects = objectGroup.objects().stream()
                .map(TiledTilesetMapper::toTiledTileObject)
                .toList();
        return Map.of(
                "draworder", objectGroup.draworder(),
                KEY_NAME, objectGroup.name(),
                "opacity", objectGroup.opacity(),
                KEY_TYPE, objectGroup.type(),
                KEY_VISIBLE, objectGroup.visible(),
                "x", objectGroup.x(),
                "y", objectGroup.y(),
                "objects", objects);
    }

    private static Map<String, Object> toTiledTileObject(final TileSet.TileObject object) {

        final List<Map.Entry<String, Object>> entries = new ArrayList<>(List.of(
                Map.entry("id", object.id()),
                Map.entry(KEY_NAME, object.name()),
                Map.entry(KEY_TYPE, object.type()),
                Map.entry(KEY_VISIBLE, object.visible()),
                Map.entry("rotation", object.rotation()),
                Map.entry("x", object.x()),
                Map.entry("y", object.y()),
                Map.entry("width", object.width()),
                Map.entry("height", object.height())));

        if (object.polygon() != null && !object.polygon().isEmpty()) {
            final List<Map<String, Object>> polygon = object.polygon().stream()
                    .map(TiledTilesetMapper::toTiledPoint)
                    .toList();
            entries.add(Map.entry("polygon", polygon));
        }

        return Map.ofEntries(entries.toArray(new Map.Entry[0]));

    }

    private static Map<String, Object> toTiledPoint(final TileSet.Point point) {
        return Map.of(
                "x", point.x(),
                "y", point.y());
    }
}
