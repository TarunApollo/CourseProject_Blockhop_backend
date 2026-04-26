package ch.usi.inf.bsc.sa4.lab02spring.model;

public record Position(int x, int y) {

    /// Returns this position as a compact `x,y` string.
    /// @return a comma-separated representation of the coordinates
    public String compactString() {
        return this.x() + "," + this.y();
    }
}
