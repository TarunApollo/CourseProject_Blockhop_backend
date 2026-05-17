package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.InputLogFingerprint;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
public class InputLogFingerprintService {

    private static final int DEFAULT_BUCKET_SIZE = 6;
    private static final List<Integer> DEFAULT_BUCKET_OFFSETS = List.of(0, 3);

    public InputLogFingerprint fingerprint(final List<InputFrameDTO> inputLog) {
        final List<InputChange> changes = inputChanges(inputLog);
        final String exactCanonical = canonicalExact(inputLog);
        final List<String> changeBucketHashes = DEFAULT_BUCKET_OFFSETS.stream()
                .map(offset -> canonicalBucketedChanges(changes, DEFAULT_BUCKET_SIZE, offset))
                .map(InputLogFingerprintService::sha256)
                .toList();

        return new InputLogFingerprint(
                sha256(exactCanonical),
                changeBucketHashes,
                inputLog.size(),
                changes.size()
        );
    }

    public String exactHash(final List<InputFrameDTO> inputLog) {
        return fingerprint(inputLog).exactHash();
    }

    public List<String> changeBucketHashes(final List<InputFrameDTO> inputLog) {
        return fingerprint(inputLog).changeBucketHashes();
    }

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

    public String canonicalBucketedChanges(final List<InputChange> changes,
                                           final int bucketSize,
                                           final int offset) {
        if (bucketSize <= 0) {
            throw new IllegalArgumentException("bucketSize must be positive");
        }

        final StringBuilder canonical = new StringBuilder();
        for (final InputChange change : changes) {
            appendSeparatorIfNeeded(canonical);
            canonical.append(bucket(change.frame(), bucketSize, offset))
                    .append(':')
                    .append(change.state());
        }
        return canonical.toString();
    }

    public List<InputChange> inputChanges(final List<InputFrameDTO> inputLog) {
        InputState previous = null;
        final java.util.ArrayList<InputChange> changes = new java.util.ArrayList<>();

        for (final InputFrameDTO frame : inputLog) {
            final InputState current = InputState.from(frame);
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

    private static String sha256(final String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public record InputChange(int frame, String state) {
    }

    private record InputState(boolean left, boolean right, boolean jump, boolean run) {
        static InputState from(final InputFrameDTO frame) {
            return new InputState(frame.left(), frame.right(), frame.jump(), frame.run());
        }

        String canonical() {
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
