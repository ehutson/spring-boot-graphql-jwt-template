package dev.ehutson.template.config;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMongock
//EnableElasticsearchRepositories("dev.ehutson.template.repository.search")
public class MongoConfig {
}
