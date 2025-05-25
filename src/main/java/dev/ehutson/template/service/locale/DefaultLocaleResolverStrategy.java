package dev.ehutson.template.service.locale;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Fallback locale resolver that returns the system default locale.
 * Always succeeds, so it should have low priority.
 */
@Component
public class DefaultLocaleResolverStrategy implements LocaleResolverStrategy {

    @Override
    public Optional<Locale> resolveLocale() {
        return Optional.of(Locale.getDefault());
    }

    @Override
    public int getPriority() {
        return 1000; // Lowest priority - fallback
    }

    @Override
    public String getDescription() {
        return "System default locale resolver";
    }
}