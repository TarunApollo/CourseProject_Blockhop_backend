package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import ch.usi.inf.bsc.sa4.lab02spring.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoMappingContext mongoMappingContext(MongoCustomConversions customConversions) {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(Set.of(
                StartFlag.class, ExitDoor.class, Coin.class,
                Box.class, Decoration.class, Shell.class,
                Snail.class, Slime.class, User.class,
                Level.class, Attempt.class));
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        return mappingContext;
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> custom = new ArrayList<>();
        custom.add(new PositionToStringConverter());
        custom.add(new StringToPositionConverter());
        custom.add(new ZonedDateTimeToDateConverter());
        custom.add(new DateToZonedDateTimeConverter());
        return new MongoCustomConversions(custom);
    }

    @WritingConverter
    public static class PositionToStringConverter implements Converter<Position, String> {
        public String convert(Position position) {
            return position.compactString();
        }
    }

    @ReadingConverter
    public static class StringToPositionConverter implements Converter<String, Position> {
        public Position convert(String string) {
            String[] parts = string.split(",");
            int posX = Integer.parseInt(parts[0]);
            int posY = Integer.parseInt(parts[1]);
            return new Position(posX, posY);
        }
    }

    @WritingConverter
    public static class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {
        @Override
        public Date convert(final ZonedDateTime dateTime) {
            return Date.from(dateTime.toInstant());
        }
    }

    @ReadingConverter
    public static class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(final Date date) {
            return date.toInstant().atZone(ZoneOffset.UTC);
        }
    }
}
