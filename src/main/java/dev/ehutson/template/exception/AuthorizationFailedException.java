package dev.ehutson.template.exception;

import graphql.ErrorType;

public class AuthorizationFailedException extends ApplicationException {
    public AuthorizationFailedException() {
        super("Authorization failed", ErrorCode.AUTHORIZATION_FAILED, ErrorType.ValidationError);
    }

    public AuthorizationFailedException(String message) {
        super(message, ErrorCode.AUTHORIZATION_FAILED, ErrorType.ValidationError);
    }

    public AuthorizationFailedException(String message, Throwable cause) {
        super(message, ErrorCode.AUTHORIZATION_FAILED, ErrorType.ValidationError, cause);
    }

    public AuthorizationFailedException(String message, Object... args) {
        super(message, ErrorCode.AUTHORIZATION_FAILED, ErrorType.ValidationError, args);
    }

    public AuthorizationFailedException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.AUTHORIZATION_FAILED, ErrorType.ValidationError, cause, args);
    }
}
