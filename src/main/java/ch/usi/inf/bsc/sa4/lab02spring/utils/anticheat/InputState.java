package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

/// Button state for one frame.
record InputState(boolean left, boolean right, boolean jump, boolean run) {

    /* package */ static InputState from(final InputFrameDTO frame) {
        return new InputState(frame.left(), frame.right(), frame.jump(), frame.run());
    }

    /* package */ String canonical() {
        return "L" + bit(left)
                + "R" + bit(right)
                + "J" + bit(jump)
                + "S" + bit(run);
    }

    /* package */ boolean isNeutral() {
        return !left && !right && !jump && !run;
    }

    /* package */ boolean isJumpOnly() {
        return !left && !right && jump && !run;
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
