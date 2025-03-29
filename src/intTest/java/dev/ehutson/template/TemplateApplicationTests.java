package dev.ehutson.template;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TemplateApplication.class)
@Import({TestContainersConfiguration.class, IntTestConfiguration.class})
@ActiveProfiles("test")
class TemplateApplicationTests {

    @Test
    void contextLoads() {
    }

}
