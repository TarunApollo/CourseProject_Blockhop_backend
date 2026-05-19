package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

/// Record for a request of a replay of a level. Whose attempt is being replayed
/// 
public record ReplayRequest(
        // userId id of the player
        String userId,
        // levelId id of the level being replayed
        String levelId,
        /// levelJson serialized Tiled level map consumed by the replay script
        String levelJson,
        /// inputLogJson serialized input log consumed by the replay script
        String inputLogJson) {
};
