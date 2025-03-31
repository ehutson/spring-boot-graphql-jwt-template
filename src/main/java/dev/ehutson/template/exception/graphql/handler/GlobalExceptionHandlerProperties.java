package dev.ehutson.template.exception.graphql.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.exception")
public class GlobalExceptionHandlerProperties {
    private boolean stackTraceEnabled = false;
}
