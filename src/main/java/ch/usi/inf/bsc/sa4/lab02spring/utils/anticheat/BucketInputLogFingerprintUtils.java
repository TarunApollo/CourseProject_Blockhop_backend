package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

import java.util.List;

/// Builds bucket hashes for fuzzy matching.
final class BucketInputLogFingerprintUtils {

    /// Number of frames in one bucket.
    private static final int DEF_BUCKET_SIZE = 10;
    /// Bucket shifts used to avoid edge misses.
    private static final List<Integer> DEF_BUCKET_OFFS = List.of(0, 5);

    /// Do not create this helper.
    private BucketInputLogFingerprintUtils() {
    }

    static List<String> hashes(final List<InputFrameDTO> inputLog) {
        return DEF_BUCKET_OFFS.stream()
                .map(offset -> canonicalBucketedCombinedInputs(inputLog, DEF_BUCKET_SIZE, offset))
                .map(InputLogFingerprintUtils::sha256)
                .toList();
    }

    /// Groups frames and stores which buttons were pressed in each bucket.
    ///
    /// @param inputLog input frames from the attempt, in order
    /// @param bucketSize number of frames in each bucket
    /// @param offset frame shift used before placing frames in buckets
    /// @return text used for the bucket hash
    private static String canonicalBucketedCombinedInputs(final List<InputFrameDTO> inputLog,
                                                          final int bucketSize,
                                                          final int offset) {
        if (bucketSize <= 0) {
            throw new IllegalArgumentException("bucketSize must be positive");
        }

        final StringBuilder canonical = new StringBuilder();
        boolean hasCurrentBucket = false;
        int currentBucket = 0;
        BucketInputState bucketInput = new BucketInputState();

        for (final InputFrameDTO frame : inputLog) {
            final int bucket = bucket(frame.frame(), bucketSize, offset);
            if (!hasCurrentBucket) {
                currentBucket = bucket;
                hasCurrentBucket = true;
            }
            if (bucket != currentBucket) {
                appendBucketInputIfNeeded(canonical, currentBucket, bucketInput);
                currentBucket = bucket;
                bucketInput.reset();
            }
            bucketInput.include(InputState.from(frame));
        }

        if (hasCurrentBucket) {
            appendBucketInputIfNeeded(canonical, currentBucket, bucketInput);
        }
        return canonical.toString();
    }

    private static int bucket(final int frame, final int bucketSize, final int offset) {
        return Math.floorDiv(frame + offset, bucketSize);
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

    private static void appendSeparatorIfNeeded(final StringBuilder builder) {
        if (!builder.isEmpty()) {
            builder.append('|');
        }
    }

    /// Buttons seen inside one bucket.
    /// left: true if left was pressed.
    /// right: true if right was pressed.
    /// jump: true if jump was pressed.
    /// run: true if run was pressed.
    private static final class BucketInputState {
        private boolean left;
        private boolean right;
        private boolean jump;
        private boolean run;

        private void include(final InputState state) {
            left |= state.left();
            right |= state.right();
            jump |= state.jump();
            run |= state.run();
        }

        private void reset() {
            left = false;
            right = false;
            jump = false;
            run = false;
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

        private static int bit(final boolean value) {
            return value ? 1 : 0;
        }
    }
}
