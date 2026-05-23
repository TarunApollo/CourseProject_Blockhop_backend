package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.List;

public record GhostDTO(
    String attemptId,
    List<InputFrameDTO> inputLog,
    long timeTakenMs,
    String holderName
) {}
