package dev.ehutson.template.exception;

import graphql.ErrorType;

public class ExternalServiceErrorException extends ApplicationException {
    public ExternalServiceErrorException() {
        super("External service error", ErrorCode.EXTERNAL_SERVICE_ERROR, ErrorType.ExecutionAborted);
    }

    public ExternalServiceErrorException(String message) {
        super(message, ErrorCode.EXTERNAL_SERVICE_ERROR, ErrorType.ExecutionAborted);
    }

    public ExternalServiceErrorException(String message, Throwable cause) {
        super(message, ErrorCode.EXTERNAL_SERVICE_ERROR, ErrorType.ExecutionAborted, cause);
    }

    public ExternalServiceErrorException(String message, Object... args) {
        super(message, ErrorCode.EXTERNAL_SERVICE_ERROR, ErrorType.ExecutionAborted, args);
    }

    public ExternalServiceErrorException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.EXTERNAL_SERVICE_ERROR, ErrorType.ExecutionAborted, cause, args);
    }
}
