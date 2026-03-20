package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("decoration")
public record Decoration(Position pos) implements Item {

}
