package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

import java.util.ArrayList;
import java.util.List;

/// Builds input hash text after removing small input noise.
final class JitterInputLogFingerprintUtils {

    /// Jump-only input followed by this much idle time is treated as noise.
    private static final int JUMP_X_GAP_FRAMES = 90;
    /// Short opposite left/right input is treated as noise.
    private static final int MAX_CANCEL_X_RUN = 5;

    /// Do not create this helper.
    private JitterInputLogFingerprintUtils() {
    }

    static JitterCanonical canonical(final List<InputFrameDTO> inputLog) {
        final List<InputRun> runs = jitterInputRuns(inputLog);
        final List<InputRun> withoutFakeJumps = removeFakeJumpOnlyRuns(runs);
        final List<InputRun> withoutXNoise = removeHorizontalCancellationNoise(withoutFakeJumps);
        final List<InputRun> normalizedRuns = collapseAdjacentEqualRuns(withoutXNoise);
        final StringBuilder canonical = new StringBuilder();

        for (final InputRun run : normalizedRuns) {
            appendSeparatorIfNeeded(canonical);
            canonical.append(run.normalizedCount())
                    .append(':')
                    .append(run.state().canonical());
        }

        return new JitterCanonical(canonical.toString(), normalizedRuns.size());
    }

    private static List<InputRun> jitterInputRuns(final List<InputFrameDTO> inputLog) {
        final List<InputRun> runs = new ArrayList<>();
        InputState currentRunState = null;
        int runStartFrame = 0;
        int currentRunLength = 0;
        int neutralAfterRun = 0;
        int strippedNeutral = 0;
        int strippedBeforeRun = 0;

        for (final InputFrameDTO frame : inputLog) {
            final InputState current = InputState.from(frame);
            if (current.isNeutral()) {
                strippedNeutral++;
                if (currentRunState != null) {
                    neutralAfterRun++;
                }
                continue;
            }

            if (currentRunState == null) {
                currentRunState = current;
                runStartFrame = frame.frame();
                currentRunLength = 1;
                neutralAfterRun = 0;
                strippedBeforeRun = strippedNeutral;
                continue;
            }

            if (neutralAfterRun > 0) {
                runs.add(new InputRun(
                        currentRunState,
                        runStartFrame,
                        currentRunLength,
                        neutralAfterRun,
                        strippedBeforeRun));
                currentRunState = current;
                runStartFrame = frame.frame();
                currentRunLength = 1;
                neutralAfterRun = 0;
                strippedBeforeRun = strippedNeutral;
                continue;
            }

            if (current.equals(currentRunState)) {
                currentRunLength++;
            } else {
                runs.add(new InputRun(
                        currentRunState,
                        runStartFrame,
                        currentRunLength,
                        0,
                        strippedBeforeRun));
                currentRunState = current;
                runStartFrame = frame.frame();
                currentRunLength = 1;
                strippedBeforeRun = strippedNeutral;
            }
        }

        if (currentRunState != null) {
            runs.add(new InputRun(
                    currentRunState,
                    runStartFrame,
                    currentRunLength,
                    neutralAfterRun,
                    strippedBeforeRun));
        }

        return List.copyOf(runs);
    }

    private static List<InputRun> removeFakeJumpOnlyRuns(final List<InputRun> runs) {
        final List<InputRun> normalized = new ArrayList<>();
        int fakeJumpFrames = 0;

        for (final InputRun run : runs) {
            if (run.state().isJumpOnly()
                    && run.neutralAfter() >= JUMP_X_GAP_FRAMES) {
                fakeJumpFrames += run.length();
                continue;
            }
            normalized.add(run.withAdditionalStrippedFrames(fakeJumpFrames).withoutNeutralFramesAfter());
        }

        return List.copyOf(normalized);
    }

    private static List<InputRun> removeHorizontalCancellationNoise(final List<InputRun> runs) {
        final List<InputRun> normalized = new ArrayList<>();
        int noiseFrames = 0;

        for (final InputRun run : runs) {
            final InputRun current = run.withAdditionalStrippedFrames(noiseFrames);
            final int lastIndex = normalized.size() - 1;
            if (lastIndex >= 1
                    && isCancelableHorizontalInterruption(
                    normalized.get(lastIndex - 1),
                    normalized.get(lastIndex),
                    current)) {
                final InputRun before = normalized.get(lastIndex - 1);
                final InputRun interruption = normalized.remove(lastIndex);
                if (before.state().equals(current.state())) {
                    final int returnStripFrames = Math.min(run.length(), interruption.length());
                    noiseFrames += interruption.length() + returnStripFrames;
                    if (run.length() > returnStripFrames) {
                        addOrMergeAdjacentEqualRun(
                                normalized,
                                run.withoutLeadingFrames(returnStripFrames)
                                        .withAdditionalStrippedFrames(noiseFrames));
                    }
                } else {
                    noiseFrames += interruption.length();
                    addOrMergeAdjacentEqualRun(
                            normalized,
                            run.withAdditionalStrippedFrames(noiseFrames));
                }
                continue;
            }
            addOrMergeAdjacentEqualRun(normalized, current);
        }

        return List.copyOf(normalized);
    }

    private static void addOrMergeAdjacentEqualRun(final List<InputRun> runs, final InputRun run) {
        final int lastIndex = runs.size() - 1;
        if (lastIndex >= 0 && runs.get(lastIndex).state().equals(run.state())) {
            runs.set(lastIndex, runs.get(lastIndex).mergeWith(run));
        } else {
            runs.add(run);
        }
    }

    private static boolean isCancelableHorizontalInterruption(final InputRun before,
                                                             final InputRun interruption,
                                                             final InputRun after) {
        return interruption.length() <= MAX_CANCEL_X_RUN
                && before.state().sameHorizontalDirection(after.state())
                && before.state().oppositeHorizontalDirection(interruption.state());
    }

    private static List<InputRun> collapseAdjacentEqualRuns(final List<InputRun> runs) {
        final List<InputRun> collapsed = new ArrayList<>();

        for (final InputRun run : runs) {
            final int lastIndex = collapsed.size() - 1;
            if (lastIndex >= 0 && collapsed.get(lastIndex).state().equals(run.state())) {
                collapsed.set(lastIndex, collapsed.get(lastIndex).mergeWith(run));
            } else {
                collapsed.add(run);
            }
        }

        return List.copyOf(collapsed);
    }

    private static void appendSeparatorIfNeeded(final StringBuilder builder) {
        if (!builder.isEmpty()) {
            builder.append('|');
        }
    }

    /// Same non-idle input repeated for several frames.
    /// state: input used in this run.
    /// startFrame: first frame in the run.
    /// length: number of non-idle frames in the run.
    /// neutralAfter: idle frames right after the run.
    /// strippedBefore: noise frames removed before this run.
    private record InputRun(
            InputState state,
            int startFrame,
            int length,
            int neutralAfter,
            int strippedBefore) {

        private int normalizedCount() {
            return startFrame - strippedBefore;
        }

        private InputRun withoutNeutralFramesAfter() {
            return new InputRun(state, startFrame, length, 0, strippedBefore);
        }

        private InputRun withAdditionalStrippedFrames(final int extraStripped) {
            return new InputRun(
                    state,
                    startFrame,
                    length,
                    neutralAfter,
                    strippedBefore + extraStripped);
        }

        private InputRun withoutLeadingFrames(final int leadingFrames) {
            return new InputRun(
                    state,
                    startFrame + leadingFrames,
                    length - leadingFrames,
                    neutralAfter,
                    strippedBefore);
        }

        private InputRun mergeWith(final InputRun second) {
            return new InputRun(
                    state,
                    startFrame,
                    length + second.length(),
                    second.neutralAfter(),
                    strippedBefore);
        }
    }

    /// Jitter hash text and number of input changes.
    record JitterCanonical(String canonical, int changeCount) {
    }
}
