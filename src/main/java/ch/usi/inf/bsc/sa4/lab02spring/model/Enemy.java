package ch.usi.inf.bsc.sa4.lab02spring.model;

/// Marker interface for enemy game objects.
///
/// Enemies are game objects that can appear in a level as hostile entities.
public sealed interface Enemy extends GameObject permits Bee, Snail, Slime, SpikedAlien {
}
