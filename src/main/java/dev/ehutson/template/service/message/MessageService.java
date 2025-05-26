package dev.ehutson.template.service.message;

import dev.ehutson.template.dto.LocalizedMessage;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.service.locale.LocaleResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {
    private final MessageSource messageSource;
    private final LocaleResolutionService localeResolutionService;

    /**
     * Gets the translated message for an instance of ApplicationException
     *
     * @param exception ApplicationException
     * @return then translated message
     */
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
     * Resolves the user's locale
     *
     * @return The Locale
     */
    public Locale resolveLocale() {
        return localeResolutionService.resolveLocale();
    }
}
