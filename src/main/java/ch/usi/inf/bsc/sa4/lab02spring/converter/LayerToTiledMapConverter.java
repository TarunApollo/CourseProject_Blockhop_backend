package ch.usi.inf.bsc.sa4.lab02spring.converter;

import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Content;
import ch.usi.inf.bsc.sa4.lab02spring.model.GameObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.GroundObject;
import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.TileSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LayerToTiledMapConverter {

    private static final Map<String, Object> MAP_METADATA = Map.of(
        "type", "map",
        "orientation", "orthogonal",
        "renderorder", "right-down",
        "tilewidth", 128,
        "tileheight", 128
);

    public static Map<String, Object> convertPipeline(
            Level level,
            TileSet tileSet
    ) 
    {
        Map<String,Object> worldLayer = buildWolrdLayer(level.getWorldLayer(), level.getWidth(), level.getHeight());
        Map<String,Object> objectLayer = buildObjectLayer(level.getObjectLayer());
        Map<String,Object> tilesetMap = buildTileset(tileSet);
        Map<String,Object> res = new LinkedHashMap<>();
        res.putAll(MAP_METADATA);
        res.put("width",level.getWidth());
        res.put("height",level.getHeight());
        res.put("layers",List.of(worldLayer,objectLayer));
        res.put("tilesets",List.of(tilesetMap));
        return res;
    }

    private static Map<String, Object> buildWolrdLayer(
            Map<Position, GroundObject> worldLayer,
            int width,
            int height
    ) {
        List<Integer> data = new ArrayList<>(Collections.nCopies(width * height, 0));
        for (Map.Entry<Position, GroundObject> entry : worldLayer.entrySet()) {
            Position pos = entry.getKey();
            GroundObject groundObject = entry.getValue();
            int x = pos.x();
            int y = pos.y();
            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }
            int idx = y * width + x;
            data.set(idx, groundObject.gid());
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("name", "World");
        res.put("type", "tilelayer");
        res.put("width", width);
        res.put("height", height);
        res.put("opacity", 1);
        res.put("visible", true);
        res.put("x", 0);
        res.put("y", 0);
        res.put("data", data);
        return res;
    }

    private static Map<String,Object> buildObjectLayer(
        Map<Position,GameObject> objectLayer
    )
    {
        List<Map<String,Object>> objects = new ArrayList<>();
        for(GameObject gameObject :objectLayer.values())
        {
            objects.add(toTiledObject(gameObject));
        }
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("name","QMLayer");
        res.put("type","objectgroup");
        res.put("draworder","topdown");
        res.put("opacity",1);
        res.put("visible",true);
        res.put("x",0);
        res.put("y",0);
        res.put("objects",objects);
        return res;
    }

    private static Map<String,Object> toTiledObject(GameObject gameObject)
    {
        Position pos = gameObject.pos();
        int tileSize = 128;
        int x = pos.x() * tileSize;
        int y = (pos.y()+1) * tileSize;
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("gid",gameObject.gid());
        res.put("x",x);
        res.put("y",y);
        res.put("width",tileSize);
        res.put("height",tileSize);
        res.put("visible",true);
        res.put("rotation",0);
        res.put("name","");
        res.put("type","");
        if(gameObject instanceof Box box)
        {
            List<Map<String,Object>> properties = buildBoxProperties(box);
            if (!properties.isEmpty()) {
                res.put("properties", properties);
            }
        }
        return res;
    }

    private static List<Map<String,Object>> buildBoxProperties(Box box)
    {
        List<Map<String,Object>> properties = new ArrayList<>();
        if (box.content() instanceof Content.SomeContent someContent) {
            Map<String,Object> contentProperty = new LinkedHashMap<>();
            contentProperty.put("name", "Content");
            contentProperty.put("type", "string");
            contentProperty.put("value", someContent.type().value());
            properties.add(contentProperty);
        }
        return properties;
    }

    private static Map<String,Object> buildTileset(TileSet tileSet)
    {
        List<Map<String,Object>> tiles = new ArrayList<>();
        for(TileSet.TileData tile :tileSet.tiles())
        {
            tiles.add(toTiledTile(tile));
        }

        Map<String,Object> res = new LinkedHashMap<>();
        res.put("firstgid",tileSet.firstgid());
        res.put("name",tileSet.name());
        res.put("tilewidth",tileSet.tilewidth());
        res.put("tileheight",tileSet.tileheight());
        res.put("tilecount",tileSet.tilecount());
        res.put("columns",tileSet.columns());
        /// tileset only store the imgae filename
        /// prefix write fixed thing in frontend path
        res.put("image","/assets/" + tileSet.image());
        res.put("imagewidth",tileSet.imagewidth());
        res.put("imageheight",tileSet.imageheight());
        res.put("margin",tileSet.margin());
        res.put("spacing",tileSet.spacing());
        res.put("tiles",tiles);
        return res;
    }

    private static Map<String,Object> toTiledTile(TileSet.TileData tile)
    {
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("id",tile.id());
        res.put("type",tile.type());

        if (tile.properties() != null && !tile.properties().isEmpty()) {
            List<Map<String,Object>> properties = new ArrayList<>();
            for (TileSet.Property property : tile.properties()) {
                properties.add(toTiledProperty(property));
            }
            res.put("properties",properties);
        }

        if (tile.objectgroup() != null) {
            res.put("objectgroup",toTiledObjectGroup(tile.objectgroup()));
        }
        return res;
    }

    private static Map<String,Object> toTiledProperty(TileSet.Property property)
    {
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("name",property.name());
        res.put("type",property.type());
        res.put("value",property.value());
        return res;
    }

    private static Map<String,Object> toTiledObjectGroup(TileSet.ObjectGroup objectGroup)
    {
        List<Map<String,Object>> objects = new ArrayList<>();
        for (TileSet.TileObject object : objectGroup.objects()) {
            objects.add(toTiledTileObject(object));
        }

        Map<String,Object> res = new LinkedHashMap<>();
        res.put("draworder",objectGroup.draworder());
        res.put("name",objectGroup.name());
        res.put("opacity",objectGroup.opacity());
        res.put("type",objectGroup.type());
        res.put("visible",objectGroup.visible());
        res.put("x",objectGroup.x());
        res.put("y",objectGroup.y());
        res.put("objects",objects);
        return res;
    }

    private static Map<String,Object> toTiledTileObject(TileSet.TileObject object)
    {
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("id",object.id());
        res.put("name",object.name());
        res.put("type",object.type());
        res.put("visible",object.visible());
        res.put("rotation",object.rotation());
        res.put("x",object.x());
        res.put("y",object.y());
        res.put("width",object.width());
        res.put("height",object.height());

        if (object.polygon() != null && !object.polygon().isEmpty()) {
            List<Map<String,Object>> polygon = new ArrayList<>();
            for (TileSet.Point point : object.polygon()) {
                polygon.add(toTiledPoint(point));
            }
            res.put("polygon",polygon);
        }
        return res;
    }

    private static Map<String,Object> toTiledPoint(TileSet.Point point)
    {
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("x",point.x());
        res.put("y",point.y());
        return res;
    }
}
