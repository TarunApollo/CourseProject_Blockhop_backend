package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

/// Heartbeat payload sent each frame.
public record HeartbeatDTO(
    int frame,
    PlayerStateDTO player,
    double gravity
) {}
