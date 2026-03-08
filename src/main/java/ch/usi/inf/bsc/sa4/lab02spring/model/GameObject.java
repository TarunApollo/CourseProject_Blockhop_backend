package ch.usi.inf.bsc.sa4.lab02spring.model;

@SuppressWarnings("NullAway.Init")
public abstract class GameObject {
    int id;
    int gid;
    Position pos;

    public GameObject() {}
    public GameObject(GameObject g) {
        this.id = g.id;
        this.gid = g.gid;
        this.pos = g.pos;
    }

    public abstract GameObject copy();
}
