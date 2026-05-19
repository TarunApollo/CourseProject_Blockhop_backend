package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

/// Record for a request of a replay of a level. Whose attempt is being replayed
/// userId: userId id of the player
/// levelId: levelId id of the level being replayed
/// levelJson: levelJson serialized Tiled level map consumed by the replay script
/// inputLogJson: inputLogJson serialized input log consumed by the replay script
public record ReplayRequest(
        String userId,
        String levelId,
        String levelJson,
        String inputLogJson) {
};
