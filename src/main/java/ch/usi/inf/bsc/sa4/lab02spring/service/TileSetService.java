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
    private TileSet tileSet;
    
    /// Ground GIDs - type is always "Ground", so just store GIDs
    private Set<Integer> groundGIDs = Set.of();
    
    /// Object GIDs mapped to their type string
    private Map<Integer, String> objectGIDs = Map.of();


    @PostConstruct
    ///
    /// Loads the tileset from JSON and categorizes GIDs into ground and object sets.
    ///
    public void loadTileSet() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TileSet loadedTileSet = mapper.readValue(
                new ClassPathResource("tileset_batch_1.json").getInputStream(),
                TileSet.class);
            
            groundGIDs = tileSet.tiles().stream()
                .filter(tile -> Objects.equals(tile.type(), "Ground"))
                .map(tile -> tileSet.firstgid() + tile.id())
                .collect(Collectors.toUnmodifiableSet());
            
            objectGIDs = tileSet.tiles().stream()
                .filter(tile -> !Objects.equals(tile.type(), "Ground"))
                .filter(tile -> tile.type() != null && !tile.type().isEmpty())
                .collect(Collectors.toUnmodifiableMap(
                    tile -> tileSet.firstgid() + tile.id(),
                    TileSet.TileData::type
                ));
            this.tileSet = loadedTileSet;
        } catch (IOException e) {
            throw new TileSetNotLoadedException(e);
        }
    }

    public boolean isGroundGID(int gid) {
        return groundGIDs.contains(gid);
    }

    public boolean isObjectGID(int gid) {
        return objectGIDs.containsKey(gid);
    }

    public String getObjectTileType(int gid) {
        return objectGIDs.getOrDefault(gid, "");
    }

    ///getter 
    public int getFirstGid() { return tileSet.firstgid(); }
    public int getColumns() { return tileSet.columns(); }
    public int getTileWidth() { return tileSet.tilewidth(); }
    public int getTileHeight() { return tileSet.tileheight(); }
}
