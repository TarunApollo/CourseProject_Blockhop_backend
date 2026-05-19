package ch.usi.inf.bsc.sa4.lab02spring.controller;

/// Raised when the backend cannot serialize replay payloads for the replay
/// runner after the request has already been accepted as valid.
public final class ReplaySerializationException extends RuntimeException {

    /// Creates the exception with the original serialization failure attached.
    ///
    /// @param cause root serialization failure
    public ReplaySerializationException(final Throwable cause) {
        super("Failed to serialize replay data", cause);
    }
}
