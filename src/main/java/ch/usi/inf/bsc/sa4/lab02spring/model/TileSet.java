package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/// Record to parse only the essential tileset JSON data.
/// Jackson ignores extra fields (columns, image, properties, objectgroup, etc.)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TileSet(
    List<TileData> tiles,
    int firstgid,
    int columns,
    int tilewidth,
    int tileheight
) {
    /// Represents minimal tile data: id (relative) and type.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TileData(
        int id,
        String type
    ) {}
}
