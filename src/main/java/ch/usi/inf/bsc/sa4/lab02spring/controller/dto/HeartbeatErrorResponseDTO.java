package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import org.jspecify.annotations.Nullable;

public record HeartbeatErrorResponseDTO(
    String error,
    @Nullable Integer expectedFrame,
    @Nullable Integer receivedFrame,
    @Nullable Long maxDelayMs,
    @Nullable Long recivedDelayMs
){};
