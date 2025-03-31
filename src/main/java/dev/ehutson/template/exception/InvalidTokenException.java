package dev.ehutson.template.exception;

import graphql.ErrorType;

public class InvalidTokenException extends ApplicationException {
    public InvalidTokenException() {
        super("Token invalid or missing", ErrorCode.INVALID_TOKEN, ErrorType.ValidationError);
    }

    public InvalidTokenException(String message) {
        super(message, ErrorCode.INVALID_TOKEN, ErrorType.ValidationError);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, ErrorCode.INVALID_TOKEN, ErrorType.ValidationError, cause);
    }

    public InvalidTokenException(String message, Object... args) {
        super(message, ErrorCode.INVALID_TOKEN, ErrorType.ValidationError, args);
    }

    public InvalidTokenException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.INVALID_TOKEN, ErrorType.ValidationError, cause, args);
    }
}
