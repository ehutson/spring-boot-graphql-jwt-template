package dev.ehutson.template.exception;

import graphql.ErrorType;

public class SystemErrorException extends ApplicationException {
    public SystemErrorException() {
        super("System error", ErrorCode.SYSTEM_ERROR, ErrorType.ExecutionAborted);
    }

    public SystemErrorException(String message) {
        super(message, ErrorCode.SYSTEM_ERROR, ErrorType.ExecutionAborted);
    }

    public SystemErrorException(String message, Throwable cause) {
        super(message, ErrorCode.SYSTEM_ERROR, ErrorType.ExecutionAborted, cause);
    }

    public SystemErrorException(String message, Object... args) {
        super(message, ErrorCode.SYSTEM_ERROR, ErrorType.ExecutionAborted, args);
    }

    public SystemErrorException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.SYSTEM_ERROR, ErrorType.ExecutionAborted, cause, args);
    }
}
