package dev.ehutson.template.exception.graphql.handler.strategy;

import dev.ehutson.template.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Handles validation exceptions from Bean Validation or Spring.
 */
@Component
public class ValidationExceptionStrategy implements ExceptionHandlerStrategy {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof jakarta.validation.ConstraintViolationException ||
                exception instanceof org.springframework.web.bind.MethodArgumentNotValidException;
    }

    @Override
    public ErrorCode getErrorCode(Throwable exception) {
        return ErrorCode.VALIDATION_FAILED;
    }

    @Override
    public String getCustomMessage(Throwable exception) {
        if (exception instanceof jakarta.validation.ConstraintViolationException cve) {
            return "Validation failed: " + cve.getConstraintViolations().iterator().next().getMessage();
        }
        return "Validation failed";
    }

    @Override
    public int getPriority() {
        return 15; // High priority for validation
    }
}