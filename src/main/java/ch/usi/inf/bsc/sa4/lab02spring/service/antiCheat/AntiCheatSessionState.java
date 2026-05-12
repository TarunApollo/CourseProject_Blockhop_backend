package ch.usi.inf.bsc.sa4.lab02spring.service.antiCheat;

import ch.usi.inf.bsc.sa4.lab02spring.controller.dto.PlayerStateDTO;

/// In memory anticheat state for one run
@SuppressWarnings("NullAway.Init")
public class AntiCheatSessionState {

    private PlayerStateDTO lastPlayerSnapshot;

    public PlayerStateDTO getLastPlayerSnapshot() {
        return lastPlayerSnapshot;
    }

    public void setLastPlayerSnapshot(final PlayerStateDTO snapshot) {
        lastPlayerSnapshot = snapshot;
    }
}
