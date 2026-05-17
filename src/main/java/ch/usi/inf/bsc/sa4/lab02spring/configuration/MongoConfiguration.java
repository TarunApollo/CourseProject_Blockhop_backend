package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import ch.usi.inf.bsc.sa4.lab02spring.model.Level;
import ch.usi.inf.bsc.sa4.lab02spring.model.Attempt;
import ch.usi.inf.bsc.sa4.lab02spring.model.Box;
import ch.usi.inf.bsc.sa4.lab02spring.model.Bee;
import ch.usi.inf.bsc.sa4.lab02spring.model.Coin;
import ch.usi.inf.bsc.sa4.lab02spring.model.Decoration;
import ch.usi.inf.bsc.sa4.lab02spring.model.ExitDoor;
import ch.usi.inf.bsc.sa4.lab02spring.model.Position;
import ch.usi.inf.bsc.sa4.lab02spring.model.Shell;
import ch.usi.inf.bsc.sa4.lab02spring.model.Slime;
import ch.usi.inf.bsc.sa4.lab02spring.model.Snail;
import ch.usi.inf.bsc.sa4.lab02spring.model.StartFlag;
import ch.usi.inf.bsc.sa4.lab02spring.model.User;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/// MongoDB configuration for the application. Registers MongoDB mapping
/// packages, entity classes, and custom converters used to persist and read
/// domain objects.
///
@Configuration
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class MongoConfiguration {

    /// Configures the Mongo mapping context with initial entities.
    ///
    /// @param customConversions the custom conversions to use
    /// @return the configured mapping context
    @Bean
    public MongoMappingContext mongoMappingContext(final MongoCustomConversions customConversions) {
        final MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(Set.of(
                StartFlag.class, ExitDoor.class, Coin.class,
                Box.class, Decoration.class, Shell.class,
                Snail.class, Slime.class, User.class,
                Bee.class,
                Level.class, Attempt.class));
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        return mappingContext;
    }

    /// Converts a [Position] to its compact string representation.
    ///
    @WritingConverter
    public static class PositionToStringConverter implements Converter<Position, String> {
        @Override
        public String convert(final Position position) {
            return position.compactString();
        }
    }

    /// Converts a compact string representation to a [Position].
    ///
    @ReadingConverter
    public static class StringToPositionConverter implements Converter<String, Position> {
        @Override
        public Position convert(final String string) {
            final String[] parts = string.split(",");
            final int posX = Integer.parseInt(parts[0]);
            final int posY = Integer.parseInt(parts[1]);
            return new Position(posX, posY);
        }
    }

    /// Converts a [ZonedDateTime] to an [Instant] for MongoDB storage.
    ///
    @WritingConverter
    public static class ZonedDateTimeToInstantConverter implements Converter<ZonedDateTime, Instant> {
        @Override
        public Instant convert(final ZonedDateTime dateTime) {
            return dateTime.toInstant();
        }
    }

    /// Converts an [Instant] read from MongoDB to a [ZonedDateTime] in UTC.
    ///
    @ReadingConverter
    public static class InstantToZonedDateTimeConverter implements Converter<Instant, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(final Instant instant) {
            return instant.atZone(ZoneOffset.UTC);
        }
    }

    /// Converts a [Date] read from MongoDB to a [ZonedDateTime] in UTC.
    ///
    @ReadingConverter
    public static class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(final Date date) {
            return date.toInstant().atZone(ZoneOffset.UTC);
        }
    }

    /// Configures the custom conversions for MongoDB.
    ///
    /// @return the custom conversions
    @Bean
    public MongoCustomConversions customConversions() {
        final List<Converter<?, ?>> custom = new ArrayList<>();
        custom.add(new PositionToStringConverter());
        custom.add(new StringToPositionConverter());
        custom.add(new ZonedDateTimeToInstantConverter());
        custom.add(new InstantToZonedDateTimeConverter());
        custom.add(new DateToZonedDateTimeConverter());
        return new MongoCustomConversions(custom);
    }
}
