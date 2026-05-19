package ch.usi.inf.bsc.sa4.lab02spring.service.anticheat;

public record ReplayRequest(
        String userId,
        String levelId,
        String levelJson,
        String inputLogJson) {
};
