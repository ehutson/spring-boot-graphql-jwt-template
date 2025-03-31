package dev.ehutson.template.exception;

import graphql.ErrorType;

public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException() {
        super("", ErrorCode.RESOURCE_NOT_FOUND, ErrorType.ValidationError);
    }

    public ResourceNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, ErrorType.ValidationError);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, ErrorType.ValidationError, cause);
    }

    public ResourceNotFoundException(String message, Object... args) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, ErrorType.ValidationError, args);
    }

    public ResourceNotFoundException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, ErrorType.ValidationError, cause, args);
    }
}
