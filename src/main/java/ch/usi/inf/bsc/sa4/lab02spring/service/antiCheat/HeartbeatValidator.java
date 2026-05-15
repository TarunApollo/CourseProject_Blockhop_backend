package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PlayerStateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class HeartbeatValidator {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatValidator.class);

    /// Mirrors frontend mechanics/constants.js GRAVITY.
    private static final double EXPECTED_GRAVITY = 2.5;

    // TODO: Tighten these tolerances through play testing.
    private static final double MAX_DX = 25;
    private static final double MAX_DY = 42.5;
    /// Mirrors the Tiled/Phaser tile size used by the level player.
    private static final double TILE_SIZE = 128;
    /// Mirrors player.setDisplaySize(128, 128) in the frontend.
    private static final double PLAYER_HALF_HEIGHT = 64;
    /// Mirrors the frontend foot-probe width used for ground detection.
    private static final double FOOT_SENSOR_HALF_WIDTH = 32;
    /// Mirrors frontend feetY = player.y + player.displayHeight / 2 + 4.
    private static final double FOOT_SENSOR_MARGIN = 4;
    private static final double MAX_STABLE_UNSUPPORTED_DY = 0.5;
    private static final double MIN_DIRECTION_CHANGE_DY = 0.5;
    // TODO: 8 frames means a cheater can fly in 7-frame bursts.
    // So we need to tighten here too.
    private static final int FLYING_FRAME_THRESHOLD = 8;
    private static final int UNSUPPORTED_DIRECTION_CHANGE_THRESHOLD = 2;

    /// Unsupported descending frames required before acceleration checks.
    /// This avoids landing and edge snap noise.
    private static final int FREEFALL_ACCEL_THRESHOLD = 3;
    private static final int GRAVITY_MISMATCH_FRAME_THRESHOLD = 2;

    private static final double EPSILON = 0.001;

    /// Tolerance for measured acceleration below expected gravity.
    /// Play-testing showed early-fall deltas up to about 1.17 below expected.
    private static final double GRAVITY_TOLERANCE = 1.5;

    /// Maximum falling speed in pixels/frame.
    /// Near this cap, Y deltas flatten and acceleration is no longer reliable.
    /// Use a margin because terminal deltas can land between 17 and 18px.
    /// Mirrors frontend mechanics/constants.js MAX_FALL_VY.
    private static final double MAX_FALL_VY = 18;
    private static final double NEAR_TERMINAL_MARGIN = 2.0;

    public HeartbeatResponseDTO validate(final HeartbeatDTO payload,
                                         final AntiCheatSessionState state) {
        final List<ViolationCode> violations = new ArrayList<>();

        checkFreefallAcceleration(payload, state, violations);
        checkDisplacement(payload, state, violations);
        checkFlying(payload, state, violations);

        state.shiftSnapshotHistory(payload.player());

        return new HeartbeatResponseDTO(payload.frame(), violations);
    }

    /// Checks frontend-style freefall acceleration from three Y positions.
    /// Formula: a = y[n] - 2*y[n-1] + y[n-2].
    private void checkFreefallAcceleration(final HeartbeatDTO payload,
                                           final AntiCheatSessionState state,
                                           final List<ViolationCode> violations) {
        final PlayerStateDTO prev = state.getLastPlayerSnapshot();
        final PlayerStateDTO prev2 = state.getSecondLastPlayerSnapshot();

        // Need at least two prior frames for the second derivative.
        if (prev == null || prev2 == null) {
            return;
        }

        final PlayerStateDTO curr = payload.player();
        // Jump hold affects ascent, so validate only unsupported descent.
        final boolean unsupported = !isSupported(curr, state);
        final boolean descending = curr.y() >= prev.y();

        if (!unsupported || !descending) {
            state.resetFreefallFrames();
            return;
        }

        final int count = state.incrementFreefallFrames();
        // Skip the first few freefall frames: collision snaps at
        // platform edges make the second derivative unreliable.
        if (count < FREEFALL_ACCEL_THRESHOLD) {
            return;
        }

        // Near terminal velocity, second derivative is too flat to trust.
        final double fallDelta = Math.abs(curr.y() - prev.y());
        if (fallDelta >= MAX_FALL_VY - NEAR_TERMINAL_MARGIN) {
            return;
        }

        // Discrete second derivative of the frontend Y positions.
        final double acceleration = curr.y() - 2 * prev.y() + prev2.y();
        // Gravity cheats reduce acceleration; collision snaps usually raise it.
        if (acceleration < EXPECTED_GRAVITY - GRAVITY_TOLERANCE) {
            if (state.incrementGravityMismatchFrames() == GRAVITY_MISMATCH_FRAME_THRESHOLD) {
                log.warn("GRAVITY_MISMATCH: accel={}/{} ffFrames={} delta=({},{},{})",
                        String.format("%.3f", acceleration), EXPECTED_GRAVITY, count,
                        String.format("%.1f", prev2.y()),
                        String.format("%.1f", prev.y()),
                        String.format("%.1f", curr.y()));
                violations.add(ViolationCode.GRAVITY_MISMATCH);
            }
        } else {
            state.resetGravityMismatchFrames();
        }
    }

    private void checkDisplacement(final HeartbeatDTO payload,
                                   final AntiCheatSessionState state,
                                   final List<ViolationCode> violations) {
        final PlayerStateDTO prev = state.getLastPlayerSnapshot();
        if (prev == null) {
            return;
        }

        final PlayerStateDTO curr = payload.player();
        final double dx = Math.abs(curr.x() - prev.x());
        final double dy = Math.abs(curr.y() - prev.y());

        if (dx > MAX_DX + EPSILON || dy > MAX_DY + EPSILON) {
            violations.add(ViolationCode.DISPLACEMENT_EXCEEDED);
        }
    }

    private void checkFlying(final HeartbeatDTO payload,
                              final AntiCheatSessionState state,
                              final List<ViolationCode> violations) {
        final PlayerStateDTO prev = state.getLastPlayerSnapshot();
        if (prev == null) {
            return;
        }

        final PlayerStateDTO curr = payload.player();
        final double dy = curr.y() - prev.y();
        final boolean stableY = Math.abs(dy) <= MAX_STABLE_UNSUPPORTED_DY;
        final boolean supported = isSupported(curr, state);

        if (!stableY || supported) {
            state.resetUnsupportedStableFrames();
            if (supported) {
                state.resetUnsupportedMotion();
            }
        } else {
            final int count = state.incrementUnsupportedStableFrames();
            if (count == FLYING_FRAME_THRESHOLD) {
                violations.add(ViolationCode.FLYING_WITHOUT_SUPPORT);
            }
        }

        if (supported) {
            return;
        }

        final int dySign = significantSign(dy);
        if (dySign == 0) {
            return;
        }

        final int prevDySign = state.getLastUnsupportedDySign();
        state.setLastUnsupportedDySign(dySign);
        if (prevDySign != 0
                && prevDySign != dySign
                && state.incrementUnsupportedDirectionChanges() == UNSUPPORTED_DIRECTION_CHANGE_THRESHOLD) {
            violations.add(ViolationCode.FLYING_WITHOUT_SUPPORT);
        }
    }

    private int significantSign(final double value) {
        if (Math.abs(value) <= MIN_DIRECTION_CHANGE_DY) {
            return 0;
        }
        return value > 0 ? 1 : -1;
    }

    private boolean isSupported(final PlayerStateDTO player, final AntiCheatSessionState state) {
        final double feetY = player.y() + PLAYER_HALF_HEIGHT + FOOT_SENSOR_MARGIN;
        // Same pixel-to-tile conversion the frontend uses for map coordinates.
        final int centerCol = (int) Math.floor(player.x() / TILE_SIZE);
        final int leftCol = (int) Math.floor((player.x() - FOOT_SENSOR_HALF_WIDTH) / TILE_SIZE);
        final int rightCol = (int) Math.floor((player.x() + FOOT_SENSOR_HALF_WIDTH) / TILE_SIZE);
        final int row = (int) Math.floor(feetY / TILE_SIZE);

        return state.hasSupportAt(new Position(centerCol, row))
                || state.hasSupportAt(new Position(leftCol, row))
                || state.hasSupportAt(new Position(rightCol, row));
    }

}
