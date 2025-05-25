package dev.ehutson.template.service.locale;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Configurable locale resolver for testing or forcing specific locales.
 * When configured, has very high priority.
 */
@Component
public class ConfiguredLocaleResolverStrategy implements LocaleResolverStrategy {

    private volatile String configuredLanguageTag;

    /**
     * Sets a specific language tag to force a locale.
     * Useful for testing or administrative overrides.
     */
    public void setLanguageTag(String languageTag) {
        this.configuredLanguageTag = languageTag;
    }

    /**
     * Clears any configured locale.
     */
    public void clearLanguageTag() {
        this.configuredLanguageTag = null;
    }

    @Override
    public Optional<Locale> resolveLocale() {
        if (configuredLanguageTag != null && !configuredLanguageTag.trim().isEmpty()) {
            try {
                return Optional.of(Locale.forLanguageTag(configuredLanguageTag));
            } catch (Exception e) {
                // Invalid language tag
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public int getPriority() {
        return configuredLanguageTag != null ? 5 : 1001; // Very high when configured, very low when not
    }

    @Override
    public String getDescription() {
        return "Configured locale resolver (tag: " + configuredLanguageTag + ")";
    }
}