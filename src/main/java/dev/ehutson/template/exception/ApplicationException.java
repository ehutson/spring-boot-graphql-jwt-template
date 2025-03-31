package dev.ehutson.template.exception;

import graphql.ErrorType;
import lombok.Getter;

@SuppressWarnings("unused")
@Getter
public class ApplicationException extends RuntimeException {
    private final ErrorCode code;
    private final ErrorType errorType;
    private final transient Object[] messageArgs;

    public ApplicationException(final String message, final ErrorCode code, final ErrorType errorType) {
        super(message);
        this.code = code;
        this.errorType = errorType;
        this.messageArgs = null;
    }

    public ApplicationException(final String message, final ErrorCode code, final ErrorType errorType, final Throwable cause) {
        super(message, cause);
        this.code = code;
        this.errorType = errorType;
        this.messageArgs = null;
    }

    public ApplicationException(final String message, final ErrorCode code, final ErrorType errorType, final Object... args) {
        super(message);
        this.code = code;
        this.errorType = errorType;
        this.messageArgs = args;
    }

    public ApplicationException(final String message, final ErrorCode code, final ErrorType errorType, final Throwable cause, final Object... args) {
        super(message, cause);
        this.code = code;
        this.errorType = errorType;
        this.messageArgs = args;
    }

    public boolean isLocalizable() {
        return true;
    }
}
