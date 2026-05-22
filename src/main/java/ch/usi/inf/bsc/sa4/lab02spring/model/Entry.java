package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/// One tile listed in the tile catalog.
///
/// @param id       stable tile id used by the backend
/// @param type     game object or world tile type
/// @param category group used to organize similar tiles
/// @param layer    level layer where this tile can be placed
@JsonIgnoreProperties(ignoreUnknown = true)
public record Entry(
        String id,
        String type,
        String category,
        String layer) {
    /// Creates one tile catalog entry.
    ///
    /// @spec.requires id, type, category, and layer are not null.
    /// @spec.modifies this.
    /// @spec.effects stores the JSON fields for one tile catalog entry.
    public Entry {
    }
}
