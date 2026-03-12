package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class TileSetRules {
    private TileSetRules() {}

    public static Set<Integer> extractGroundGIDs(TileSet tileSet) {
        return tileSet.tiles().stream()
                .filter(tile -> Objects.equals(tile.type(), "ground"))
                .map(tile -> tileSet.firstgid() + tile.id())
                .collect(Collectors.toUnmodifiableSet());
    }
}
