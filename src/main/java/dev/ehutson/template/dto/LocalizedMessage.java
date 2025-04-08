package dev.ehutson.template.dto;

import dev.ehutson.template.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Locale;

/**
 * Value object that encapsulates information needed for localized message generation.
 * This class follows the immutability principle from Clean Code and supports
 * the Single Responsibility Pattern by focusing solely on representing message data.
 */
@Getter
@Builder
public class LocalizedMessage {

    /**
     * The error code associated with this message
     */
    @NonNull
    private final ErrorCode code;

    /**
     * Arguments to be used for message placeholders
     */
    private final Object[] args;

    /**
     * The locale for message localization
     */
    @NonNull
    private final Locale locale;

    /**
     * Optional additional context information
     */
    private final String context;

    /**
     * Constructor with required parameters only
     *
     * @param code   The error code for the message
     * @param locale The locale for translation
     */
    public LocalizedMessage(@NonNull ErrorCode code, @NonNull Locale locale) {
        this(code, null, locale, null);
    }

    /**
     * Full constructor with all parameters
     *
     * @param code    The error code for the message
     * @param args    Arguments for message placeholders
     * @param locale  The locale for translation
     * @param context Optional additional context
     */
    public LocalizedMessage(
            @NonNull ErrorCode code,
            Object[] args,
            @NonNull Locale locale,
            String context) {
        this.code = code;
        this.args = args;
        this.locale = locale;
        this.context = context;
    }

    /**
     * Create a new instance with the same values but a different locale
     *
     * @param newLocale The new locale to use
     * @return A new LocalizedMessage with updated locale
     */
    public LocalizedMessage withLocale(@NonNull Locale newLocale) {
        return new LocalizedMessage(this.code, this.args, newLocale, this.context);
    }

    /**
     * Create a new instance with the same values but different arguments
     *
     * @param newArgs The new arguments to use
     * @return A new LocalizedMessage with updated arguments
     */
    public LocalizedMessage withArgs(Object[] newArgs) {
        return new LocalizedMessage(this.code, newArgs, this.locale, this.context);
    }
}