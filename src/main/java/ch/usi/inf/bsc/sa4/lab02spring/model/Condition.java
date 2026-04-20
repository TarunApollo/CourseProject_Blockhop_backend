package ch.usi.inf.bsc.sa4.lab02spring.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// By using polymorphism with (@JsonTypeInfo), the API accepts
// exactly what it needs for the specific clear condition type.
// eg: A some condition contains the target type.

/// Represents a level clear condition.
///
/// Implementations model either the absence of a clear condition or a
/// condition that requires reaching a specific target type.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Condition.NoClearCondition.class, name = "none"),
    @JsonSubTypes.Type(value = Condition.SomeClearCondition.class, name = "some")
})
public sealed interface Condition 
    permits Condition.NoClearCondition, Condition.SomeClearCondition {

    // door is open by default
    record NoClearCondition() implements Condition {}

    record SomeClearCondition(ClearConditionType target) implements Condition {}
}
