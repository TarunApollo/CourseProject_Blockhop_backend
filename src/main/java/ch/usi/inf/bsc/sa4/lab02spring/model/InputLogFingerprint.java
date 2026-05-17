package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.List;

public record InputLogFingerprint(
        String exactHash,
        List<String> changeBucketHashes,
        int inputFrameCount,
        int inputChangeCount) {

    public static InputLogFingerprint empty(){
        return new InputLogFingerprint("", List.of(), -1, -1);
    }
}