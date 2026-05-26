package ch.usi.inf.bsc.sa4.lab02spring.utils;

///
/// Signals that a sprite catalog could not be loaded successfully.
///
public class SpriteCatalogNotLoadedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    ///
    /// Creates a new exception with the given cause.
    /// @param cause the underlying reason for the sprite catalog loading failure
    ///
    public SpriteCatalogNotLoadedException(final Throwable cause) {
        super("Failed to load spritesheets", cause);
    }
}
