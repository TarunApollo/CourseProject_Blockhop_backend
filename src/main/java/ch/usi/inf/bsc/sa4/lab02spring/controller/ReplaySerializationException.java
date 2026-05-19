package ch.usi.inf.bsc.sa4.lab02spring.controller;

/// Raised when the backend cannot serialize replay payloads for the replay
/// runner after the request has already been accepted as valid.
public final class ReplaySerializationException extends RuntimeException {

    /// Sonarqube bs fix
    /// This exception is never actually 
    //  serialized so I don't see why we should have this.
    private static final long serialVersionUID = 1L;

    /// Creates the exception with the original serialization failure attached.
    ///
    /// @param cause root serialization failure
    public ReplaySerializationException(final Throwable cause) {
        super("Failed to serialize replay data", cause);
    }
}
