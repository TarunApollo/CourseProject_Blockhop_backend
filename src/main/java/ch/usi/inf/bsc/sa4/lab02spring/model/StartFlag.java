package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("start_flag")
public record StartFlag(Position pos) implements Item {}
