package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.List;

/// Fingerprint data derived from a submitted replay input log.
///
/// @param exactHash hash of the exact input sequence
/// @param jitterInputHash hash of the jitter input sequence
/// @param jitterInputChangeCount changes in the jitter sequence
/// @param changeBucketHashes bucketed hashes used for fuzzy matching
/// @param inputFrameCount number of frames in the input log
/// @param inputChangeCount number of non-neutral input changes
public record InputLogFingerprint(
        String exactHash,
        String jitterInputHash,
        int jitterInputChangeCount,
        List<String> changeBucketHashes,
        int inputFrameCount,
        int inputChangeCount) {

    /// Creates an empty fingerprint for attempts that have not been verified yet.
    ///
    /// @return an empty fingerprint with sentinel counts
    public static InputLogFingerprint empty(){
        return new InputLogFingerprint("", "", -1, List.of(), -1, -1);
    }
}
