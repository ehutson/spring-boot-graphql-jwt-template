package dev.ehutson.template.exception;

import graphql.ErrorType;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final String code;
    private final ErrorType errorType;

    public CustomException(String message, String code, ErrorType errorType) {
        super(message);
        this.code = code;
        this.errorType = errorType;
    }

    public CustomException(String message, String code, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.errorType = errorType;
    }

    // Common exception factory methods
    public static CustomException notFound(String entity, String id) {
        return new CustomException(
                entity + " not found with id: " + id,
                "NOT_FOUND",
                ErrorType.DataFetchingException
        );
    }

    public static CustomException badRequest(String message) {
        return new CustomException(
                message,
                "BAD_REQUEST",
                ErrorType.ValidationError
        );
    }

    public static CustomException alreadyExists(String entity, String field, String value) {
        return new CustomException(
                entity + " with " + field + " " + value + " already exists",
                "ALREADY_EXISTS",
                ErrorType.ValidationError
        );
    }

    public static CustomException unauthorized(String message) {
        return new CustomException(
                message,
                "UNAUTHORIZED",
                ErrorType.ValidationError
        );
    }
}
