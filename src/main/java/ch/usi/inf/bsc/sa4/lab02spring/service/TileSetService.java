package ch.usi.inf.bsc.sa4.lab02spring.service;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class TileSetService {
    private Set<Integer> groundGIDs = Set.of();

    @PostConstruct
    public void extractGroundGIDs() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TileSet tileSet = mapper.readValue(
                new ClassPathResource("tileset_batch_1.json").getInputStream(),
                TileSet.class);
            groundGIDs = tileSet.tiles().stream()
                .filter(tile -> Objects.equals(tile.type(), "Ground"))
                .map(tile -> tileSet.firstgid() + tile.id())
                .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load tileset", e);
        }
    }

    public boolean isGroundGID(int gid) {
        return groundGIDs.contains(gid);
    }
}

