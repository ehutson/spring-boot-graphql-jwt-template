package dev.ehutson.template.exception.graphql.handler.strategy;

import dev.ehutson.template.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Fallback strategy for any unhandled exception.
 * Must have the lowest priority and always return true for canHandle.
 */
@Component
public class FallbackExceptionStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean canHandle(Throwable exception) {
        return true; // Handles everything as fallback
    }

    @Override
    public ErrorCode getErrorCode(Throwable exception) {
        return ErrorCode.SYSTEM_ERROR;
    }

    @Override
    public int getPriority() {
        return 1000; // Lowest priority - this is the safety net
    }
}