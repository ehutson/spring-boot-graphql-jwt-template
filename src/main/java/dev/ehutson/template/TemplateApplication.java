package dev.ehutson.template;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Collections;
import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan("dev.ehutson.template")
public class TemplateApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TemplateApplication.class);

        // Set default profile if not specified
        if (System.getProperty("spring.profiles.active") == null &&
                System.getenv("SPRING_PROFILES_ACTIVE") == null) {
            app.setDefaultProperties(Collections.singletonMap("spring.profiles.active", "dev"));
        }

        app.run(args);
    }

}
