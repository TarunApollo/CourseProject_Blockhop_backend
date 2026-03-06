package ch.usi.inf.bsc.sa4.lab02spring.model;

public class WorldLayer {
        private int width;
    private int height;

    public WorldLayer(int width, int height){
        this.width = width;
        this.height = height;

        // we need some structure to contain the tiles here
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

}
