package ch.usi.inf.bsc.sa4.lab02spring.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("snail")
public record Snail(int gid, Position pos, boolean isHiding ) implements Enemy {
    public Snail withHiding(boolean hiding){
        return new Snail(this.gid(),this.pos(),hiding);
    }
}
