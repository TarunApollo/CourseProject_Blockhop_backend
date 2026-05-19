package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/// Makes input hashes for anti-cheat checks.
public final class InputLogFingerprintUtils {

    /// Do not create this helper.
    private InputLogFingerprintUtils() {
    }

    /// Builds all hashes for one input log.
    ///
    /// @param inputLog input frames from the attempt, in order
    /// @return exact, jitter, and bucket hashes
    public static InputLogFingerprint fingerprint(final List<InputFrameDTO> inputLog) {
        final ExactInputLogFingerprintUtils.ExactCanonical exactCanonical =
                ExactInputLogFingerprintUtils.canonical(inputLog);
        final JitterInputLogFingerprintUtils.JitterCanonical jitterCanonical =
                JitterInputLogFingerprintUtils.canonical(inputLog);

        return new InputLogFingerprint(
                sha256(exactCanonical.canonical()),
                sha256(jitterCanonical.canonical()),
                jitterCanonical.changeCount(),
                BucketInputLogFingerprintUtils.hashes(inputLog),
                inputLog.size(),
                exactCanonical.inputChangeCount()
        );
    }

    static String sha256(final String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
