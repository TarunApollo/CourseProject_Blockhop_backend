package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.List;


 // Record to interpret the tileset JSON data.
 
public record TileSet(
    int columns,
    int firstgid,
    String image,
    int imageheight,
    int imagewidth,
    int margin,
    String name,
    int spacing,
    int tilecount,
    int tileheight,
    int tilewidth,
    List<TileData> tiles
) {

    
     //Represents an individual tile entry in the "tiles" array.
     
    public record TileData(
        int id,
        String type,
        List<Property> properties,
        ObjectGroup objectgroup
    ) {}

    
     //Represents the properties array inside each individual tile.
     
    public record Property(
        String name,
        String type,
        Object value
    ) {}

    
     // Represents collision or shape data in the "objectgroup" field.
     
    public record ObjectGroup(
        String draworder,
        String name,
        List<TiledObject> objects,
        double opacity,
        String type,
        boolean visible,
        double x,
        double y
    ) {}


     // Represents each object in objectgroup.
     
    public record TiledObject(
        double height,
        int id,
        String name,
        double rotation,
        String type,
        boolean visible,
        double width,
        double x,
        double y,
        List<Point> polygon
    ) {}


      //Represents individual coordinate points.
    
    public record Point(
        double x,
        double y
    ) {}
}