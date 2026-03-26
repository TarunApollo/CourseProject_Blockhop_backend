package ch.usi.inf.bsc.sa4.lab02spring.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ch.usi.inf.bsc.sa4.lab02spring.utils.DateRangePreset;

@Component
public class DateRangePresetConverter implements Converter<String, DateRangePreset> {
    @Override
    public DateRangePreset convert(String source) {
        return DateRangePreset.fromValue(source);
    }
}
