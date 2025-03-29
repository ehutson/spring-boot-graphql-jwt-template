package dev.ehutson.template;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;

/**
 * Helper class for running the application in test mode
 * Not an actual application class, just a launcher
 */
@Configuration
public class TestTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.from(TemplateApplication::main).with(TestContainersConfiguration.class).run(args);
    }

}
