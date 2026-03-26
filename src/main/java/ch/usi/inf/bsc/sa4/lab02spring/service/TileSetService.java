package ch.usi.inf.bsc.sa4.lab02spring.service;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import ch.usi.inf.bsc.sa4.lab02spring.utils.TileSetNotLoadedException;
import jakarta.annotation.PostConstruct;


@Service
public class TileSetService {
    private Set<Integer> groundGIDs = Set.of();
    private Map<Integer,String> gidToType = Map.of();
    @PostConstruct
    ///
    /// a parameterless void method that extracts gids from a predefined tileset of type Ground and 
    /// puts them into a set.
    /// 
    public void loadTileSet() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TileSet tileSet = mapper.readValue(
                new ClassPathResource("tileset_batch_1.json").getInputStream(),
                TileSet.class);
            groundGIDs = tileSet.tiles().stream()
                .filter(tile -> Objects.equals(tile.type(), "Ground"))
                .map(tile -> tileSet.firstgid() + tile.id())
                .collect(Collectors.toUnmodifiableSet());
            gidToType = tileSet.tiles().stream()
            .collect(Collectors.toUnmodifiableMap(
                tile -> tileSet.firstgid() + tile.id(),
                tile -> tile.type() != null ? tile.type() : ""
            ));
            
        } catch (IOException e) {
            throw new TileSetNotLoadedException(e);
        }
    }

    public boolean isGroundGID(int gid) {
        return groundGIDs.contains(gid);
    }

    public boolean isObjectGID(int gid) {
        return gidToType.containsKey(gid) && !isGroundGID(gid);
    }

    public String getObjectTileType(int gid){
        return gidToType.getOrDefault(gid, "");
    }
}

