package ch.usi.inf.bsc.sa4.lab02spring.utils;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/// Creates stable hashes and canonical input sequences for anti-cheat checks.
// Methods are used only for calculating fingerprint.
// They are mostly helpers to split tasks into multiple function.
@SuppressWarnings("PMD.TooManyMethods")
public final class InputLogFingerprintUtils {

    /// Default number of frames grouped into one fuzzy input bucket.
    private static final int DEF_BUCKET_SIZE = 10;
    /// Bucket offsets used to produce fuzzy hashes with shifted boundaries.
    private static final List<Integer> DEF_BUCKET_OFFS = List.of(0, 5);
    /// Neutral-frame gap that identifies jump-only runs likely caused by jitter.
    private static final int JUMP_X_GAP_FRAMES = 90;
    /// Maximum opposite-direction run length treated as horizontal input noise.
    private static final int MAX_CANCEL_X_RUN = 5;

    /// Prevents construction of this utility class.
    private InputLogFingerprintUtils() {
    }

    /// Builds all anti-cheat fingerprints for the provided input log.
    /// @param inputLog the ordered input frames recorded during an attempt
    /// @return exact, jitter-normalized, and bucketed fingerprints
    public static InputLogFingerprint fingerprint(final List<InputFrameDTO> inputLog) {
        final List<InputChange> changes = nonNeutralInputChanges(inputLog);
        final String exactCanonical = canonicalExact(inputLog);
        final JitterCanonical jitterCanonical = jitterCanonical(inputLog);
        final List<String> bucketHashes = DEF_BUCKET_OFFS.stream()
                .map(offset -> canonicalBucketedCombinedInputs(inputLog, DEF_BUCKET_SIZE, offset))
                .map(InputLogFingerprintUtils::sha256)
                .toList();

        return new InputLogFingerprint(
                sha256(exactCanonical),
                sha256(jitterCanonical.canonical()),
                jitterCanonical.changeCount(),
                bucketHashes,
                inputLog.size(),
                changes.size()
        );
    }

    /// Converts each frame into the exact canonical input representation.
    /// @param inputLog the ordered input frames recorded during an attempt
    /// @return a frame-by-frame canonical string
    private static String canonicalExact(final List<InputFrameDTO> inputLog) {
        final StringBuilder canonical = new StringBuilder();
        for (final InputFrameDTO frame : inputLog) {
            appendSeparatorIfNeeded(canonical);
            canonical.append(frame.frame())
                    .append(':')
                    .append(canonical(from(frame)));
        }
        return canonical.toString();
    }

    private static JitterCanonical jitterCanonical(final List<InputFrameDTO> inputLog) {
        final List<InputRun> runs = jitterInputRuns(inputLog);
        final List<InputRun> withoutFakeJumps = removeFakeJumpOnlyRuns(runs);
        final List<InputRun> withoutXNoise = removeHorizontalCancellationNoise(withoutFakeJumps);
        final List<InputRun> normalizedRuns = collapseAdjacentEqualRuns(withoutXNoise);
        final StringBuilder canonical = new StringBuilder();

        for (final InputRun run : normalizedRuns) {
            appendSeparatorIfNeeded(canonical);
            canonical.append(normalizedCount(run))
                    .append(':')
                    .append(canonical(run.state()));
        }

        return new JitterCanonical(canonical.toString(), normalizedRuns.size());
    }

    /// Groups input frames into buckets and records combined inputs per bucket.
    /// @param inputLog the ordered input frames recorded during an attempt
    /// @param bucketSize the number of frames included in each bucket
    /// @param offset the frame offset applied before bucket assignment
    /// @return the canonical fuzzy bucket representation
    private static String canonicalBucketedCombinedInputs(final List<InputFrameDTO> inputLog,
                                                          final int bucketSize,
                                                          final int offset) {
        if (bucketSize <= 0) {
            throw new IllegalArgumentException("bucketSize must be positive");
        }

        final StringBuilder canonical = new StringBuilder();
        Integer currentBucket = null;
        BucketInputState bucketInput = new BucketInputState();

        for (final InputFrameDTO frame : inputLog) {
            final int bucket = bucket(frame.frame(), bucketSize, offset);
            if (currentBucket == null) {
                currentBucket = bucket;
            } else if (bucket != currentBucket) {
                appendBucketInputIfNeeded(canonical, currentBucket, bucketInput);
                currentBucket = bucket;
                bucketInput = new BucketInputState();
            }
            include(bucketInput, from(frame));
        }

        if (currentBucket != null) {
            appendBucketInputIfNeeded(canonical, currentBucket, bucketInput);
        }
        return canonical.toString();
    }

    private static List<InputRun> jitterInputRuns(final List<InputFrameDTO> inputLog) {
        final ArrayList<InputRun> runs = new ArrayList<>();
        InputState currentRunState = null;
        int runStartFrame = 0;
        int currentRunLength = 0;
        int neutralAfterRun = 0;
        int strippedNeutral = 0;
        int strippedBeforeRun = 0;

        for (final InputFrameDTO frame : inputLog) {
            final InputState current = from(frame);
            if (isNeutral(current)) {
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
        final ArrayList<InputRun> normalized = new ArrayList<>();
        int fakeJumpFrames = 0;

        for (final InputRun run : runs) {
            if (isJumpOnly(run.state())
                    && run.neutralAfter() >= JUMP_X_GAP_FRAMES) {
                fakeJumpFrames += run.length();
                continue;
            }
            normalized.add(withoutNeutralFramesAfter(withAdditionalStrippedFrames(run, fakeJumpFrames)));
        }

        return List.copyOf(normalized);
    }

    private static List<InputRun> removeHorizontalCancellationNoise(final List<InputRun> runs) {
        final ArrayList<InputRun> normalized = new ArrayList<>();
        int noiseFrames = 0;

        for (final InputRun run : runs) {
            final InputRun current = withAdditionalStrippedFrames(run, noiseFrames);
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
                                withAdditionalStrippedFrames(
                                        withoutLeadingFrames(run, returnStripFrames),
                                        noiseFrames));
                    }
                } else {
                    noiseFrames += interruption.length();
                    addOrMergeAdjacentEqualRun(
                            normalized,
                            withAdditionalStrippedFrames(run, noiseFrames));
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
        return interruption.length() <= MAX_CANCEL_X_RUN
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

    private static int normalizedCount(final InputRun run) {
        return run.startFrame() - run.strippedBefore();
    }

    private static InputRun withoutNeutralFramesAfter(final InputRun run) {
        return new InputRun(run.state(), run.startFrame(), run.length(), 0, run.strippedBefore());
    }

    private static InputRun withAdditionalStrippedFrames(final InputRun run, final int extraStripped) {
        return new InputRun(
                run.state(),
                run.startFrame(),
                run.length(),
                run.neutralAfter(),
                run.strippedBefore() + extraStripped);
    }

    private static InputRun withoutLeadingFrames(final InputRun run, final int leadingFrames) {
        return new InputRun(
                run.state(),
                run.startFrame() + leadingFrames,
                run.length() - leadingFrames,
                run.neutralAfter(),
                run.strippedBefore());
    }

    private static InputRun mergeRuns(final InputRun first, final InputRun second) {
        return new InputRun(
                first.state(),
                first.startFrame(),
                first.length() + second.length(),
                second.neutralAfter(),
                first.strippedBefore());
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
    private static List<InputChange> nonNeutralInputChanges(final List<InputFrameDTO> inputLog) {
        InputState previous = null;
        final ArrayList<InputChange> changes = new ArrayList<>();

        for (final InputFrameDTO frame : inputLog) {
            final InputState current = from(frame);
            if (isNeutral(current)) {
                continue;
            }
            if (!current.equals(previous)) {
                changes.add(new InputChange(frame.frame(), canonical(current)));
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
        if (isNeutral(input)) {
            return;
        }
        appendSeparatorIfNeeded(canonical);
        canonical.append(bucket)
                .append(':')
                .append(canonical(input));
    }

    private static void include(final BucketInputState input, final InputState state) {
        input.left |= state.left();
        input.right |= state.right();
        input.jump |= state.jump();
        input.run |= state.run();
    }

    private static boolean isNeutral(final BucketInputState input) {
        return !input.left && !input.right && !input.jump && !input.run;
    }

    private static String canonical(final BucketInputState input) {
        return "L" + bit(input.left)
                + "R" + bit(input.right)
                + "J" + bit(input.jump)
                + "S" + bit(input.run);
    }

    private static InputState from(final InputFrameDTO frame) {
        return new InputState(frame.left(), frame.right(), frame.jump(), frame.run());
    }

    private static String canonical(final InputState state) {
        return "L" + bit(state.left())
                + "R" + bit(state.right())
                + "J" + bit(state.jump())
                + "S" + bit(state.run());
    }

    private static boolean isNeutral(final InputState state) {
        return !state.left() && !state.right() && !state.jump() && !state.run();
    }

    private static boolean isJumpOnly(final InputState state) {
        return !state.left() && !state.right() && state.jump() && !state.run();
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
    /// frame: Frame number where the non-neutral state was observed.
    /// state: Canonical representation of the observed input state.
    private record InputChange(
            int frame,
            String state) {
    }

    /// Consecutive run of equal non-neutral input states.
    /// state: Input state repeated by this run.
    /// startFrame: First frame belonging to this run.
    /// length: Number of non-neutral frames in this run.
    /// neutralAfter: Neutral frames seen immediately after this run.
    /// strippedBefore: Frames stripped before this run during jitter normalization.
    private record InputRun(
            InputState state,
            int startFrame,
            int length,
            int neutralAfter,
            int strippedBefore) {
    }

    /// Canonical jitter-normalized sequence and its change count.
    private record JitterCanonical(String canonical, int changeCount) {
    }

    /// Aggregates all inputs observed inside one frame bucket.
    /// left: Whether a left input occurred in the bucket.
    /// right: Whether a right input occurred in the bucket.
    /// jump: Whether a jump input occurred in the bucket.
    /// run: Whether a run input occurred in the bucket.
    private static final class BucketInputState {
        private boolean left;
        private boolean right;
        private boolean jump;
        private boolean run;
    }

    /// Canonical state of the player controls on a single frame.
    private record InputState(boolean left, boolean right, boolean jump, boolean run) {
    }
}
