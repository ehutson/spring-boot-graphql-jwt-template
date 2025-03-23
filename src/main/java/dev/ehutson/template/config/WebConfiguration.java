package dev.ehutson.template.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * This class directs all URIs to static/index.html except for graphql and graphiql.
 * Why? Because react router is using URIs like "/foo/bar".  The default behavior is to
 * try to resolve /foo/bar and when it can't find it, the server returns a 404.  We want
 * the server to serve up index.html and let react router parse the URL to determine which
 * page to show.  That's what this does.
 */

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NotNull String resourcePath, @NotNull Resource location) throws IOException {
                        if (resourcePath.startsWith("graphql") || resourcePath.startsWith("graphiql")) {
                            return null;
                        }
                        Resource resource = location.createRelative(resourcePath);
                        return resource.exists() && resource.isReadable() ? resource : new ClassPathResource("/static/index.html");
                    }
                });
    }
}