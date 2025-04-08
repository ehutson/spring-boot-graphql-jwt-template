package dev.ehutson.template.service;

import dev.ehutson.template.dto.LocalizedMessage;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;

import java.util.Locale;

/**
 * Interface for message services that handle localized messages.
 * Following the Interface Segregation Principle from SOLID.
 */
public interface MessageService {

    /**
     * Gets the translated message for an ApplicationException
     *
     * @param exception The exception containing message details
     * @return The localized message
     */
    String getMessage(ApplicationException exception);

    /**
     * Gets the translated message for an error code
     *
     * @param code The error code
     * @return The localized message
     */
    String getMessage(ErrorCode code);

    /**
     * Gets the translated message with parameters
     *
     * @param code The error code
     * @param args Parameters for the message template
     * @return The localized message
     */
    String getMessage(ErrorCode code, Object[] args);

    /**
     * Gets the translated message with specified locale
     *
     * @param code   The error code
     * @param args   Parameters for the message template
     * @param locale The locale to use
     * @return The localized message
     */
    String getMessage(ErrorCode code, Object[] args, Locale locale);

    /**
     * Gets the translated message using a LocalizedMessage object
     *
     * @param message The message details
     * @return The localized message
     */
    String getMessage(LocalizedMessage message);

    /**
     * Resolves the appropriate locale for the current context
     *
     * @return The resolved locale
     */
    Locale resolveLocale();
}