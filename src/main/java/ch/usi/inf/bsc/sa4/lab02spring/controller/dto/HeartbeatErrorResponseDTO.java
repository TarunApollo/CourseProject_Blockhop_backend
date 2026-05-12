package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HeartbeatErrorResponseDTO(
    String error,
    Integer expectedFrame,
    Integer receivedFrame,
    Long maxDelayMs,
    Long recivedDelayMs
){};