package dev.ehutson.template.exception;

import graphql.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Resource Errors (1xx)
    RESOURCE_NOT_FOUND(
            "errors.resource.not_found",
            ErrorType.ValidationError,
            "Resource not found"
    ),
    RESOURCE_ALREADY_EXISTS(
            "errors.resource.already_exists",
            ErrorType.ValidationError,
            "Resource already exists"
    ),

    // Authentication errors (2xx)
    AUTHENTICATION_FAILED(
            "errors.authentication.failed",
            ErrorType.ValidationError,
            "Authentication failed"
    ),
    INVALID_CREDENTIALS(
            "errors.authentication.invalid_credentials",
            ErrorType.ValidationError,
            "Invalid username or password"
    ),
    TOKEN_EXPIRED(
            "errors.authentication.token_expired",
            ErrorType.ValidationError,
            "Token expired"
    ),
    INVALID_TOKEN(
            "errors.authentication.invalid_token",
            ErrorType.ValidationError,
            "Token invalid or missing"
    ),

    // Authorization errors (3xx)
    AUTHORIZATION_FAILED(
            "errors.authorization.authorization_failed",
            ErrorType.ValidationError,
            "Authorization failed"
    ),
    INSUFFICIENT_PRIVILEGES(
            "errors.authorization.insufficient_privileges",
            ErrorType.ValidationError,
            "You do not have sufficient privileges to access this resource"
    ),

    // Validation errors (4xx)
    VALIDATION_FAILED(
            "errors.validation.validation_failed",
            ErrorType.ValidationError,
            "Validation failed"
    ),

    // Service errors (5xx)
    SERVICE_ERROR(
            "errors.service.service_error",
            ErrorType.ExecutionAborted,
            "Service error"
    ),
    EXTERNAL_SERVICE_ERROR(
            "errors.service.external_service_error",
            ErrorType.ExecutionAborted,
            "External service error"
    ),

    // System errors (9xx)
    SYSTEM_ERROR(
            "errors.system.system_error",
            ErrorType.ExecutionAborted,
            "System error"
    );

    private final String messageKey;
    private final ErrorType graphQlErrorType;
    private final String defaultMessage;

    public ErrorCategory getCategory() {
        int code = Integer.parseInt(this.name().replaceAll("\\D", "").substring(0, 1));
        return switch (code) {
            case 1 -> ErrorCategory.RESOURCE;
            case 2 -> ErrorCategory.AUTHENTICATION;
            case 3 -> ErrorCategory.AUTHORIZATION;
            case 4 -> ErrorCategory.VALIDATION;
            case 5 -> ErrorCategory.SERVICE;
            default -> ErrorCategory.SYSTEM;
        };
    }

    public enum ErrorCategory {
        RESOURCE, AUTHENTICATION, AUTHORIZATION, VALIDATION, SERVICE, SYSTEM
    }
}
