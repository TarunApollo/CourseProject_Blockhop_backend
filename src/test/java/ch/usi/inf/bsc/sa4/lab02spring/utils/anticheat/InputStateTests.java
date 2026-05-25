package ch.usi.inf.bsc.sa4.lab02spring.utils.anticheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.InputFrameDTO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/// Tests for [InputState].
@DisplayName("The Input State")
@SuppressWarnings("java:S2187")
class InputStateTests {

    /// Tests for creating an input state.
    @Nested
    @DisplayName("when built from a frame")
    class FromFrame {

        /// Checks that all button values are copied.
        @Test
        @DisplayName("copies button values from the frame")
        void copiesButtonValues() {
            final InputFrameDTO frame = new InputFrameDTO(7, true, false, true, true, true, false, true, true);

            final InputState state = InputState.from(frame);

            Assertions.assertTrue(state.left());
            Assertions.assertFalse(state.right());
            Assertions.assertTrue(state.jump());
            Assertions.assertTrue(state.run());
            Assertions.assertTrue(state.climbUp());
            Assertions.assertFalse(state.climbDown());
            Assertions.assertTrue(state.climbExit());
            Assertions.assertTrue(state.pickupAndThrow());
        }
    }

    /// Tests for text output.
    @Nested
    @DisplayName("when writing canonical text")
    class CanonicalText {

        /// Checks the fixed button order.
        @Test
        @DisplayName("uses left right jump and run order")
        void usesFixedButtonOrder() {
            final InputState state = new InputState(true, false, true, false, true, false, true, false);

            Assertions.assertEquals("L1R0J1S0U1D0X1P0", state.canonical());
        }
    }

    /// Tests for input state checks.
    @Nested
    @DisplayName("when checking input type")
    class Checks {

        /// Checks neutral input.
        @Test
        @DisplayName("detects neutral input")
        void detectsNeutralInput() {
            Assertions.assertTrue(new InputState(false, false, false, false).isNeutral());
            Assertions.assertFalse(new InputState(false, true, false, false).isNeutral());
            Assertions.assertFalse(
                    new InputState(false, false, false, false, false, false, false, true).isNeutral());
        }

        /// Checks jump-only input.
        @Test
        @DisplayName("detects jump only input")
        void detectsJumpOnlyInput() {
            Assertions.assertTrue(new InputState(false, false, true, false).isJumpOnly());
            Assertions.assertFalse(new InputState(false, false, true, true).isJumpOnly());
            Assertions.assertFalse(
                    new InputState(false, false, true, false, true, false, false, false).isJumpOnly());
        }

        /// Checks matching horizontal direction.
        @Test
        @DisplayName("detects the same horizontal direction")
        void detectsSameHorizontalDirection() {
            final InputState left = new InputState(true, false, false, false);
            final InputState leftJump = new InputState(true, false, true, false);
            final InputState right = new InputState(false, true, false, false);
            final InputState neutral = new InputState(false, false, false, false);
            final InputState bothDirections = new InputState(true, true, false, false);

            Assertions.assertTrue(left.sameHorizontalDirection(leftJump));
            Assertions.assertFalse(left.sameHorizontalDirection(right));
            Assertions.assertFalse(left.sameHorizontalDirection(bothDirections));
            Assertions.assertFalse(neutral.sameHorizontalDirection(left));
        }

        /// Checks opposite horizontal direction.
        @Test
        @DisplayName("detects the opposite horizontal direction")
        void detectsOppositeHorizontalDirection() {
            final InputState left = new InputState(true, false, false, false);
            final InputState right = new InputState(false, true, false, false);
            final InputState neutral = new InputState(false, false, false, false);

            Assertions.assertTrue(left.oppositeHorizontalDirection(right));
            Assertions.assertFalse(left.oppositeHorizontalDirection(left));
            Assertions.assertFalse(left.oppositeHorizontalDirection(neutral));
            Assertions.assertFalse(neutral.oppositeHorizontalDirection(right));
        }
    }
}
