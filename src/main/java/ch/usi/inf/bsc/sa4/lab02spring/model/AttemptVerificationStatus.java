package ch.usi.inf.bsc.sa4.lab02spring.model;

/// Verification states assigned to attempts after replay and fingerprint checks.
public enum AttemptVerificationStatus {
    /// The attempt has not yet been checked by the anti-cheat flow.
    NOT_VERIFIED,

    /// The attempt passed replay and fingerprint checks.
    LEGIT,

    /// The attempt failed replay or frame-count verification.
    CHEATED,

    /// The replay process could not complete successfully.
    REPLAY_ERROR,

    /// The attempt replayed successfully but matched suspicious fingerprint patterns.
    SUSPICIOUS
}
