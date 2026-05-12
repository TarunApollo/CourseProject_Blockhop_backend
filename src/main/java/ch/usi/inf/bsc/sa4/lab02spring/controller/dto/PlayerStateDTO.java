package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

/// Player snapshot sent in each heartbeat
public record PlayerStateDTO(
    double x,
    double y
) {}
