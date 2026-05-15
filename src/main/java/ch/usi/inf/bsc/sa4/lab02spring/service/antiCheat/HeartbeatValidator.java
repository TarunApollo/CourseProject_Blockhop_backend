package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PlayerStateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class HeartbeatValidator {

    private static final double EXPECTED_GRAVITY = 2.5;

    // TODO: Tighten these tolerances through play testing.
    // They are way too generous right now.
    private static final double MAX_DX = 25;
    private static final double MAX_DY = 42.5;
    private static final double TILE_SIZE = 128;
    private static final double PLAYER_HALF_HEIGHT = 64;
    private static final double FOOT_SENSOR_HALF_WIDTH = 32;
    private static final double FOOT_SENSOR_MARGIN = 4;
    private static final double MAX_STABLE_UNSUPPORTED_DY = 0.5;
    // TODO: 8 frames means a cheater can fly in 7-frame bursts. 
    // So we need to tighten here too.
    private static final int FLYING_FRAME_THRESHOLD = 8;

    private static final double EPSILON = 0.001;

    public HeartbeatResponseDTO validate(final HeartbeatDTO payload,
                                         final AntiCheatSessionState state) {
        final List<ViolationCode> violations = new ArrayList<>();

        checkGravity(payload, violations);
        checkDisplacement(payload, state, violations);
        checkFlying(payload, state, violations);

        state.setLastPlayerSnapshot(payload.player());

        return new HeartbeatResponseDTO(payload.frame(), violations);
    }

    private void checkGravity(final HeartbeatDTO payload, final List<ViolationCode> violations) {
        if (Math.abs(payload.gravity() - EXPECTED_GRAVITY) > EPSILON) {
            violations.add(ViolationCode.GRAVITY_MISMATCH);
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

        final boolean stableY = Math.abs(payload.player().y() - prev.y()) <= MAX_STABLE_UNSUPPORTED_DY;
        if (!stableY || isSupported(payload.player(), state)) {
            state.resetUnsupportedStableFrames();
            return;
        }

        if (state.incrementUnsupportedStableFrames() >= FLYING_FRAME_THRESHOLD) {
            violations.add(ViolationCode.FLYING_WITHOUT_SUPPORT);
        }
    }

    private boolean isSupported(final PlayerStateDTO player, final AntiCheatSessionState state) {
        final double feetY = player.y() + PLAYER_HALF_HEIGHT + FOOT_SENSOR_MARGIN;
        // Mirror the frontend foot sensor roughly sample center, left foot, and
        // right foot so standing near an edge does not look unsupported.
        // Taken from: frontend/src/components/levelPlayer/main.js
        return hasSupportAt(player.x(), feetY, state)
                || hasSupportAt(player.x() - FOOT_SENSOR_HALF_WIDTH, feetY, state)
                || hasSupportAt(player.x() + FOOT_SENSOR_HALF_WIDTH, feetY, state);
    }

    private boolean hasSupportAt(final double x, final double y, final AntiCheatSessionState state) {
        return state.hasSupportAt(new Position((int) Math.floor(x / TILE_SIZE), (int) Math.floor(y / TILE_SIZE)));
    }

}
