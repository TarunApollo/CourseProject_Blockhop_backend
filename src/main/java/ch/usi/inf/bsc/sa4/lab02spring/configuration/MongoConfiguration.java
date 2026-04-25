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

/**
 * MongoDB configuration class.
 * Sets up custom converters and mapping context for the application.
 */
@Configuration
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class MongoConfiguration {

    /**
     * Configures the MongoMappingContext with the initial set of entities.
     *
     * @param customConversions the custom conversions to be used
     * @return the configured MongoMappingContext
     */
    @Bean
    public MongoMappingContext mongoMappingContext(final MongoCustomConversions customConversions) {
        final MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(Set.of(
                StartFlag.class, ExitDoor.class, Coin.class,
                Box.class, Decoration.class, Shell.class,
                Snail.class, Slime.class, User.class,
                Level.class, Attempt.class));
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        return mappingContext;
    }

    /**
     * Defines the custom conversions for MongoDB.
     *
     * @return the MongoCustomConversions bean
     */
    @Bean
    public MongoCustomConversions customConversions() {
        final List<Converter<?, ?>> custom = new ArrayList<>();
        custom.add(new PositionToStringConverter());
        custom.add(new StringToPositionConverter());
        custom.add(new ZonedDateTimeToDateConverter());
        custom.add(new DateToZonedDateTimeConverter());
        return new MongoCustomConversions(custom);
    }

    /**
     * Converter to transform a Position to a String.
     */
    @WritingConverter
    public static class PositionToStringConverter implements Converter<Position, String> {
        /**
         * Converts a Position to a compact String representation.
         *
         * @param position the Position to convert
         * @return the String representation
         */
        @Override
        public String convert(final Position position) {
            return position.compactString();
        }
    }

    /**
     * Converter to transform a String to a Position.
     */
    @ReadingConverter
    public static class StringToPositionConverter implements Converter<String, Position> {
        /**
         * Converts a String representation back to a Position object.
         *
         * @param string the String to convert
         * @return the Position object
         */
        @Override
        public Position convert(final String string) {
            final String[] parts = string.split(",");
            final int posX = Integer.parseInt(parts[0]);
            final int posY = Integer.parseInt(parts[1]);
            return new Position(posX, posY);
        }
    }

    /**
     * Converter to transform a ZonedDateTime to a Date.
     */
    @WritingConverter
    public static class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {
        /**
         * Converts a ZonedDateTime to a Date object.
         *
         * @param dateTime the ZonedDateTime to convert
         * @return the Date object
         */
        @Override
        public Date convert(final ZonedDateTime dateTime) {
            return Date.from(dateTime.toInstant());
        }
    }

    /**
     * Converter to transform a Date to a ZonedDateTime.
     */
    @ReadingConverter
    public static class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
        /**
         * Converts a Date to a ZonedDateTime object at UTC.
         *
         * @param date the Date to convert
         * @return the ZonedDateTime object
         */
        @Override
        public ZonedDateTime convert(final Date date) {
            return date.toInstant().atZone(ZoneOffset.UTC);
        }
    }
}
