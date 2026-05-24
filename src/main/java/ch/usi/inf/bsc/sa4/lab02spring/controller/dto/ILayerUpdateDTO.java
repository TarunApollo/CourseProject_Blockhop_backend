package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;

import java.util.List;

/// Base contract for batch layer updates.
@FunctionalInterface
public interface ILayerUpdateDTO {
    /// Returns the list of entries (tiles or objects) for the layer.
    ///
    /// @spec.modifies nothing.
    /// @spec.effects returns the entries contained in this layer update.
    /// @return the entries to apply to the layer
    List<EditorLevelDTO> entries();
}
