package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

/// Heartbeat payload sent each frame.
/// Gravity is not sent: the server computes freefall acceleration
/// from consecutive Y positions instead of trusting the client.
public record HeartbeatDTO(
    int frame,
    PlayerStateDTO player
) {}
