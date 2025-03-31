package dev.ehutson.template.exception;

import graphql.ErrorType;

public class ValidationFailedException extends ApplicationException {
    public ValidationFailedException(String message) {
        super(message, ErrorCode.VALIDATION_FAILED, ErrorType.ValidationError);
    }

    public ValidationFailedException(String message, Throwable cause) {
        super(message, ErrorCode.VALIDATION_FAILED, ErrorType.ValidationError, cause);
    }

    public ValidationFailedException(String message, Object... args) {
        super(message, ErrorCode.VALIDATION_FAILED, ErrorType.ValidationError, args);
    }

    public ValidationFailedException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.VALIDATION_FAILED, ErrorType.ValidationError, cause, args);
    }
}
