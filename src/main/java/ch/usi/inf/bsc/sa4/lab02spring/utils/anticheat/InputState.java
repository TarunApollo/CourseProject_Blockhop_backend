package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

/// Button state for one frame.
record InputState(
        boolean left,
        boolean right,
        boolean jump,
        boolean run,
        boolean climbUp,
        boolean climbDown,
        boolean climbExit,
        boolean pickupAndThrow) {

    /* package */ InputState(final boolean left, final boolean right, final boolean jump, final boolean run) {
        this(left, right, jump, run, false, false, false, false);
    }

    /* package */ static InputState from(final InputFrameDTO frame) {
        return new InputState(
                frame.left(),
                frame.right(),
                frame.jump(),
                frame.run(),
                frame.climbUp(),
                frame.climbDown(),
                frame.climbExit(),
                frame.pickupAndThrow());
    }

    /* package */ String canonical() {
        return "L" + bit(left)
                + "R" + bit(right)
                + "J" + bit(jump)
                + "S" + bit(run)
                + "U" + bit(climbUp)
                + "D" + bit(climbDown)
                + "X" + bit(climbExit)
                + "P" + bit(pickupAndThrow);
    }

    /* package */ boolean isNeutral() {
        return !left && !right && !jump && !run
                && !climbUp && !climbDown && !climbExit && !pickupAndThrow;
    }

    /* package */ boolean isJumpOnly() {
        return !left && !right && jump && !run
                && !climbUp && !climbDown && !climbExit && !pickupAndThrow;
    }

    /* package */ boolean sameHorizontalDirection(final InputState other) {
        return hasSingleHorizontalDirection()
                && other.hasSingleHorizontalDirection()
                && left == other.left()
                && right == other.right();
    }

    /* package */ boolean oppositeHorizontalDirection(final InputState other) {
        return hasSingleHorizontalDirection()
                && other.hasSingleHorizontalDirection()
                && left == other.right()
                && right == other.left();
    }

    private boolean hasSingleHorizontalDirection() {
        return left != right;
    }

    private static int bit(final boolean value) {
        return value ? 1 : 0;
    }
}
