package dev.ehutson.template.config;

import dev.ehutson.template.util.InstantToOffsetDateTimeConverter;
import dev.ehutson.template.util.MongoOffsetDateTimeConverter;
import io.mongock.runner.springboot.EnableMongock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMongock
//EnableElasticsearchRepositories("dev.ehutson.template.repository.search")
public class MongoConfig {
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(InstantToOffsetDateTimeConverter.INSTANCE);
        converters.add(MongoOffsetDateTimeConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }
}
