package dev.ehutson.template.exception;

import graphql.ErrorType;

public class ServiceErrorException extends ApplicationException {
    public ServiceErrorException() {
        super("Service error", ErrorCode.SERVICE_ERROR, ErrorType.ExecutionAborted);
    }

    public ServiceErrorException(String message) {
        super(message, ErrorCode.SERVICE_ERROR, ErrorType.ExecutionAborted);
    }

    public ServiceErrorException(String message, Throwable cause) {
        super(message, ErrorCode.SERVICE_ERROR, ErrorType.ExecutionAborted, cause);
    }

    public ServiceErrorException(String message, Object... args) {
        super(message, ErrorCode.SERVICE_ERROR, ErrorType.ExecutionAborted, args);
    }

    public ServiceErrorException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.SERVICE_ERROR, ErrorType.ExecutionAborted, cause, args);
    }
}
