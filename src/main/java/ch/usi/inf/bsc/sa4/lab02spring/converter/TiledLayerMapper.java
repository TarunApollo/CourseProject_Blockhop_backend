package ch.usi.inf.bsc.sa4.lab02spring.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileSetService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Maps level layers into Tiled-compatible layer structures.
@Component
final class TiledLayerMapper {
    /// Pixel size used for each exported tile.
    private static final int TILE_SIZE = 128;

    /// Resolves object tile type metadata.
    private final TileSetService tileSetService;

    /// Package-private constructor used by the converter package and Spring.
    /* package */ TiledLayerMapper(final TileSetService tileSetService) {
        this.tileSetService = tileSetService;
    }

    /// Package-private helper that exports the world layer in Tiled format.
    /* package */ Map<String, Object> buildWorldLayer(
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

        final Map<String, Object> tiledWorldLayer = new LinkedHashMap<>();
        tiledWorldLayer.put("id", 1);
        tiledWorldLayer.put("name", "World");
        tiledWorldLayer.put("type", "tilelayer");
        tiledWorldLayer.put("width", width);
        tiledWorldLayer.put("height", height);
        tiledWorldLayer.put("opacity", 1);
        tiledWorldLayer.put("visible", Boolean.TRUE);
        tiledWorldLayer.put("x", 0);
        tiledWorldLayer.put("y", 0);
        tiledWorldLayer.put("data", data);
        return tiledWorldLayer;
    }

    /// Package-private helper that exports the object layer in Tiled format.
    /* package */ Map<String, Object> buildObjectLayer(final Map<Position, GameObject> objectLayer) {
        final List<Map<String, Object>> objects = new ArrayList<>(objectLayer.size());
        int idCounter = 1;
        for (final GameObject gameObject : objectLayer.values()) {
            objects.add(toTiledObject(gameObject, idCounter++));
        }

        final Map<String, Object> tiledObjectLayer = new LinkedHashMap<>();
        tiledObjectLayer.put("id", 2);
        tiledObjectLayer.put("name", "QMLayer");
        tiledObjectLayer.put("type", "objectgroup");
        tiledObjectLayer.put("draworder", "topdown");
        tiledObjectLayer.put("opacity", 1);
        tiledObjectLayer.put("visible", Boolean.TRUE);
        tiledObjectLayer.put("x", 0);
        tiledObjectLayer.put("y", 0);
        tiledObjectLayer.put("objects", objects);
        return tiledObjectLayer;
    }

    private Map<String, Object> toTiledObject(final GameObject gameObject, final int id) {
        final Position pos = gameObject.pos();
        final int x = pos.x() * TILE_SIZE;
        final int y = (pos.y() + 1) * TILE_SIZE;

        final Map<String, Object> tiledObject = new LinkedHashMap<>();
        tiledObject.put("id", id);
        tiledObject.put("gid", gameObject.gid());
        tiledObject.put("x", x);
        tiledObject.put("y", y);
        tiledObject.put("width", TILE_SIZE);
        tiledObject.put("height", TILE_SIZE);
        tiledObject.put("visible", Boolean.TRUE);
        tiledObject.put("rotation", 0);
        tiledObject.put("name", "");
        tiledObject.put("type", this.tileSetService.getObjectTileType(gameObject.gid()));

        if (gameObject instanceof Box box) {
            final List<Map<String, Object>> properties = buildBoxProperties(box);
            if (!properties.isEmpty()) {
                tiledObject.put("properties", properties);
            }
        }
        return tiledObject;
    }

    private static List<Map<String, Object>> buildBoxProperties(final Box box) {
        final List<Map<String, Object>> properties = new ArrayList<>();
        if (box.content() instanceof Content.SomeContent someContent) {
            final Map<String, Object> contentProperty = new LinkedHashMap<>();
            contentProperty.put("name", "Content");
            contentProperty.put("type", "string");
            contentProperty.put("value", someContent.coinType().value());
            properties.add(contentProperty);
        }
        return properties;
    }
}
