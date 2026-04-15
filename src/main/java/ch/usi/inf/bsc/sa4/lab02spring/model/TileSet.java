package ch.usi.inf.bsc.sa4.lab02spring.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/// right now it parse all data from json becasue phaser need them
/// but still keep jsonignore avoid mistakes in future
/// polygon and point(x,y) working for collision,its not position
@JsonIgnoreProperties(ignoreUnknown = true)
public record TileSet(
    int firstgid,
    String name,
    int tilewidth,
    int tileheight,
    int tilecount,
    int columns,
    String image,
    int imagewidth,
    int imageheight,
    int margin,
    int spacing,
    List<TileData> tiles
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TileData(
        int id,
        String type,
        List<Property> properties,
        ObjectGroup objectgroup
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Property(
        String name,
        String type,
        Object value
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObjectGroup(
        String draworder,
        String name,
        List<TileObject> objects,
        int opacity,
        String type,
        boolean visible,
        int x,
        int y 
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TileObject(
        int id,
        String name,
        String type,
        boolean visible,
        double rotation,
        double x,
        double y,
        double width,
        double height,
        List<Point> polygon
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Point(
        double x,
        double y
    ) {}

}
