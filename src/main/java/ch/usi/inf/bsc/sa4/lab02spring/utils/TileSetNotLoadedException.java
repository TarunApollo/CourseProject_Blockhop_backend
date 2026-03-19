package ch.usi.inf.bsc.sa4.lab02spring.utils;

public class TileSetNotLoadedException extends RuntimeException {
    public TileSetNotLoadedException(Throwable cause) {
        super("Failed to load tileset", cause);
    }
}
