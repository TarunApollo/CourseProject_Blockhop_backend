package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.HeartbeatResponseDTO;
import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PlayerStateDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class HeartbeatValidator {

    private static final double EXPECTED_GRAVITY = 2.5;

    // The bounds here are generous/have extra margin but should
    // still catch teleports and obvious speed hacks.
    // might need finetuning.
    private static final double MAX_DX = 25;
    private static final double MAX_DY = 42.5;

    private static final double EPSILON = 0.001;

    public HeartbeatResponseDTO validate(final HeartbeatDTO payload, final AntiCheatSessionState state) {
        final List<ViolationCode> violations = new ArrayList<>();

        checkGravity(payload, violations);
        checkDisplacement(payload, state, violations);

        state.setLastPlayerSnapshot(payload.player());

        return new HeartbeatResponseDTO(payload.runId(), payload.frame(), violations);
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

}
