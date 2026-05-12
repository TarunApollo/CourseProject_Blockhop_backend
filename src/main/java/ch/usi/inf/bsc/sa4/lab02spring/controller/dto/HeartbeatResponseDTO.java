package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat.ViolationCode;

import java.util.List;

/// Result of validating one heartbeat frame
public record HeartbeatResponseDTO(
    int frame,
    List<ViolationCode> violations
) {}
