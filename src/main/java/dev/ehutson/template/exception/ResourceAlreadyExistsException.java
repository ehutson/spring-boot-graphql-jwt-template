package dev.ehutson.template.exception;

import graphql.ErrorType;

public class ResourceAlreadyExistsException extends ApplicationException {
    public ResourceAlreadyExistsException(String message) {
        super(message, ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorType.ValidationError);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorType.ValidationError, cause);
    }

    public ResourceAlreadyExistsException(String message, Object... args) {
        super(message, ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorType.ValidationError, args);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.RESOURCE_ALREADY_EXISTS, ErrorType.ValidationError, cause, args);
    }
}
