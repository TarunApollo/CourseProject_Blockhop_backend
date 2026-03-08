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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public abstract GameObject copy();
}
