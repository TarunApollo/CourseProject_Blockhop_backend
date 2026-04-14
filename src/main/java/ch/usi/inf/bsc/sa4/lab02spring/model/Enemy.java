package ch.usi.inf.bsc.sa4.lab02spring.model;

public sealed interface Enemy extends GameObject
        permits Slime, Snail {
}
