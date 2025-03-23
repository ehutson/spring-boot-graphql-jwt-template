package dev.ehutson.template.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class InstantToOffsetDateTimeConverter implements Converter<Instant, OffsetDateTime> {

    public static final InstantToOffsetDateTimeConverter INSTANCE = new InstantToOffsetDateTimeConverter();

    @Override
    public OffsetDateTime convert(Instant source) {
        return source != null ? source.atOffset(ZoneOffset.UTC) : null;
    }
}
