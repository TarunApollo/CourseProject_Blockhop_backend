package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/// Tile catalog loaded from tile_catalog.json.
///
/// @param tiles all known tile entries
@JsonIgnoreProperties(ignoreUnknown = true)
public record TileCatalog(
        List<Entry> tiles) {
    /// Creates a tile catalog.
    ///
    /// @spec.requires tiles is not null.
    /// @spec.modifies this.
    /// @spec.effects stores the tile entries loaded from JSON.
    public TileCatalog {
    }
}
