package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import ch.usi.inf.bsc.sa4.lab02spring.model.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

/// MongoDB configuration for the application.
/// Registers MongoDB mapping packages, entity classes, and custom converters
/// used to persist and read domain objects.
///
@Configuration
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "test";
    }

    /// Tells Spring Data MongoDB to scan the model package, discover classes with @TypeAlias annotations
    /// (e.g., @TypeAlias("box") on Box), and resolve type aliases to their corresponding classes when deserializing.
    /// Without this, Spring cannot map "_class": "box" back to Box.class.
    /// So the batch endpoints would fail for example.
    @Override
    protected Set<String> getMappingBasePackages() {
        return Set.of("ch.usi.inf.bsc.sa4.lab02spring.model");
    }

    @Override
    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        Set<Class<?>> entitySet = super.getInitialEntitySet();  // gets @Document classes
        entitySet.addAll(Set.of(
                StartFlag.class, ExitDoor.class, Coin.class,
                Box.class, Decoration.class, Shell.class,
                Snail.class, Slime.class
        ));
        return entitySet;
    }

    ///
    /// Converts a {@link Position} to its compact string representation.
    ///
    @WritingConverter
    public static class PositionToStringConverter implements Converter<Position, String> {
        public String convert(Position position){
            return position.compactString();
        }
    }

    ///
    /// Converts a compact string representation to a {@link Position}.
    ///
    @ReadingConverter
    public static class StringToPositionConverter implements Converter<String, Position> {
        public Position convert(String string){
            String[] parts = string.split(",");
            int posX = Integer.parseInt(parts[0]);
            int posY = Integer.parseInt(parts[1]);
            return new Position(posX, posY);
        }
    }

    ///
    /// Converts a {@link ZonedDateTime} to a {@link Date} for MongoDB storage.
    ///
    @WritingConverter
    public static class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {
        @Override
        public Date convert(final ZonedDateTime dateTime) {
            return Date.from(dateTime.toInstant());
        }
    }

    ///
    /// Converts a {@link Date} read from MongoDB to a {@link ZonedDateTime}.
    ///
    @ReadingConverter
    public static class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(final Date date) {
            return date.toInstant().atZone(ZoneOffset.UTC);
        }
    }

    @Override
    public MongoCustomConversions customConversions(){
        List<Converter<?, ?>> custom = new ArrayList<>();
        custom.add(new PositionToStringConverter());
        custom.add(new StringToPositionConverter());
        custom.add(new ZonedDateTimeToDateConverter());
        custom.add(new DateToZonedDateTimeConverter());
        return new MongoCustomConversions(custom);
    }
}
