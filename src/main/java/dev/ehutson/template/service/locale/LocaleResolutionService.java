package dev.ehutson.template.service.locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Simple service that resolves locales using strategy pattern.
 * Handles all the complex logic so strategies can stay simple.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocaleResolutionService {

    private final List<LocaleResolverStrategy> strategies;

    /**
     * Resolves the best locale by trying strategies in priority order.
     * Always returns a locale (fallback to English if all fail).
     */
    public Locale resolveLocale() {
        return strategies.stream()
                .sorted(Comparator.comparingInt(LocaleResolverStrategy::getPriority))
                .map(this::tryResolve)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(Locale.ENGLISH); // Final fallback
    }

    private Optional<Locale> tryResolve(LocaleResolverStrategy strategy) {
        try {
            Optional<Locale> locale = strategy.resolveLocale();
            locale.ifPresent(value -> log.debug("Resolved locale {} using {}", value, strategy.getDescription()));
            return locale;
        } catch (Exception e) {
            log.debug("Strategy {} failed to resolve locale: {}", strategy.getDescription(), e.getMessage());
            return Optional.empty();
        }
    }
}