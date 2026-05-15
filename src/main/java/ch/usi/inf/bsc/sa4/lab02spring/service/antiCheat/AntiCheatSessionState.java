package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PlayerStateDTO;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/// In-memory anticheat state for one run
public class AntiCheatSessionState {

    private final Set<Position> supportTiles;
    private @Nullable PlayerStateDTO lastPlayerSnapshot;
    /// Two frames back, needed for 2nd derivative acceleration.
    private @Nullable PlayerStateDTO secondLastPlayerSnapshot;
    private int unsupportedStableFrames;
    /// Consecutive freefall frames (unsupported and descending) for acceleration check.
    private int freefallFrames;
    private int unsupportedDirectionChanges;
    private int lastUnsupportedDySign;
    private int gravityMismatchFrames;

    public AntiCheatSessionState(final Set<Position> supportTiles) {
        this.supportTiles = Set.copyOf(supportTiles);
    }

    public @Nullable PlayerStateDTO getLastPlayerSnapshot() {
        return lastPlayerSnapshot;
    }

    public @Nullable PlayerStateDTO getSecondLastPlayerSnapshot() {
        return secondLastPlayerSnapshot;
    }

    public void shiftSnapshotHistory(final PlayerStateDTO current) {
        secondLastPlayerSnapshot = lastPlayerSnapshot;
        lastPlayerSnapshot = current;
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

    public int incrementUnsupportedDirectionChanges() {
        unsupportedDirectionChanges++;
        return unsupportedDirectionChanges;
    }

    public void resetUnsupportedMotion() {
        unsupportedDirectionChanges = 0;
        lastUnsupportedDySign = 0;
    }

    public int getLastUnsupportedDySign() {
        return lastUnsupportedDySign;
    }

    public void setLastUnsupportedDySign(final int dySign) {
        lastUnsupportedDySign = dySign;
    }

    public int incrementFreefallFrames() {
        freefallFrames++;
        return freefallFrames;
    }

    public void resetFreefallFrames() {
        freefallFrames = 0;
        gravityMismatchFrames = 0;
    }

    public int getFreefallFrames() {
        return freefallFrames;
    }

    public int incrementGravityMismatchFrames() {
        gravityMismatchFrames++;
        return gravityMismatchFrames;
    }

    public void resetGravityMismatchFrames() {
        gravityMismatchFrames = 0;
    }

    public void setLastPlayerSnapshot(final PlayerStateDTO snapshot) {
        lastPlayerSnapshot = snapshot;
    }
}
