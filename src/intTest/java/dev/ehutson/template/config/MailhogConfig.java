package dev.ehutson.template.config;

import dev.ehutson.template.service.MailhogClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MailhogConfig {

    @Bean
    public MailhogClient mailhogClient(GenericContainer<?> mailhogContainer) {
        return new MailhogClient(getMailhogUiUrl(mailhogContainer));
    }

    private String getMailhogUiUrl(GenericContainer<?> mailhogContainer) {
        return String.format("http://%s:%d", mailhogContainer.getHost(),
                mailhogContainer.getMappedPort(8025));
    }
}
