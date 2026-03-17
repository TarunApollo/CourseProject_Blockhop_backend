package ch.usi.inf.bsc.sa4.lab02spring.utils;

public class TileSetNotLoadedException extends RuntimeException {
    public TileSetNotLoadedException() {
        super("Failed to load tileset");
    }

    public TileSetNotLoadedException(Throwable cause) {
        super("Failed to load tileset", cause);
    }

    public TileSetNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
