package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.List;

/// Record to parse only the essential tileset JSON data.
/// Jackson ignores extra fields (columns, image, properties, objectgroup, etc.)
public record TileSet(
    int firstgid,
    List<TileData> tiles
) {
    /// Represents minimal tile data: id (relative) and type.
    public record TileData(
        int id,
        String type
    ) {}
}
