package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/// Creates stable hashes and canonical input sequences for anti-cheat checks.
@Service
@SuppressWarnings("PMD.TooManyMethods")
public class InputLogFingerprintService {

    /// Default number of frames grouped into one fuzzy input bucket.
    private static final int DEFAULT_BUCKET_SIZE = 10;
    /// Bucket offsets used to produce fuzzy hashes with shifted boundaries.
    private static final List<Integer> DEFAULT_BUCKET_OFFSETS = List.of(0, 5);
    /// Neutral-frame gap that identifies jump-only runs likely caused by jitter.
    private static final int JUMP_ONLY_HORIZONTAL_DELTA_FRAMES = 90;
    /// Maximum opposite-direction run length treated as horizontal input noise.
    private static final int MAX_CANCELABLE_HORIZONTAL_RUN_FRAMES = 5;

    /// Builds all anti-cheat fingerprints for the provided input log.
    /// @param inputLog the ordered input frames recorded during an attempt
    /// @return exact, jitter-normalized, and bucketed fingerprints
    public InputLogFingerprint fingerprint(final List<InputFrameDTO> inputLog) {
        final List<InputChange> changes = nonNeutralInputChanges(inputLog);
        final String exactCanonical = canonicalExact(inputLog);
        final JitterCanonical jitterCanonical = jitterCanonical(inputLog);
        final List<String> changeBucketHashes = DEFAULT_BUCKET_OFFSETS.stream()
                .map(offset -> canonicalBucketedCombinedInputs(inputLog, DEFAULT_BUCKET_SIZE, offset))
                .map(InputLogFingerprintService::sha256)
                .toList();

        return new InputLogFingerprint(
                sha256(exactCanonical),
                sha256(jitterCanonical.canonical()),
                jitterCanonical.changeCount(),
                changeBucketHashes,
                inputLog.size(),
                changes.size()
        );
    }

    /// Converts each frame into the exact canonical input representation.
    /// @param inputLog the ordered input frames recorded during an attempt
    /// @return a frame-by-frame canonical string
    public String canonicalExact(final List<InputFrameDTO> inputLog) {
        final StringBuilder canonical = new StringBuilder();
        for (final InputFrameDTO frame : inputLog) {
            appendSeparatorIfNeeded(canonical);
            canonical.append(frame.frame())
                    .append(':')
                    .append(InputState.from(frame).canonical());
        }
        return canonical.toString();
    }

    private JitterCanonical jitterCanonical(final List<InputFrameDTO> inputLog) {
        final List<InputRun> runs = jitterInputRuns(inputLog);
        final List<InputRun> withoutFakeJumps = removeFakeJumpOnlyRuns(runs);
        final List<InputRun> withoutHorizontalNoise = removeHorizontalCancellationNoise(withoutFakeJumps);
        final List<InputRun> normalizedRuns = collapseAdjacentEqualRuns(withoutHorizontalNoise);
        final StringBuilder canonical = new StringBuilder();

        for (final InputRun run : normalizedRuns) {
            appendSeparatorIfNeeded(canonical);
            canonical.append(run.normalizedCount())
                    .append(':')
                    .append(run.state().canonical());
        }

        return new JitterCanonical(canonical.toString(), normalizedRuns.size());
    }

    /// Groups input frames into buckets and records combined inputs per bucket.
    /// @param inputLog the ordered input frames recorded during an attempt
    /// @param bucketSize the number of frames included in each bucket
    /// @param offset the frame offset applied before bucket assignment
    /// @return the canonical fuzzy bucket representation
    public String canonicalBucketedCombinedInputs(final List<InputFrameDTO> inputLog,
                                                  final int bucketSize,
                                                  final int offset) {
        if (bucketSize <= 0) {
            throw new IllegalArgumentException("bucketSize must be positive");
        }

        final StringBuilder canonical = new StringBuilder();
        Integer currentBucket = null;
        BucketInputState currentBucketInput = new BucketInputState();

        for (final InputFrameDTO frame : inputLog) {
            final int bucket = bucket(frame.frame(), bucketSize, offset);
            if (currentBucket == null) {
                currentBucket = bucket;
            } else if (bucket != currentBucket) {
                appendBucketInputIfNeeded(canonical, currentBucket, currentBucketInput);
                currentBucket = bucket;
                currentBucketInput = new BucketInputState();
            }
            currentBucketInput.include(InputState.from(frame));
        }

        if (currentBucket != null) {
            appendBucketInputIfNeeded(canonical, currentBucket, currentBucketInput);
        }
        return canonical.toString();
    }

    private List<InputRun> jitterInputRuns(final List<InputFrameDTO> inputLog) {
        final ArrayList<InputRun> runs = new ArrayList<>();
        InputState currentRunState = null;
        int currentRunStartFrame = 0;
        int currentRunLength = 0;
        int neutralFramesAfterCurrentRun = 0;
        int strippedNeutralFrames = 0;
        int currentRunStrippedFramesBefore = 0;

        for (final InputFrameDTO frame : inputLog) {
            final InputState current = InputState.from(frame);
            if (current.isNeutral()) {
                strippedNeutralFrames++;
                if (currentRunState != null) {
                    neutralFramesAfterCurrentRun++;
                }
                continue;
            }

            if (currentRunState == null) {
                currentRunState = current;
                currentRunStartFrame = frame.frame();
                currentRunLength = 1;
                neutralFramesAfterCurrentRun = 0;
                currentRunStrippedFramesBefore = strippedNeutralFrames;
                continue;
            }

            if (neutralFramesAfterCurrentRun > 0) {
                runs.add(new InputRun(
                        currentRunState,
                        currentRunStartFrame,
                        currentRunLength,
                        neutralFramesAfterCurrentRun,
                        currentRunStrippedFramesBefore));
                currentRunState = current;
                currentRunStartFrame = frame.frame();
                currentRunLength = 1;
                neutralFramesAfterCurrentRun = 0;
                currentRunStrippedFramesBefore = strippedNeutralFrames;
                continue;
            }

            if (current.equals(currentRunState)) {
                currentRunLength++;
            } else {
                runs.add(new InputRun(
                        currentRunState,
                        currentRunStartFrame,
                        currentRunLength,
                        0,
                        currentRunStrippedFramesBefore));
                currentRunState = current;
                currentRunStartFrame = frame.frame();
                currentRunLength = 1;
                currentRunStrippedFramesBefore = strippedNeutralFrames;
            }
        }

        if (currentRunState != null) {
            runs.add(new InputRun(
                    currentRunState,
                    currentRunStartFrame,
                    currentRunLength,
                    neutralFramesAfterCurrentRun,
                    currentRunStrippedFramesBefore));
        }

        return List.copyOf(runs);
    }

    private static List<InputRun> removeFakeJumpOnlyRuns(final List<InputRun> runs) {
        final ArrayList<InputRun> normalized = new ArrayList<>();
        int strippedFakeJumpFrames = 0;

        for (final InputRun run : runs) {
            if (run.state().isJumpOnly()
                    && run.neutralFramesAfter() >= JUMP_ONLY_HORIZONTAL_DELTA_FRAMES) {
                strippedFakeJumpFrames += run.length();
                continue;
            }
            normalized.add(run
                    .withAdditionalStrippedFrames(strippedFakeJumpFrames)
                    .withoutNeutralFramesAfter());
        }

        return List.copyOf(normalized);
    }

    private static List<InputRun> removeHorizontalCancellationNoise(final List<InputRun> runs) {
        final ArrayList<InputRun> normalized = new ArrayList<>();
        int strippedHorizontalNoiseFrames = 0;

        for (final InputRun run : runs) {
            final InputRun current = run.withAdditionalStrippedFrames(strippedHorizontalNoiseFrames);
            final int lastIndex = normalized.size() - 1;
            if (lastIndex >= 1
                    && isCancelableHorizontalInterruption(
                    normalized.get(lastIndex - 1),
                    normalized.get(lastIndex),
                    current)) {
                final InputRun before = normalized.get(lastIndex - 1);
                final InputRun interruption = normalized.remove(lastIndex);
                if (before.state().equals(current.state())) {
                    final int returnFramesToStrip = Math.min(run.length(), interruption.length());
                    strippedHorizontalNoiseFrames += interruption.length() + returnFramesToStrip;
                    if (run.length() > returnFramesToStrip) {
                        addOrMergeAdjacentEqualRun(
                                normalized,
                                run.withoutLeadingFrames(returnFramesToStrip)
                                        .withAdditionalStrippedFrames(strippedHorizontalNoiseFrames));
                    }
                } else {
                    strippedHorizontalNoiseFrames += interruption.length();
                    addOrMergeAdjacentEqualRun(
                            normalized,
                            run.withAdditionalStrippedFrames(strippedHorizontalNoiseFrames));
                }
                continue;
            }
            addOrMergeAdjacentEqualRun(normalized, current);
        }

        return List.copyOf(normalized);
    }

    private static void addOrMergeAdjacentEqualRun(final ArrayList<InputRun> runs, final InputRun run) {
        final int lastIndex = runs.size() - 1;
        if (lastIndex >= 0 && runs.get(lastIndex).state().equals(run.state())) {
            runs.set(lastIndex, mergeRuns(runs.get(lastIndex), run));
        } else {
            runs.add(run);
        }
    }

    private static boolean isCancelableHorizontalInterruption(final InputRun before,
                                                             final InputRun interruption,
                                                             final InputRun after) {
        return interruption.length() <= MAX_CANCELABLE_HORIZONTAL_RUN_FRAMES
                && sameHorizontalDirection(before.state(), after.state())
                && oppositeHorizontalDirection(before.state(), interruption.state());
    }

    private static boolean sameHorizontalDirection(final InputState first, final InputState second) {
        return hasSingleHorizontalDirection(first)
                && hasSingleHorizontalDirection(second)
                && first.left() == second.left()
                && first.right() == second.right();
    }

    private static boolean oppositeHorizontalDirection(final InputState first, final InputState second) {
        return hasSingleHorizontalDirection(first)
                && hasSingleHorizontalDirection(second)
                && first.left() == second.right()
                && first.right() == second.left();
    }

    private static boolean hasSingleHorizontalDirection(final InputState state) {
        return state.left() != state.right();
    }

    private static InputRun mergeRuns(final InputRun first, final InputRun second) {
        return new InputRun(
                first.state(),
                first.startFrame(),
                first.length() + second.length(),
                second.neutralFramesAfter(),
                first.strippedFramesBefore());
    }

    private static List<InputRun> collapseAdjacentEqualRuns(final List<InputRun> runs) {
        final ArrayList<InputRun> collapsed = new ArrayList<>();

        for (final InputRun run : runs) {
            final int lastIndex = collapsed.size() - 1;
            if (lastIndex >= 0 && collapsed.get(lastIndex).state().equals(run.state())) {
                collapsed.set(lastIndex, mergeRuns(collapsed.get(lastIndex), run));
            } else {
                collapsed.add(run);
            }
        }

        return List.copyOf(collapsed);
    }

    /// Extracts non-neutral input changes from the provided input log.
    /// @param inputLog the ordered input frames recorded during an attempt
    /// @return the ordered list of non-neutral state transitions
    public List<InputChange> nonNeutralInputChanges(final List<InputFrameDTO> inputLog) {
        InputState previous = null;
        final ArrayList<InputChange> changes = new ArrayList<>();

        for (final InputFrameDTO frame : inputLog) {
            final InputState current = InputState.from(frame);
            if (current.isNeutral()) {
                continue;
            }
            if (!current.equals(previous)) {
                changes.add(new InputChange(frame.frame(), current.canonical()));
                previous = current;
            }
        }

        return List.copyOf(changes);
    }

    private static int bucket(final int frame, final int bucketSize, final int offset) {
        return Math.floorDiv(frame + offset, bucketSize);
    }

    private static void appendSeparatorIfNeeded(final StringBuilder builder) {
        if (!builder.isEmpty()) {
            builder.append('|');
        }
    }

    private static void appendBucketInputIfNeeded(final StringBuilder canonical,
                                                  final int bucket,
                                                  final BucketInputState input) {
        if (input.isNeutral()) {
            return;
        }
        appendSeparatorIfNeeded(canonical);
        canonical.append(bucket)
                .append(':')
                .append(input.canonical());
    }

    private static int bit(final boolean value) {
        return value ? 1 : 0;
    }

    private static String sha256(final String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /// Non-neutral input transition used by fuzzy duplicate matching.
    public record InputChange(
            /// Frame number where the non-neutral state was observed.
            int frame,
            /// Canonical representation of the observed input state.
            String state) {
    }

    /// Consecutive run of equal non-neutral input states.
    private record InputRun(
            /// Input state repeated by this run.
            InputState state,
            /// First frame belonging to this run.
            int startFrame,
            /// Number of non-neutral frames in this run.
            int length,
            /// Neutral frames seen immediately after this run.
            int neutralFramesAfter,
            /// Frames stripped before this run during jitter normalization.
            int strippedFramesBefore) {

        private int normalizedCount() {
            return startFrame - strippedFramesBefore;
        }

        private InputRun withoutNeutralFramesAfter() {
            return new InputRun(state, startFrame, length, 0, strippedFramesBefore);
        }

        private InputRun withAdditionalStrippedFrames(final int additionalStrippedFrames) {
            return new InputRun(
                    state,
                    startFrame,
                    length,
                    neutralFramesAfter,
                    strippedFramesBefore + additionalStrippedFrames);
        }

        private InputRun withoutLeadingFrames(final int leadingFrames) {
            return new InputRun(
                    state,
                    startFrame + leadingFrames,
                    length - leadingFrames,
                    neutralFramesAfter,
                    strippedFramesBefore);
        }
    }

    /// Canonical jitter-normalized sequence and its change count.
    private record JitterCanonical(String canonical, int changeCount) {
    }

    /// Aggregates all inputs observed inside one frame bucket.
    private static final class BucketInputState {
        /// Whether a left input occurred in the bucket.
        private boolean left;
        /// Whether a right input occurred in the bucket.
        private boolean right;
        /// Whether a jump input occurred in the bucket.
        private boolean jump;
        /// Whether a run input occurred in the bucket.
        private boolean run;

        private void include(final InputState input) {
            this.left |= input.left();
            this.right |= input.right();
            this.jump |= input.jump();
            this.run |= input.run();
        }

        private boolean isNeutral() {
            return !left && !right && !jump && !run;
        }

        private String canonical() {
            return "L" + bit(left)
                    + "R" + bit(right)
                    + "J" + bit(jump)
                    + "S" + bit(run);
        }
    }

    /// Canonical state of the player controls on a single frame.
    private record InputState(boolean left, boolean right, boolean jump, boolean run) {
        private static InputState from(final InputFrameDTO frame) {
            return new InputState(frame.left(), frame.right(), frame.jump(), frame.run());
        }

        private String canonical() {
            return "L" + bit(left)
                    + "R" + bit(right)
                    + "J" + bit(jump)
                    + "S" + bit(run);
        }

        private boolean isNeutral() {
            return !left && !right && !jump && !run;
        }

        private boolean isJumpOnly() {
            return !left && !right && jump && !run;
        }

    }
}
