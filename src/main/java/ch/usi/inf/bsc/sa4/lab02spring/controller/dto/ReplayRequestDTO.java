package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.List;

public record ReplayRequestDTO(
        String levelId,
        String attemptId,
        int totalFrames,
        List<InputFrameDTO> inputLog
) {
}
