package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

public record InputFrameDTO(
        int frame,
        boolean left,
        boolean right,
        boolean jump,
        boolean run
) {
}
