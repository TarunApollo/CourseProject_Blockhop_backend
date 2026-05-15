package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PlayerStateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/// In memory anticheat state for one run
public class AntiCheatSessionState {

    private final Set<Position> supportTiles;
    private @Nullable PlayerStateDTO lastPlayerSnapshot;
    private int unsupportedStableFrames;

    public AntiCheatSessionState(final Set<Position> supportTiles) {
        this.supportTiles = Set.copyOf(supportTiles);
    }

    public @Nullable PlayerStateDTO getLastPlayerSnapshot() {
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
