package dev.ehutson.template.service.impl;

import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.dto.LocalizedMessage;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.security.service.AuthorizationService;
import dev.ehutson.template.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageServiceImpl implements MessageService {
    private final MessageSource messageSource;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    @Nullable
    private static Locale getLocalFromUserModel(Optional<UserModel> user) {
        return user.map(u -> {
                    String langKey = u.getLangKey();
                    return langKey != null ? Locale.forLanguageTag(langKey) : null;
                })
                .orElse(null);
    }

    /**
     * Gets the translated message for an instance of ApplicationException
     *
     * @param exception ApplicationException
     * @return then translated message
     */
    @Override
    public String getMessage(ApplicationException exception) {
        return getMessage(new LocalizedMessage(
                exception.getCode(),
                exception.getMessageArgs(),
                resolveLocale(),
                null
        ));
    }

    /**
     * Gets the translated message for a particular message code
     *
     * @param code the message code
     * @return the translated message
     */
    @Override
    public String getMessage(ErrorCode code) {
        return getMessage(new LocalizedMessage(code, resolveLocale()));
    }

    /**
     * Gets the translated message for a particular message code
     * and substitues in values for any provided args
     *
     * @param code the message code
     * @param args any arguments to pass to the translator
     * @return the translated message
     */
    @Override
    public String getMessage(ErrorCode code, Object[] args) {
        return getMessage(new LocalizedMessage(code, args, resolveLocale(), null));
    }

    /**
     * Gets the translated message for a particular message code
     * using the provided locale and substitutes in values for any provided args
     *
     * @param code   the message code
     * @param args   any arguments to pass to the translator
     * @param locale The user's locale
     * @return the translated message
     */
    @Override
    public String getMessage(ErrorCode code, Object[] args, Locale locale) {
        return getMessage(new LocalizedMessage(code, args, locale, null));
    }

    /**
     * Gets the translated message using the provided LocalizedMessage object
     *
     * @param message The LocalizedMessage containing all translation details
     * @return the translated message
     */
    public String getMessage(LocalizedMessage message) {
        try {
            return messageSource.getMessage(
                    message.getCode().toString(),
                    message.getArgs(),
                    message.getLocale()
            );
        } catch (NoSuchMessageException e) {
            log.warn("No message found for code: {}", message.getCode());
            return message.getCode().toString(); // Return the code as a fallback
        }
    }

    /**
     * Resolves the user's locale in the following priority:
     * 1. Authenticated user's langKey
     * 2. Request locale from Accept-Language header
     * 3. Default locale (English)
     *
     * @return Locale
     */
    @Override
    public Locale resolveLocale() {
        // Try to get the locale from the authenticated user first
        Locale userLocale = getCurrentUserLocale();
        if (userLocale != null) return userLocale;

        // Fall back to the request locale
        try {
            return getLocaleFromAcceptLanguageHeader();
        } catch (Exception e) {
            return Locale.getDefault();
        }
    }

    private Locale getLocaleFromAcceptLanguageHeader() {
        return Optional.ofNullable(request)
                .map(HttpServletRequest::getLocale)
                .orElse(Locale.getDefault());
    }

    /**
     * Get the locale from the current authenticated user's langKey
     *
     * @return Locale The user's preferred locale
     */
    private Locale getCurrentUserLocale() {
        try {
            Optional<UserModel> user = authorizationService.getCurrentUser();
            if (user.isPresent()) {
                return getLocalFromUserModel(user);
            }
        } catch (Exception e) {
            log.warn("Could not get current user locale", e);
        }

        return null;
    }
}
