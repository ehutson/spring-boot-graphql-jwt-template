package dev.ehutson.template.exception;

import graphql.ErrorType;
import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {
    private final ErrorCode code;
    private final transient Object[] messageArgs;

    protected ApplicationException(ErrorCode code, String message, Throwable cause, Object[] messageArgs) {
        super(message != null ? message : code.getDefaultMessage(), cause);
        this.code = code;
        this.messageArgs = messageArgs;
    }

    public ErrorType getErrorType() {
        return code.getGraphQlErrorType();
    }

    public static ApplicationException of(ErrorCode code) {
        return new ApplicationException(code, null, null, null);
    }

    public static ApplicationException of(ErrorCode code, String message) {
        return new ApplicationException(code, message, null, null);
    }

    public static ApplicationException of(ErrorCode code, Throwable cause) {
        return new ApplicationException(code, null, cause, null);
    }

    public static ApplicationException of(ErrorCode code, String message, Object... args) {
        return new ApplicationException(code, message, null, args);
    }

    public static ApplicationException of(ErrorCode code, String message, Throwable cause, Object... args) {
        return new ApplicationException(code, message, cause, args);
    }

    public static class builder {
        private ErrorCode code;
        private String message;
        private Throwable cause;
        private Object[] args;

        public builder() {
            // Builder class
        }

        public builder code(ErrorCode code) {
            this.code = code;
            return this;
        }

        public builder message(String message) {
            this.message = message;
            return this;
        }

        public builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public builder args(Object... args) {
            this.args = args;
            return this;
        }

        public ApplicationException build() {
            return new ApplicationException(code, message, cause, args);
        }

        public void throwException() {
            throw build();
        }
    }
}
