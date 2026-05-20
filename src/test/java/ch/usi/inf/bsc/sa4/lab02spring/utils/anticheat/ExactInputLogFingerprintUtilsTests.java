package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/// Tests for [ExactInputLogFingerprintUtils].
@DisplayName("The Exact Input Log Fingerprint Utils")
@SuppressWarnings("java:S2187")
class ExactInputLogFingerprintUtilsTests {

    /// Tests for exact canonical text.
    @Nested
    @DisplayName("when building canonical text")
    class CanonicalText {

        /// Checks the empty input log.
        @Test
        @DisplayName("returns empty text for an empty log")
        void returnsEmptyTextForEmptyLog() {
            final ExactInputLogFingerprintUtils.ExactCanonical result =
                    ExactInputLogFingerprintUtils.canonical(List.of());

            Assertions.assertEquals("", result.canonical());
            Assertions.assertEquals(0, result.inputChangeCount());
        }

        /// Checks frame order and separators.
        @Test
        @DisplayName("keeps every frame in order")
        void keepsEveryFrameInOrder() {
            final List<InputFrameDTO> inputLog = List.of(
                    new InputFrameDTO(0, false, false, false, false),
                    new InputFrameDTO(1, true, false, false, false),
                    new InputFrameDTO(2, true, false, false, false),
                    new InputFrameDTO(3, false, false, false, false),
                    new InputFrameDTO(4, false, true, false, false),
                    new InputFrameDTO(5, false, true, true, false),
                    new InputFrameDTO(6, false, false, false, false),
                    new InputFrameDTO(7, true, false, false, false));

            final ExactInputLogFingerprintUtils.ExactCanonical result =
                    ExactInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals(
                    "0:L0R0J0S0|1:L1R0J0S0|2:L1R0J0S0|3:L0R0J0S0"
                            + "|4:L0R1J0S0|5:L0R1J1S0|6:L0R0J0S0|7:L1R0J0S0",
                    result.canonical());
            Assertions.assertEquals(4, result.inputChangeCount());
        }
    }
}
