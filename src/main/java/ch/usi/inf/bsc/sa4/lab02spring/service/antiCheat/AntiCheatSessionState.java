package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PlayerStateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;

import java.util.Set;

/// In memory anticheat state for one run
@SuppressWarnings("NullAway.Init")
public class AntiCheatSessionState {

    private final Set<Position> supportTiles;
    private PlayerStateDTO lastPlayerSnapshot;
    private int unsupportedStableFrames;

    public AntiCheatSessionState(final Set<Position> supportTiles) {
        this.supportTiles = Set.copyOf(supportTiles);
    }

    public PlayerStateDTO getLastPlayerSnapshot() {
        return lastPlayerSnapshot;
    }

    public boolean hasSupportAt(final Position position) {
        return supportTiles.contains(position);
    }

    public int incrementUnsupportedStableFrames() {
        unsupportedStableFrames++;
        return unsupportedStableFrames;
    }

    public void resetUnsupportedStableFrames() {
        unsupportedStableFrames = 0;
    }

    public void setLastPlayerSnapshot(final PlayerStateDTO snapshot) {
        lastPlayerSnapshot = snapshot;
    }
}
