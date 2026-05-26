package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/// Tests for [JitterInputLogFingerprintUtils].
@DisplayName("The Jitter Input Log Fingerprint Utils")
@SuppressWarnings("java:S2187")
class JitterInputLogFingerprintUtilsTests {

    /// Tests for jitter canonical text.
    @Nested
    @DisplayName("when building canonical text")
    class CanonicalText {

        /// Checks the empty input log.
        @Test
        @DisplayName("returns empty text for an empty log")
        void returnsEmptyTextForEmptyLog() {
            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(List.of());

            Assertions.assertEquals("", result.canonical());
            Assertions.assertEquals(0, result.changeCount());
        }

        /// Checks that idle frames do not become input changes.
        @Test
        @DisplayName("strips idle frames from the text")
        void stripsIdleFramesFromText() {
            final List<InputFrameDTO> inputLog = List.of(
                    frame(0, false, false, false, false),
                    frame(1, false, false, false, false),
                    frame(2, true, false, false, false),
                    frame(3, true, false, false, false),
                    frame(4, false, false, false, false),
                    frame(5, false, true, false, false));

            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals("0:L1R0J0S0U0D0X0P0|2:L0R1J0S0U0D0X0P0", result.canonical());
            Assertions.assertEquals(2, result.changeCount());
        }

        /// Checks that a fake jump with a long idle gap is removed.
        @Test
        @DisplayName("removes jump noise before later input")
        void removesJumpNoiseBeforeLaterInput() {
            final List<InputFrameDTO> inputLog = new ArrayList<>();
            inputLog.add(frame(0, false, false, true, false));
            addIdleFrames(inputLog, 1, 90);
            inputLog.add(frame(91, true, false, false, false));

            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals("0:L1R0J0S0U0D0X0P0", result.canonical());
            Assertions.assertEquals(1, result.changeCount());
        }

        /// Checks that a shorter idle gap keeps the jump.
        @Test
        @DisplayName("keeps jump input when the idle gap is short")
        void keepsJumpInputWhenIdleGapIsShort() {
            final List<InputFrameDTO> inputLog = new ArrayList<>();
            inputLog.add(frame(0, false, false, true, false));
            addIdleFrames(inputLog, 1, 89);
            inputLog.add(frame(90, true, false, false, false));

            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals("0:L0R0J1S0U0D0X0P0|1:L1R0J0S0U0D0X0P0", result.canonical());
            Assertions.assertEquals(2, result.changeCount());
        }

        /// Checks that a short opposite direction tap is removed.
        @Test
        @DisplayName("removes short horizontal cancellation")
        void removesShortHorizontalCancellation() {
            final List<InputFrameDTO> inputLog = List.of(
                    frame(0, true, false, false, false),
                    frame(1, true, false, false, false),
                    frame(2, true, false, false, false),
                    frame(3, false, true, false, false),
                    frame(4, false, true, false, false),
                    frame(5, true, false, false, false),
                    frame(6, true, false, false, false),
                    frame(7, true, false, false, false),
                    frame(8, true, false, false, false));

            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals("0:L1R0J0S0U0D0X0P0", result.canonical());
            Assertions.assertEquals(1, result.changeCount());
        }

        /// Checks cancellation when the returned run is fully removed.
        @Test
        @DisplayName("removes matching return frames")
        void removesMatchingReturnFrames() {
            final List<InputFrameDTO> inputLog = List.of(
                    frame(0, true, false, false, false),
                    frame(1, true, false, false, false),
                    frame(2, false, true, false, false),
                    frame(3, false, true, false, false),
                    frame(4, true, false, false, false),
                    frame(5, true, false, false, false));

            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals("0:L1R0J0S0U0D0X0P0", result.canonical());
            Assertions.assertEquals(1, result.changeCount());
        }

        /// Checks that long opposite input is real input.
        @Test
        @DisplayName("keeps long horizontal input")
        void keepsLongHorizontalInput() {
            final List<InputFrameDTO> inputLog = List.of(
                    frame(0, true, false, false, false),
                    frame(1, true, false, false, false),
                    frame(2, true, false, false, false),
                    frame(3, false, true, false, false),
                    frame(4, false, true, false, false),
                    frame(5, false, true, false, false),
                    frame(6, false, true, false, false),
                    frame(7, false, true, false, false),
                    frame(8, false, true, false, false),
                    frame(9, true, false, false, false));

            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals("0:L1R0J0S0U0D0X0P0|3:L0R1J0S0U0D0X0P0|9:L1R0J0S0U0D0X0P0", result.canonical());
            Assertions.assertEquals(3, result.changeCount());
        }

        /// Checks that same-direction button changes are not cancellation.
        @Test
        @DisplayName("keeps same direction button changes")
        void keepsSameDirectionButtonChanges() {
            final List<InputFrameDTO> inputLog = List.of(
                    frame(0, true, false, false, false),
                    frame(1, true, false, true, false),
                    frame(2, true, false, false, false));

            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals("0:L1R0J0S0U0D0X0P0|1:L1R0J1S0U0D0X0P0|2:L1R0J0S0U0D0X0P0", result.canonical());
            Assertions.assertEquals(3, result.changeCount());
        }

        /// Checks a cancelled input when the returned state is not the same.
        @Test
        @DisplayName("keeps changed buttons after horizontal cancellation")
        void keepsChangedButtonsAfterHorizontalCancellation() {
            final List<InputFrameDTO> inputLog = List.of(
                    frame(0, true, false, true, false),
                    frame(1, false, true, false, false),
                    frame(2, false, true, false, false),
                    frame(3, true, false, false, false));

            final JitterInputLogFingerprintUtils.JitterCanonical result =
                    JitterInputLogFingerprintUtils.canonical(inputLog);

            Assertions.assertEquals("0:L1R0J1S0U0D0X0P0|1:L1R0J0S0U0D0X0P0", result.canonical());
            Assertions.assertEquals(2, result.changeCount());
        }
    }

    /// Builds one input frame.
    private static InputFrameDTO frame(final int frame,
                                       final boolean left,
                                       final boolean right,
                                       final boolean jump,
                                       final boolean run) {
        return new InputFrameDTO(frame, left, right, jump, run);
    }

    /// Adds idle frames from start to end, both included.
    private static void addIdleFrames(final Collection<InputFrameDTO> inputLog,
                                      final int startFrame,
                                      final int endFrame) {
        for (int frame = startFrame; frame <= endFrame; frame++) {
            inputLog.add(frame(frame, false, false, false, false));
        }
    }
}
