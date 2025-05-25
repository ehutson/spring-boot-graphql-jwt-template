package dev.ehutson.template.service.locale;

import java.util.Locale;
import java.util.Optional;

/**
 * Simple strategy interface for resolving locales from different sources.
 * Strategies only need to identify if they can resolve and provide the locale.
 */
public interface LocaleResolverStrategy {

    /**
     * Attempts to resolve a locale from this strategy's source.
     */
    Optional<Locale> resolveLocale();

    /**
     * Returns the priority of this resolver. Lower values have higher priority.
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Returns a description for debugging.
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
}