package dev.ehutson.template.exception;

import graphql.ErrorType;

public class AuthenticationFailedException extends ApplicationException {
    public AuthenticationFailedException() {
        super("Authentication failed", ErrorCode.AUTHENTICATION_FAILED, ErrorType.ValidationError);
    }

    public AuthenticationFailedException(String message) {
        super(message, ErrorCode.AUTHENTICATION_FAILED, ErrorType.ValidationError);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, ErrorCode.AUTHENTICATION_FAILED, ErrorType.ValidationError, cause);
    }

    public AuthenticationFailedException(String message, Object... args) {
        super(message, ErrorCode.AUTHENTICATION_FAILED, ErrorType.ValidationError, args);
    }

    public AuthenticationFailedException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.AUTHENTICATION_FAILED, ErrorType.ValidationError, cause, args);
    }
}
