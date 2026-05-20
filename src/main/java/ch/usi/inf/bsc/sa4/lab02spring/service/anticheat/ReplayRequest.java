package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

/// Data needed to replay one attempt.
///
/// @param userId       player id
/// @param levelId      level id
/// @param levelJson    level map JSON for the replay script
/// @param inputLogJson input log JSON for the replay script
public record ReplayRequest(
        String userId,
        String levelId,
        String levelJson,
        String inputLogJson) {}
