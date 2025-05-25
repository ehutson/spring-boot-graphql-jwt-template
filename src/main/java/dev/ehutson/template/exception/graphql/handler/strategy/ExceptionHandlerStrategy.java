package dev.ehutson.template.exception.graphql.handler.strategy;

import dev.ehutson.template.exception.ErrorCode;

/**
 * Simple strategy interface for handling different types of exceptions.
 * Keep implementations minimal and focused.
 */
public interface ExceptionHandlerStrategy {

    /**
     * Checks if this strategy can handle the given exception.
     */
    boolean canHandle(Throwable exception);

    /**
     * Returns the error code for this exception.
     * The framework handles creating the GraphQLError.
     */
    ErrorCode getErrorCode(Throwable exception);

    /**
     * Returns the priority of this strategy. Lower values have higher priority.
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Optional: customize the error message.
     * If not overridden, uses the localized message for the error code.
     */
    default String getCustomMessage(Throwable exception) {
        return null; // Use default localized message
    }
}