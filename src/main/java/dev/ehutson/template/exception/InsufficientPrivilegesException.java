package dev.ehutson.template.exception;

import graphql.ErrorType;

public class InsufficientPrivilegesException extends ApplicationException {
    public InsufficientPrivilegesException() {
        super("You do not have sufficient privileges to access this resource",
                ErrorCode.INSUFFICIENT_PRIVILEGES, ErrorType.ValidationError);
    }

    public InsufficientPrivilegesException(String message) {
        super(message, ErrorCode.INSUFFICIENT_PRIVILEGES, ErrorType.ValidationError);
    }

    public InsufficientPrivilegesException(String message, Throwable cause) {
        super(message, ErrorCode.INSUFFICIENT_PRIVILEGES, ErrorType.ValidationError, cause);
    }

    public InsufficientPrivilegesException(String message, Object... args) {
        super(message, ErrorCode.INSUFFICIENT_PRIVILEGES, ErrorType.ValidationError, args);
    }

    public InsufficientPrivilegesException(String message, Throwable cause, Object... args) {
        super(message, ErrorCode.INSUFFICIENT_PRIVILEGES, ErrorType.ValidationError, cause, args);
    }
}
