package ch.usi.inf.bsc.sa4.lab02spring.model;

public record Position(int x, int y) {

    public String compactString() {
        return this.x() + "," + this.y();
    }
}
