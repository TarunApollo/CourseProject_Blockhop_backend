package ch.usi.inf.bsc.sa4.lab02spring.model;
import java.util.List;

// class to interpret the tileset json data

public class TileSet {
    public int columns;
    public int firstgid;
    public String image;
    public int imageheight;
    public int imagewidth;
    public int margin;
    public String name;
    public int spacing;
    public int tilecount;
    public int tileheight;
    public int tilewidth;

    //list of individual tiles
    public List<TileData> tiles;

    
     // Represents an individual tile entry in the "tiles" array.

  
    public static class TileData {
        public int id;
        public String type;

        //includes an array of properties and an object group
        public List<Property> properties;
        public ObjectGroup objectgroup;
     
    }

    
     //Represents the properties array inside each individual tile tile.
     
   
    public static class Property {
        public String name;
        public String type;
        public Object value;
    }

    
     //Represents collision or shape data in the "objectgroup" field.
     
    
    public static class ObjectGroup {
        public String draworder;
        public String name;
        //s
        public List<TiledObject> objects;
        public double opacity;
        public String type;
        public boolean visible;
        public double x;
        public double y;
    }

    
     // Represents each object in objectGroup.
     
   
    public static class TiledObject {
        public double height;
        public int id;
        public String name;
        public double rotation;
        public String type;
        public boolean visible;
        public double width;
        public double x;
        public double y;
        public List<Point> polygon; // (Just if polygon there are multiple points (coordinates))
    }

    //coordinates
    public static class Point {
        public double x;
        public double y;
    }
}

