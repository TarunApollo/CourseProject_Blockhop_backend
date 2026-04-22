package ch.usi.inf.bsc.sa4.lab02spring.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;

///
/// Represents a preset for selecting a date range.
/// Implementations cover both an all-time range and relative ranges such as
/// today or the last N days.
///
public sealed interface DateRangePreset
        permits DateRangePreset.AllTimeDateRangePreset, DateRangePreset.RelativeDateRangePreset {

    ///
    /// Parses a preset value from a string representation.
    /// @param value the textual preset value
    /// @return the matching date range preset
    ///
    static DateRangePreset fromValue(final String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);

        if (AllTimeDateRangePreset.ALL_TIME.name().equals(normalized)) {
            return AllTimeDateRangePreset.ALL_TIME;
        }

        return RelativeDateRangePreset.valueOf(normalized);
    }

    ///
    /// A preset representing the complete time span.
    ///
    enum AllTimeDateRangePreset implements DateRangePreset {
        ALL_TIME
    }

    ///
    /// A preset representing a time range relative to the current day.
    ///
    enum RelativeDateRangePreset implements DateRangePreset {
        TODAY(Duration.ofDays(1)),
        LAST_7_DAYS(Duration.ofDays(7)),
        LAST_30_DAYS(Duration.ofDays(30)),
        LAST_365_DAYS(Duration.ofDays(365));

        /// The duration covered by the preset.
        private final Duration duration;

        RelativeDateRangePreset(final Duration duration) {
            this.duration = duration;
        }

        public Duration duration() {
            return duration;
        }

        ///
        /// Computes the first instant of the represented range in UTC.
        /// @return the start of the range in UTC
        ///
        public ZonedDateTime rangeStart() {
            long days = duration.toDays();
            return LocalDate.now().minusDays(days - 1).atStartOfDay(ZoneOffset.UTC);
        }
    }
}
