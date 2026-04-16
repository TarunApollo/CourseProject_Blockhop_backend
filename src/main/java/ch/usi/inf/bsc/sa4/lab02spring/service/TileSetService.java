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

@Service
public class TileSetService {
    
    /// Ground GIDs - type is always "Ground", so just store GIDs
    private final Set<Integer> groundGIDs;
    
    /// Object GIDs mapped to their type string
    private final Map<Integer, String> objectGIDs;

    private final TileSet tileSet;

    /// load the tileset and hold the complete tileSet for building frontend needed json
    /// store groundGids and objectGids for light-weight usage
    public TileSetService() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.tileSet = mapper.readValue(
                new ClassPathResource("tileset_batch_1.json").getInputStream(),
                TileSet.class);
            

            ///tmp fix for unnormalized 
            groundGIDs = tileSet.tiles().stream()
                .filter(tile ->
                Objects.equals(tile.type(), "Ground") ||
                Objects.equals(tile.type(), "Semisolid") ||
                Objects.equals(tile.type(), "Damage"))
                .map(tile -> tileSet.firstgid() + tile.id())
                .collect(Collectors.toUnmodifiableSet());
            
            objectGIDs = tileSet.tiles().stream()
                .filter(tile -> !Objects.equals(tile.type(), "Ground")&&
                !Objects.equals(tile.type(),"Semisolid")&&
                !Objects.equals(tile.type(),"Damage"))
                .filter(tile -> tile.type() != null && !tile.type().isEmpty())
                .collect(Collectors.toUnmodifiableMap(
                    tile -> tileSet.firstgid() + tile.id(),
                    TileSet.TileData::type
                ));
        } catch (IOException e) {
            throw new TileSetNotLoadedException(e);
        }
    }

    public TileSet getTileSet()
    {
        return this.tileSet;
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
}
