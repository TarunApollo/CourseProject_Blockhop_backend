package ch.usi.inf.bsc.sa4.lab02spring.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;

public sealed interface DateRangePreset
        permits DateRangePreset.AllTimeDateRangePreset, DateRangePreset.RelativeDateRangePreset {

    static DateRangePreset fromValue(final String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);

        if (AllTimeDateRangePreset.ALL_TIME.name().equals(normalized)) {
            return AllTimeDateRangePreset.ALL_TIME;
        }

        return RelativeDateRangePreset.valueOf(normalized);
    }

    enum AllTimeDateRangePreset implements DateRangePreset {
        ALL_TIME
    }

    enum RelativeDateRangePreset implements DateRangePreset {
        TODAY(Duration.ofDays(1)),
        LAST_7_DAYS(Duration.ofDays(7)),
        LAST_30_DAYS(Duration.ofDays(30)),
        LAST_365_DAYS(Duration.ofDays(365));

        private final Duration duration;

        RelativeDateRangePreset(final Duration duration) {
            this.duration = duration;
        }

        public Duration duration() {
            return duration;
        }

        public ZonedDateTime rangeStart() {
            long days = duration.toDays();
            return LocalDate.now().minusDays(days - 1).atStartOfDay(ZoneOffset.UTC);
        }
    }
}
