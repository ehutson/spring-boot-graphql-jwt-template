package dev.ehutson.template.service;

import dev.ehutson.template.domain.UserModel;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.security.service.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {
    private final MessageSource messageSource;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;


    /**
     * Gets the translated message for an instance of ApplicationException
     *
     * @param exception ApplicationException
     * @return then translated message
     */
    public String getMessage(ApplicationException exception) {
        return getMessage(exception.getCode(), exception.getMessageArgs());
    }

    /**
     * Gets the translated message for a particular message code
     *
     * @param code the message code
     * @return the translated message
     */
    public String getMessage(ErrorCode code) {
        return getMessage(code, null);
    }

    /**
     * Gets the translated message for a particular message code
     * and substitues in values for any provided args
     *
     * @param code the message code
     * @param args any arguments to pass to the translator
     * @return the translated message
     */
    public String getMessage(ErrorCode code, Object[] args) {
        return getMessage(code, args, resolveLocale());
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
    public String getMessage(ErrorCode code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code.toString(), args, locale);
        } catch (NoSuchMessageException e) {
            return code.toString(); // Return the code as a fallback
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
    public Locale resolveLocale() {
        // Try to get the locale from the authenticated user first
        Locale userLocale = getCurrentUserLocale();
        if (userLocale != null) {
            return userLocale;
        }

        // Fall back to the request locale
        try {
            return Optional.ofNullable(request)
                    .map(HttpServletRequest::getLocale)
                    .orElse(Locale.getDefault());
        } catch (Exception e) {
            return Locale.getDefault();
        }
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
                return user.map(u -> {
                            String langKey = u.getLangKey();
                            return langKey != null ? Locale.forLanguageTag(langKey) : null;
                        })
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not get current user locale", e);
        }

        return null;
    }
}
