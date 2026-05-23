package ch.usi.inf.bsc.sa4.lab02spring.utils.converter;

import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.KEY_HEIGHT;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.KEY_NAME;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.KEY_TYPE;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.KEY_VISIBLE;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.KEY_WIDTH;
import static ch.usi.inf.bsc.sa4.lab02spring.utils.converter.TiledConstants.TYPE_STRING;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.Entry;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.service.TileCatalogService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/// Converts level layers to map data for the frontend.
@SuppressWarnings({ "PMD.AtLeastOneConstructor", "PMD.TooManyStaticImports" })
final class TiledLayerMapper {
    /// Size of one tile in pixels.
    private static final int TILE_SIZE = 128;

    /// Builds the world layer with tile ids.
    ///
    /// @spec.requires worldLayer and tileCatalogService are not null.
    /// @spec.modifies nothing.
    /// @spec.effects builds the exported world layer from level tiles.
    /// @param worldLayer         world tiles by grid position
    /// @param width              level width in tiles
    /// @param height             level height in tiles
    /// @param tileCatalogService catalog service used by the converter
    /// @return world layer data
    /* package */ static Map<String, Object> buildWorldLayer(
            final Map<Position, GroundObject> worldLayer,
            final int width,
            final int height,
            final TileCatalogService tileCatalogService) {
        final List<String> data = new ArrayList<>(Collections.nCopies(width * height, ""));

        for (final Map.Entry<Position, GroundObject> entry : worldLayer.entrySet()) {
            final Position pos = entry.getKey();
            final GroundObject groundObject = entry.getValue();
            final int x = pos.x();
            final int y = pos.y();
            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }

            final int idx = y * width + x;
            data.set(idx, groundObject.tileId());
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
                "data", data);
    }

    /// Builds the object layer with tile types from the catalog.
    ///
    /// @spec.requires objectLayer and tileCatalogService are not null.
    /// @spec.modifies nothing.
    /// @spec.effects builds the exported object layer from level objects.
    /// @param objectLayer        objects by grid position
    /// @param tileCatalogService catalog service used to resolve tile types
    /// @return object layer data
    /* package */ static Map<String, Object> buildObjectLayer(
            final Map<Position, GameObject> objectLayer,
            final TileCatalogService tileCatalogService) {
        final List<Map<String, Object>> objects = new ArrayList<>(objectLayer.size());
        int idCounter = 1;
        for (final GameObject gameObject : objectLayer.values()) {
            objects.add(toTiledObject(gameObject, idCounter, tileCatalogService));
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
                "objects", objects);
    }

    /// Converts one game object into frontend map data.
    ///
    /// @spec.requires gameObject and tileCatalogService are not null.
    /// @spec.modifies nothing.
    /// @spec.effects builds one exported object and resolves its type from the
    ///               catalog.
    /// @param gameObject         object to convert
    /// @param id                 object id in the exported layer
    /// @param tileCatalogService catalog service used to resolve tile type
    /// @return object data
    private static Map<String, Object> toTiledObject(
            final GameObject gameObject,
            final int id,
            final TileCatalogService tileCatalogService) {
        final Position pos = gameObject.pos();
        final int x = pos.x() * TILE_SIZE;
        final int y = (pos.y() + 1) * TILE_SIZE;
        final Entry catalogEntry = tileCatalogService.requireTile(gameObject.tileId());

        final List<Map.Entry<String, Object>> entries = new ArrayList<>();
        entries.add(Map.entry("id", id));
        entries.add(Map.entry("tileId", gameObject.tileId()));
        entries.add(Map.entry("x", x));
        entries.add(Map.entry("y", y));
        entries.add(Map.entry(KEY_WIDTH, TILE_SIZE));
        entries.add(Map.entry(KEY_HEIGHT, TILE_SIZE));
        entries.add(Map.entry(KEY_VISIBLE, Boolean.TRUE));
        entries.add(Map.entry("rotation", 0));
        entries.add(Map.entry(KEY_NAME, ""));
        entries.add(Map.entry(KEY_TYPE, catalogEntry.type()));

        if (gameObject instanceof Box box && box.content() instanceof Content.SomeContent) {
            entries.add(Map.entry("properties", buildBoxProperties(box)));
        }

        final Map.Entry<String, Object>[] entriesArray = entries.toArray(new Map.Entry[0]);
        return Map.ofEntries(entriesArray);
    }

    /// Builds the content property for boxes.
    ///
    /// @spec.requires box is not null.
    /// @spec.modifies nothing.
    /// @spec.effects builds exported properties for the box content.
    /// @param box the box to read
    /// @return box properties
    private static List<Map<String, Object>> buildBoxProperties(final Box box) {
        final List<Map<String, Object>> properties = new ArrayList<>();
        if (box.content() instanceof Content.SomeContent someContent) {
            properties.add(Map.of(
                    KEY_NAME, "Content",
                    KEY_TYPE, TYPE_STRING,
                    "value", someContent.coinType().value()));
        }
        return properties;
    }

}
