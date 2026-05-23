package ch.usi.inf.bsc.sa4.lab02spring.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;
import ch.usi.inf.bsc.sa4.lab02spring.model.Entry;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileCatalog;
import ch.usi.inf.bsc.sa4.lab02spring.utils.TileSetNotLoadedException;

/// Service that loads and checks tile catalog entries.
@Service
public final class TileCatalogService {
    /// Layer name for world tiles.
    private static final String WORLD_LAYER = "world";

    /// Layer name for object tiles.
    private static final String OBJECT_LAYER = "object";

    /// Raw catalog loaded from JSON.
    private final TileCatalog catalog;

    /// Catalog entries keyed by tile id.
    private final Map<String, Entry> tilesById;

    /// Catalog entries in file order.
    private final List<Entry> tiles;

    /// Loads the tile catalog from the classpath.
    ///
    /// @spec.modifies this.
    /// @spec.effects loads tile_catalog.json and indexes entries by id.
    /// @throws TileSetNotLoadedException if the catalog cannot be loaded
    public TileCatalogService() {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            this.catalog = mapper.readValue(
                    new ClassPathResource("tile_catalog.json").getInputStream(),
                    TileCatalog.class);
            this.tiles = buildTiles(catalog.tiles());
            this.tilesById = tiles.stream().collect(Collectors.toUnmodifiableMap(
                    Entry::id,
                    Function.identity()));
        } catch (IllegalStateException | IOException e) {
            throw new TileSetNotLoadedException(e);
        }
    }

    /// Returns the loaded tile catalog.
    ///
    /// @spec.modifies nothing.
    /// @spec.effects returns the catalog loaded by this service.
    /// @return the full tile catalog
    public TileCatalog getCatalog() {
        return catalog;
    }

    /// Returns all catalog entries.
    ///
    /// @spec.modifies nothing.
    /// @spec.effects returns the catalog entries in file order.
    /// @return all known tiles
    public List<Entry> getTiles() {
        return tiles;
    }

    /// Returns the catalog entry for a tile id.
    ///
    /// @spec.requires tileId is not null.
    /// @spec.modifies nothing.
    /// @spec.effects returns the entry with the given tile id.
    /// @param tileId the tile id to find
    /// @return the matching catalog entry
    /// @throws IllegalArgumentException if the tile id is unknown
    public Entry requireTile(final String tileId) {
        final Entry tile = tilesById.get(tileId);
        if (tile == null) {
            throw new IllegalArgumentException("Unknown tileId: " + tileId);
        }
        return tile;
    }

    /// Checks if a tile belongs on the world layer.
    ///
    /// @spec.requires tileId is not null.
    /// @spec.modifies nothing.
    /// @spec.effects checks the catalog layer for the given tile id.
    /// @param tileId the tile id to check
    /// @return true when the tile is a world tile
    public boolean isWorldTile(final String tileId) {
        final Entry tile = tilesById.get(tileId);
        return tile != null && WORLD_LAYER.equals(tile.layer());
    }

    /// Checks if a tile belongs on the object layer.
    ///
    /// @spec.requires tileId is not null.
    /// @spec.modifies nothing.
    /// @spec.effects checks the catalog layer for the given tile id.
    /// @param tileId the tile id to check
    /// @return true when the tile is an object tile
    public boolean isObjectTile(final String tileId) {
        final Entry tile = tilesById.get(tileId);
        return tile != null && OBJECT_LAYER.equals(tile.layer());
    }

    /// Returns the game type for a tile id.
    ///
    /// @spec.requires tileId is not null.
    /// @spec.modifies nothing.
    /// @spec.effects resolves the catalog type for the given tile id.
    /// @param tileId the tile id to resolve
    /// @return the tile type
    public String getType(final String tileId) {
        return requireTile(tileId).type();
    }

    /// Copies catalog entries into a list after checking duplicates.
    ///
    /// @spec.requires curatedTiles is not null and contains non-null entries.
    /// @spec.modifies nothing.
    /// @spec.effects returns a copy of the catalog entries and rejects duplicate
    ///               ids.
    /// @param curatedTiles entries loaded from JSON
    /// @return unique catalog entries
    private static List<Entry> buildTiles(final List<Entry> curatedTiles) {
        final Map<String, Entry> entries = new LinkedHashMap<>();
        for (final Entry entry : curatedTiles) {
            putUnique(entries, entry);
        }
        return List.copyOf(entries.values());
    }

    /// Adds a catalog entry and rejects duplicate ids.
    ///
    /// @spec.requires entries and entry are not null.
    /// @spec.modifies entries.
    /// @spec.effects adds entry to entries unless an entry with the same id exists.
    /// @param entries entries already added
    /// @param entry   entry to add
    private static void putUnique(
            final Map<String, Entry> entries,
            final Entry entry) {
        if (entries.putIfAbsent(entry.id(), entry) != null) {
            throw new IllegalStateException("Duplicate tile catalog id: " + entry.id());
        }
    }

}
