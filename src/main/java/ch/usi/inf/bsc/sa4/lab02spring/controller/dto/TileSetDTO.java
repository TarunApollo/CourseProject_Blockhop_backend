package ch.usi.inf.bsc.sa4.lab02spring.controller.dto;



import java.util.List;

public record TileSetDTO( int columns, int firstgid, String image, int imageheight, int imagewidth,  int margin,
    String name, int spacing, int tilecount, int tileheight, int tilewidth, List<TileData> tiles) {
    public record TileData(int id, String type, List<PropertyDTO> properties, ObjectGroupDTO objectgroup) {}
    

    public record PropertyDTO(String name, String type, Object value) {}

    public record ObjectGroupDTO(String draworder, String name, List<TiledObjectDTO> objects, double opacity,
        String type, boolean visible, double x, double y) {}

    public record TiledObjectDTO(double height, int id, String name, double rotation, String type,boolean visible,
        double width, double x, double y, List<PointDTO> polygon) {}

    public record PointDTO(double x, double y) {}
}