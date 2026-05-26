package ch.usi.inf.bsc.sa4.lab02spring.model;

/// Marker interface for non-enemy game objects.
///
/// Items are game objects that can be placed in a level and are not enemies.
public sealed interface Item extends GameObject permits StartFlag, ExitDoor, Coin, Box, Decoration, Shell, Climbable {
}
