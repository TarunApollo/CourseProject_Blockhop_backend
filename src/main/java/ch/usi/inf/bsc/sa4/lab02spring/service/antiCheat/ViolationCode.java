package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

/// Violation codes from heartbeat validation.
public enum ViolationCode {
    /// runtime gravity does not match the expected value
    GRAVITY_MISMATCH,
    /// player moved farther than possible according to logic
    DISPLACEMENT_EXCEEDED,
    /// player stayed at the same height without support below
    FLYING_WITHOUT_SUPPORT
}
