package ch.usi.inf.bsc.sa4.lab02spring.service;


import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

@Service
@SuppressWarnings("NullAway.Init")
public class ThumbnailService {
    private final TileSetService tileSetService;
    private final int firstgid;
    private final int columns;
    private final int tilewidth;
    private final int tileheight;


    public ThumbnailService(TileSetService tileSetService) {
    this.tileSetService = tileSetService;
    this.firstgid = tileSetService.getFirstGid();
    this.columns = tileSetService.getColumns();
    this.tilewidth = tileSetService.getTileWidth();
    this.tileheight = tileSetService.getTileHeight();
}
    private static final int THUMB_TILE_SIZE = 4;
    private BufferedImage tilesetImage;

    @PostConstruct
    void loadTilesetImage() {
        try {
            tilesetImage = Objects.requireNonNull(
                ImageIO.read(new ClassPathResource("tiles.png").getInputStream()),
                "Failed to decode tiles.png"
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load tiles.png", e);
        }
    }
}
