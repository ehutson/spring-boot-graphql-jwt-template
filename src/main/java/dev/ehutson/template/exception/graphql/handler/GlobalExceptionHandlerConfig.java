package dev.ehutson.template.exception.graphql.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class GlobalExceptionHandlerConfig {
    private final GlobalExceptionHandlerProperties properties;

    public boolean isStackTraceEnabled() {
        return properties.isStackTraceEnabled();
    }
}
