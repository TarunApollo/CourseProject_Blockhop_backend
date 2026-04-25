package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Maps tileset domain objects into Tiled-compatible structures.
@SuppressWarnings({"PMD.UseConcurrentHashMap", "PMD.AvoidInstantiatingObjectsInLoops"})
final class TiledTilesetMapper {
    /// JSON key for the Tiled `name` field.
    private static final String KEY_NAME = "name";
    /// JSON key for the Tiled `type` field.
    private static final String KEY_TYPE = "type";

    private TiledTilesetMapper() {
    }

    /// Package-private helper that exports the tileset in Tiled format.
    /* package */ static Map<String, Object> buildTileset(final TileSet tileSet) {
        final Map<Integer, Map<String, Object>> tilesById = new LinkedHashMap<>(tileSet.tiles().size());
        for (final TileSet.TileData tile : tileSet.tiles()) {
            tilesById.put(tile.id(), toTiledTile(tile));
        }

        final List<Map<String, Object>> tiles = new ArrayList<>(tileSet.tilecount());
        for (int i = 0; i < tileSet.tilecount(); i++) {
            final Map<String, Object> existing = tilesById.get(i);
            if (existing != null) {
                tiles.add(existing);
                continue;
            }

            final Map<String, Object> defaultTile = new LinkedHashMap<>();
            defaultTile.put("id", i);
            defaultTile.put(KEY_TYPE, "");
            tiles.add(defaultTile);
        }

        final Map<String, Object> tiledTileset = new LinkedHashMap<>();
        tiledTileset.put("firstgid", tileSet.firstgid());
        tiledTileset.put(KEY_NAME, tileSet.name());
        tiledTileset.put("tilewidth", tileSet.tilewidth());
        tiledTileset.put("tileheight", tileSet.tileheight());
        tiledTileset.put("tilecount", tileSet.tilecount());
        tiledTileset.put("columns", tileSet.columns());
        tiledTileset.put("image", tileSet.image());
        tiledTileset.put("imagewidth", tileSet.imagewidth());
        tiledTileset.put("imageheight", tileSet.imageheight());
        tiledTileset.put("margin", tileSet.margin());
        tiledTileset.put("spacing", tileSet.spacing());
        tiledTileset.put("tiles", tiles);
        return tiledTileset;
    }

    private static Map<String, Object> toTiledTile(final TileSet.TileData tile) {
        final Map<String, Object> tiledTile = new LinkedHashMap<>();
        tiledTile.put("id", tile.id());
        tiledTile.put(KEY_TYPE, tile.type() != null ? tile.type() : "");

        if (tile.properties() != null && !tile.properties().isEmpty()) {
            final List<Map<String, Object>> properties = new ArrayList<>(tile.properties().size());
            for (final TileSet.Property property : tile.properties()) {
                properties.add(toTiledProperty(property));
            }
            tiledTile.put("properties", properties);
        }

        if (tile.objectgroup() != null) {
            tiledTile.put("objectgroup", toTiledObjectGroup(tile.objectgroup()));
        }
        return tiledTile;
    }

    private static Map<String, Object> toTiledProperty(final TileSet.Property property) {
        final Map<String, Object> tiledProperty = new LinkedHashMap<>();
        tiledProperty.put(KEY_NAME, property.name());
        tiledProperty.put(KEY_TYPE, property.type());
        tiledProperty.put("value", property.value());
        return tiledProperty;
    }

    private static Map<String, Object> toTiledObjectGroup(final TileSet.ObjectGroup objectGroup) {
        final List<Map<String, Object>> objects = new ArrayList<>(objectGroup.objects().size());
        for (final TileSet.TileObject object : objectGroup.objects()) {
            objects.add(toTiledTileObject(object));
        }

        final Map<String, Object> tiledObjectGroup = new LinkedHashMap<>();
        tiledObjectGroup.put("draworder", objectGroup.draworder());
        tiledObjectGroup.put(KEY_NAME, objectGroup.name());
        tiledObjectGroup.put("opacity", objectGroup.opacity());
        tiledObjectGroup.put(KEY_TYPE, objectGroup.type());
        tiledObjectGroup.put("visible", objectGroup.visible());
        tiledObjectGroup.put("x", objectGroup.x());
        tiledObjectGroup.put("y", objectGroup.y());
        tiledObjectGroup.put("objects", objects);
        return tiledObjectGroup;
    }

    private static Map<String, Object> toTiledTileObject(final TileSet.TileObject object) {
        final Map<String, Object> tiledObject = new LinkedHashMap<>();
        tiledObject.put("id", object.id());
        tiledObject.put(KEY_NAME, object.name());
        tiledObject.put(KEY_TYPE, object.type());
        tiledObject.put("visible", object.visible());
        tiledObject.put("rotation", object.rotation());
        tiledObject.put("x", object.x());
        tiledObject.put("y", object.y());
        tiledObject.put("width", object.width());
        tiledObject.put("height", object.height());

        if (object.polygon() != null && !object.polygon().isEmpty()) {
            final List<Map<String, Object>> polygon = new ArrayList<>(object.polygon().size());
            for (final TileSet.Point point : object.polygon()) {
                polygon.add(toTiledPoint(point));
            }
            tiledObject.put("polygon", polygon);
        }
        return tiledObject;
    }

    private static Map<String, Object> toTiledPoint(final TileSet.Point point) {
        final Map<String, Object> tiledPoint = new LinkedHashMap<>();
        tiledPoint.put("x", point.x());
        tiledPoint.put("y", point.y());
        return tiledPoint;
    }
}
