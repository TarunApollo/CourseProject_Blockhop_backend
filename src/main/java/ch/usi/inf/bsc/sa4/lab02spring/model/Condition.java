package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/// Sealed interface for the level clear condition.
/// JSON examples:
/// - No condition (reach exit): {}
/// - Collect coins: {"target": "coin"}
public sealed interface Condition {
    record NoClearCondition() implements Condition {}
    record SomeClearCondition(ClearConditionType target) implements Condition {}

    @JsonCreator
    static Condition fromJson(@JsonProperty("target") String target) {
        if (target == null || target.isEmpty()) {
            return new NoClearCondition();
        }
        return new SomeClearCondition(ClearConditionType.fromValue(target));
    }
}