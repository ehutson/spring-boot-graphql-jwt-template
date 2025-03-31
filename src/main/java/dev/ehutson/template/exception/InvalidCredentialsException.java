package dev.ehutson.template.exception;

import graphql.ErrorType;

public class InvalidCredentialsException extends ApplicationException {
    public InvalidCredentialsException() {
        super("Invalid username or password", ErrorCode.INVALID_CREDENTIALS, ErrorType.ValidationError);
    }

    public InvalidCredentialsException(String message) {
        super(message, ErrorCode.INVALID_CREDENTIALS, ErrorType.ValidationError);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, ErrorCode.INVALID_CREDENTIALS, ErrorType.ValidationError, cause);
    }

    public InvalidCredentialsException(String message, Object... args) {
        super(message, ErrorCode.INVALID_CREDENTIALS, ErrorType.ValidationError, args);
    }

    public InvalidCredentialsException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.INVALID_CREDENTIALS, ErrorType.ValidationError, cause, args);
    }
}
