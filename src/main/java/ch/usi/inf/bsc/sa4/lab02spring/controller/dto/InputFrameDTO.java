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
    /// Creates an input frame with the legacy movement fields.
    ///
    /// @param frame input frame index
    /// @param left whether left is pressed
    /// @param right whether right is pressed
    /// @param jump whether jump is pressed
    /// @param run whether run is pressed
    public InputFrameDTO(final int frame,
            final boolean left,
            final boolean right,
            final boolean jump,
            final boolean run) {
        this(frame, left, right, jump, run, false, false, false, false);
    }
}
