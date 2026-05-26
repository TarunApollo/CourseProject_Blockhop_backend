package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings(
    value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" },
    justification = "Transient response DTO; input log is intentionally passed through")
public record GhostDTO(
    String attemptId,
    List<InputFrameDTO> inputLog,
    long timeTakenMs,
    String holderName
) {}
