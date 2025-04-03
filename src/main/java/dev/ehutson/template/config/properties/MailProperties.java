package dev.ehutson.template.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    private boolean enabled = false;
    private String from = "";
    private String baseUrl = "";
}
