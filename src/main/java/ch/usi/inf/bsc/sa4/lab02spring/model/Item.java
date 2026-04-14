package ch.usi.inf.bsc.sa4.lab02spring.model;

public sealed interface Item extends GameObject permits StartFlag, ExitDoor, Coin, Box, Decoration, Shell {
}
