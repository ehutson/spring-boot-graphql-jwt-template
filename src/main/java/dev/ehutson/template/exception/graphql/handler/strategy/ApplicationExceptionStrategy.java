package dev.ehutson.template.exception.graphql.handler.strategy;

import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Handles ApplicationException instances (your business exceptions).
 * Simple and focused - just identifies the exception and extracts the error code.
 */
@Component
public class ApplicationExceptionStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof ApplicationException;
    }

    @Override
    public ErrorCode getErrorCode(Throwable exception) {
        return ((ApplicationException) exception).getCode();
    }

    @Override
    public int getPriority() {
        return 10; // High priority for business exceptions
    }
}