package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class is designed to extract and identify ground tiles
 * based on their global tile IDs (GIDs). (at least for now)
 *
 */
public final class TileSetRules {
    private TileSetRules() {}

    public static Set<Integer> extractGroundGIDs(TileSet tileSet) {
        return tileSet.tiles().stream()
                .filter(tile -> Objects.equals(tile.type(), "ground"))
                .map(tile -> tileSet.firstgid() + tile.id())
                .collect(Collectors.toUnmodifiableSet());
    }

    public static boolean isGroundGID(TileSet tileSet, int gid) {
        return extractGroundGIDs(tileSet).contains(gid);
    }
}
