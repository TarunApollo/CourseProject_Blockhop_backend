package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

public record InputFrameDTO(
        int frame,
        boolean left,
        boolean right,
        boolean jump,
        boolean run,
        boolean climbUp,
        boolean climbDown,
        boolean climbExit,
        boolean pickupAndThrow) {
    /// input DTO sent by the frontend
    public InputFrameDTO(final int frame,
            final boolean left,
            final boolean right,
            final boolean jump,
            final boolean run) {
        this(frame, left, right, jump, run, false, false, false, false);
    }
}
