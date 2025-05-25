package dev.ehutson.template.service.locale;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.Optional;

/**
 * Resolves locale from HTTP Accept-Language header.
 * Only available in web applications and when request context is available.
 */
@Component
@ConditionalOnWebApplication
public class HttpHeaderLocaleResolverStrategy implements LocaleResolverStrategy {

    @Override
    public Optional<Locale> resolveLocale() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Locale locale = attributes.getRequest().getLocale();
                return Optional.ofNullable(locale);
            }
        } catch (Exception e) {
            // Ignore — request context not available (non-web context, async operations, etc.)
        }
        return Optional.empty();
    }

    @Override
    public int getPriority() {
        return 20; // Medium priority - browser preference
    }

    @Override
    public String getDescription() {
        return "HTTP Accept-Language header locale resolver";
    }
}