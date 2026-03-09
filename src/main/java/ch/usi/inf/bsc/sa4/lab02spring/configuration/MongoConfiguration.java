package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfiguration {

    @WritingConverter
    static public class PositionToStringConverter implements Converter<Position, String> {
        public String convert(Position position){
            return position.x() + "," + position.y();
        }
    }

    @ReadingConverter
    static public class StringToPositionConverter implements Converter<String, Position> {
        public Position convert(String string){
            String[] parts = string.split(",");
            int posX = Integer.parseInt(parts[0]);
            int posY = Integer.parseInt(parts[1]);
            return new Position(posX, posY);
        }
    }

    @Bean
    public MongoCustomConversions customConversions(){
        List<Converter<?, ?>> custom = new ArrayList<>();
        custom.add(new PositionToStringConverter());
        custom.add(new StringToPositionConverter());
        return new MongoCustomConversions(custom);
    }
}
