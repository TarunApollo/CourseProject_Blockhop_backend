package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

/// Violation codes from heartbeat validation.
public enum ViolationCode {
    /// freefall acceleration derived from positions does not match expected gravity
    GRAVITY_MISMATCH,
    /// player moved farther than possible according to logic
    DISPLACEMENT_EXCEEDED,
    /// player stayed at the same height without support below
    FLYING_WITHOUT_SUPPORT,
    /// heartbeat frame did not match the serverside expected frame
    FRAME_MISMATCH,
    /// heartbeat arrived too late for the expected frame schedule
    HEARTBEAT_TIMEOUT
}
