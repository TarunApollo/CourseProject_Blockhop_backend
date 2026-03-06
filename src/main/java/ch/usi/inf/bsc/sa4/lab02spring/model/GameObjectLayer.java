package ch.usi.inf.bsc.sa4.lab02spring.model;

public class GameObjectLayer {
    
    private int width;
    private int height;

    public GameObjectLayer(int width, int height){
        this.width = width;
        this.height = height;

        //here we need some structure to contain the objects
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

}
