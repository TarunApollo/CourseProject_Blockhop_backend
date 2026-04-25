package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/// Maps level layers into Tiled-compatible layer structures.
@SuppressWarnings("PMD.OnlyOneReturn")
final class TiledLayerMapper {
    /// Pixel size used for each exported tile.
    private static final int TILE_SIZE = 128;

    /// JSON key for the Tiled `name` field.
    private static final String KEY_NAME = "name";
    /// JSON key for the Tiled `type` field.
    private static final String KEY_TYPE = "type";
    /// JSON key for the Tiled `visible` field.
    private static final String KEY_VISIBLE = "visible";
    /// JSON key for the Tiled `width` field.
    private static final String KEY_WIDTH = "width";
    /// JSON key for the Tiled `height` field.
    private static final String KEY_HEIGHT = "height";

    private TiledLayerMapper() {
    }

    /// Package-private helper that exports the world layer in Tiled format.
    /* package */ static Map<String, Object> buildWorldLayer(
            final Map<Position, GroundObject> worldLayer,
            final int width,
            final int height) {
        final List<Integer> data = new ArrayList<>(Collections.nCopies(width * height, 0));

        for (final Map.Entry<Position, GroundObject> entry : worldLayer.entrySet()) {
            final Position pos = entry.getKey();
            final GroundObject groundObject = entry.getValue();
            final int x = pos.x();
            final int y = pos.y();
            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }

            final int idx = y * width + x;
            data.set(idx, groundObject.gid());
        }

        return Map.of(
            "id", 1,
            KEY_NAME, "World",
            KEY_TYPE, "tilelayer",
            KEY_WIDTH, width,
            KEY_HEIGHT, height,
            "opacity", 1,
            KEY_VISIBLE, Boolean.TRUE,
            "x", 0,
            "y", 0,
            "data", data
        );
    }

    /// Package-private helper that exports the object layer in Tiled format.
    /* package */ static Map<String, Object> buildObjectLayer(
            final Map<Position, GameObject> objectLayer,
            final TileSetService tileSetService) {
        final List<Map<String, Object>> objects = new ArrayList<>(objectLayer.size());
        int idCounter = 1;
        for (final GameObject gameObject : objectLayer.values()) {
            objects.add(toTiledObject(gameObject, idCounter, tileSetService));
            idCounter++;
        }

        return Map.of(
            "id", 2,
            KEY_NAME, "QMLayer",
            KEY_TYPE, "objectgroup",
            "draworder", "topdown",
            "opacity", 1,
            KEY_VISIBLE, Boolean.TRUE,
            "x", 0,
            "y", 0,
            "objects", objects
        );
    }

    private static Map<String, Object> toTiledObject(
            final GameObject gameObject,
            final int id,
            final TileSetService tileSetService) {
        final Position pos = gameObject.pos();
        final int x = pos.x() * TILE_SIZE;
        final int y = (pos.y() + 1) * TILE_SIZE;
        final String type = tileSetService.getObjectTileType(gameObject.gid());

        if (gameObject instanceof Box box && box.content() instanceof Content.SomeContent) {
            return Map.ofEntries(
                Map.entry("id", id),
                Map.entry("gid", gameObject.gid()),
                Map.entry("x", x),
                Map.entry("y", y),
                Map.entry(KEY_WIDTH, TILE_SIZE),
                Map.entry(KEY_HEIGHT, TILE_SIZE),
                Map.entry(KEY_VISIBLE, Boolean.TRUE),
                Map.entry("rotation", 0),
                Map.entry(KEY_NAME, ""),
                Map.entry(KEY_TYPE, type),
                Map.entry("properties", buildBoxProperties(box))
            );
        }
        return Map.ofEntries(
            Map.entry("id", id),
            Map.entry("gid", gameObject.gid()),
            Map.entry("x", x),
            Map.entry("y", y),
            Map.entry(KEY_WIDTH, TILE_SIZE),
            Map.entry(KEY_HEIGHT, TILE_SIZE),
            Map.entry(KEY_VISIBLE, Boolean.TRUE),
            Map.entry("rotation", 0),
            Map.entry(KEY_NAME, ""),
            Map.entry(KEY_TYPE, type)
        );
    }

    private static List<Map<String, Object>> buildBoxProperties(final Box box) {
        if (box.content() instanceof Content.SomeContent someContent) {
            return List.of(Map.of(
                KEY_NAME, "Content",
                KEY_TYPE, "string",
                "value", someContent.coinType().value()
            ));
        }
        return List.of();
    }
}
