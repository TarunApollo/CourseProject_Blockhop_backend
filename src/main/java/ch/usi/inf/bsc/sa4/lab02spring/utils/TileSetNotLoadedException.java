package ch.usi.inf.bsc.sa4.lab02spring.utils;

///
/// Signals that a tileset could not be loaded successfully.
///
public class TileSetNotLoadedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    ///
    /// Creates a new exception with the given cause.
    /// @param cause the underlying reason for the tileset loading failure
    ///
    public TileSetNotLoadedException(final Throwable cause) {
        super("Failed to load tileset", cause);
    }
}
