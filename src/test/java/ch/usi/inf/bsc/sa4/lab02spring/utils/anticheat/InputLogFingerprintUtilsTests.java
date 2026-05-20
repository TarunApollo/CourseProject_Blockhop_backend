package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/// Tests for [InputLogFingerprintUtils].
@DisplayName("The Input Log Fingerprint Utils")
@SuppressWarnings({ "java:S2187", "fb-contrib:WOC_WRITE_ONLY_COLLECTION_FIELD" })
class InputLogFingerprintUtilsTests {

    /// Input log used by fingerprint tests.
    private static final List<InputFrameDTO> INPUT_LOG = List.of(
            new InputFrameDTO(0, false, false, false, false),
            new InputFrameDTO(1, true, false, false, false),
            new InputFrameDTO(2, true, false, false, false),
            new InputFrameDTO(3, false, true, false, false),
            new InputFrameDTO(4, false, false, true, false));

    /// Tests for SHA-256 hashing.
    @Nested
    @DisplayName("when hashing text")
    class Hashing {

        /// Checks a known SHA-256 value.
        @Test
        @DisplayName("returns a sha256 hex string")
        void returnsSha256HexString() {
            final String result = InputLogFingerprintUtils.sha256("abc");

            Assertions.assertEquals(
                    "ba7816bf8f01cfea414140de5dae2223"
                            + "b00361a396177a9cb410ff61f20015ad",
                    result);
        }
    }

    /// Tests for full fingerprints.
    @Nested
    @DisplayName("when building a fingerprint")
    class Fingerprint {

        /// Checks all fields in the fingerprint.
        @Test
        @DisplayName("fills all fingerprint fields")
        void fillsAllFingerprintFields() {
            final ExactInputLogFingerprintUtils.ExactCanonical exact =
                    ExactInputLogFingerprintUtils.canonical(INPUT_LOG);
            final JitterInputLogFingerprintUtils.JitterCanonical jitter =
                    JitterInputLogFingerprintUtils.canonical(INPUT_LOG);

            final InputLogFingerprint result = InputLogFingerprintUtils.fingerprint(INPUT_LOG);

            Assertions.assertEquals(InputLogFingerprintUtils.sha256(exact.canonical()), result.exactHash());
            Assertions.assertEquals(InputLogFingerprintUtils.sha256(jitter.canonical()), result.jitterInputHash());
            Assertions.assertEquals(jitter.changeCount(), result.jitterInputChangeCount());
            Assertions.assertEquals(BucketInputLogFingerprintUtils.hashes(INPUT_LOG), result.changeBucketHashes());
            Assertions.assertEquals(INPUT_LOG.size(), result.inputFrameCount());
            Assertions.assertEquals(exact.inputChangeCount(), result.inputChangeCount());
        }

        /// Checks an empty input log.
        @Test
        @DisplayName("handles an empty input log")
        void handlesEmptyInputLog() {
            final String emptyHash = InputLogFingerprintUtils.sha256("");

            final InputLogFingerprint result = InputLogFingerprintUtils.fingerprint(List.of());

            Assertions.assertEquals(emptyHash, result.exactHash());
            Assertions.assertEquals(emptyHash, result.jitterInputHash());
            Assertions.assertEquals(0, result.jitterInputChangeCount());
            Assertions.assertEquals(List.of(emptyHash, emptyHash), result.changeBucketHashes());
            Assertions.assertEquals(0, result.inputFrameCount());
            Assertions.assertEquals(0, result.inputChangeCount());
        }
    }
}
