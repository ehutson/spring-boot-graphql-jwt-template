package dev.ehutson.template.exception;

import graphql.ErrorType;

public class TokenExpiredException extends ApplicationException {
    public TokenExpiredException() {
        super("Token expired", ErrorCode.TOKEN_EXPIRED, ErrorType.ValidationError);
    }

    public TokenExpiredException(String message) {
        super(message, ErrorCode.TOKEN_EXPIRED, ErrorType.ValidationError);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, ErrorCode.TOKEN_EXPIRED, ErrorType.ValidationError, cause);
    }

    public TokenExpiredException(String message, Object... args) {
        super(message, ErrorCode.TOKEN_EXPIRED, ErrorType.ValidationError, args);
    }

    public TokenExpiredException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.TOKEN_EXPIRED, ErrorType.ValidationError, cause, args);
    }
}
