package dev.ehutson.template.util;


import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;

@Component
public class MongoOffsetDateTimeConverter implements Converter<OffsetDateTime, Instant> {

    public static final MongoOffsetDateTimeConverter INSTANCE = new MongoOffsetDateTimeConverter();

    @Override
    public Instant convert(OffsetDateTime source) {
        return source != null ? source.toInstant() : null;
    }
}
