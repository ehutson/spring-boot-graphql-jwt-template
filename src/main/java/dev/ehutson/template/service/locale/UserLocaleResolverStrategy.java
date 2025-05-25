package dev.ehutson.template.service.locale;

import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.security.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Resolves locale from the authenticated user's language preference.
 * Highest priority as it represents explicit user choice.
 */
@Component
@RequiredArgsConstructor
public class UserLocaleResolverStrategy implements LocaleResolverStrategy {

    private final AuthorizationService authorizationService;

    @Override
    public Optional<Locale> resolveLocale() {
        return authorizationService.getCurrentUser()
                .map(UserModel::getLangKey)
                .filter(langKey -> !langKey.trim().isEmpty())
                .map(Locale::forLanguageTag);
    }

    @Override
    public int getPriority() {
        return 10; // Highest priority - user's explicit choice
    }

    @Override
    public String getDescription() {
        return "User preference locale resolver";
    }
}