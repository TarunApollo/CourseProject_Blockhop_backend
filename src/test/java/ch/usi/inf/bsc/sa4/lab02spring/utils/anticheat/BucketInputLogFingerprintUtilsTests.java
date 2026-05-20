package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/// Tests for [BucketInputLogFingerprintUtils].
@DisplayName("The Bucket Input Log Fingerprint Utils")
class BucketInputLogFingerprintUtilsTests {

    /// Tests for bucket hashes.
    @Nested
    @DisplayName("when building bucket hashes")
    class Hashes {

        /// Checks the empty input log.
        @Test
        @DisplayName("returns empty hashes for an empty log")
        void returnsEmptyHashesForEmptyLog() {
            final List<String> result = BucketInputLogFingerprintUtils.hashes(List.of());
            final String emptyHash = InputLogFingerprintUtils.sha256("");

            Assertions.assertEquals(List.of(emptyHash, emptyHash), result);
        }

        /// Checks an input log with only idle frames.
        @Test
        @DisplayName("returns empty hashes for neutral input")
        void returnsEmptyHashesForNeutralInput() {
            final List<InputFrameDTO> inputLog = List.of(
                    new InputFrameDTO(0, false, false, false, false),
                    new InputFrameDTO(10, false, false, false, false));
            final String emptyHash = InputLogFingerprintUtils.sha256("");

            final List<String> result = BucketInputLogFingerprintUtils.hashes(inputLog);

            Assertions.assertEquals(List.of(emptyHash, emptyHash), result);
        }

        /// Checks that buttons are grouped by bucket.
        @Test
        @DisplayName("groups buttons by shifted buckets")
        void groupsButtonsByShiftedBuckets() {
            final List<InputFrameDTO> inputLog = List.of(
                    new InputFrameDTO(0, false, false, false, false),
                    new InputFrameDTO(2, true, false, false, false),
                    new InputFrameDTO(9, false, false, true, false),
                    new InputFrameDTO(10, false, true, false, false),
                    new InputFrameDTO(15, false, false, false, true));
            final String unshifted = InputLogFingerprintUtils.sha256(
                    "0:L1R0J1S0|1:L0R1J0S1");
            final String shifted = InputLogFingerprintUtils.sha256(
                    "0:L1R0J0S0|1:L0R1J1S0|2:L0R0J0S1");

            final List<String> result = BucketInputLogFingerprintUtils.hashes(inputLog);

            Assertions.assertEquals(List.of(unshifted, shifted), result);
        }
    }
}
