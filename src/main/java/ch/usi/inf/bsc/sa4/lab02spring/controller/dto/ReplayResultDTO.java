package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.model.AttemptVerificationStatus;

public record ReplayResultDTO(
        boolean valid,
        String reason,
        int frames,
        AttemptVerificationStatus antiCheatStatus) {

    public ReplayResultDTO(final boolean valid, final String reason, final int frames) {
        this(valid, reason, frames, AttemptVerificationStatus.NOT_VERIFIED);
    }
}
