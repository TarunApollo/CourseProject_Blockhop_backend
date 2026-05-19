package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

/// Builds the exact input hash text.
final class ExactInputLogFingerprintUtils {

    /// Do not create this helper.
    private ExactInputLogFingerprintUtils() {
    }

    /// Writes every frame exactly as it was recorded.
    ///
    /// @param inputLog input frames from the attempt, in order
    /// @return exact text and number of real input changes
    /* package */ static ExactCanonical canonical(final Iterable<InputFrameDTO> inputLog) {
        final StringBuilder canonical = new StringBuilder();
        InputState previous = null;
        int inputChangeCount = 0;

        for (final InputFrameDTO frame : inputLog) {
            final InputState current = InputState.from(frame);
            appendSeparatorIfNeeded(canonical);
            canonical.append(frame.frame())
                    .append(':')
                    .append(current.canonical());

            if (!current.isNeutral() && !current.equals(previous)) {
                inputChangeCount++;
                previous = current;
            }
        }

        return new ExactCanonical(canonical.toString(), inputChangeCount);
    }

    private static void appendSeparatorIfNeeded(final StringBuilder builder) {
        if (!builder.isEmpty()) {
            builder.append('|');
        }
    }

    /// Exact text and number of real input changes.
    /* package */ record ExactCanonical(String canonical, int inputChangeCount) {
    }
}
