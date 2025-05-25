package dev.ehutson.template.exception.graphql.handler.strategy;

import com.netflix.graphql.dgs.exceptions.DgsException;
import dev.ehutson.template.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Handles Netflix DGS framework exceptions.
 * Maps common DGS errors to appropriate error codes.
 */
@Component
public class DgsExceptionStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof DgsException;
    }

    @Override
    public ErrorCode getErrorCode(Throwable exception) {
        String message = exception.getMessage();
        if (message != null) {
            String lower = message.toLowerCase();
            if (lower.contains("not found")) return ErrorCode.RESOURCE_NOT_FOUND;
            if (lower.contains("validation") || lower.contains("invalid")) return ErrorCode.VALIDATION_FAILED;
            if (lower.contains("unauthorized") || lower.contains("forbidden")) return ErrorCode.AUTHORIZATION_FAILED;
        }
        return ErrorCode.SYSTEM_ERROR;
    }

    @Override
    public int getPriority() {
        return 20; // Medium priority
    }
}