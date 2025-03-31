package dev.ehutson.template.exception;

public enum ErrorCode {

    // Resource Errors (1xx)
    RESOURCE_NOT_FOUND("errors.resource.not_found"),
    RESOURCE_ALREADY_EXISTS("errors.resource.already_exists"),

    // Authentication errors (2xx)
    AUTHENTICATION_FAILED("errors.authentication.failed"),
    INVALID_CREDENTIALS("errors.authentication.invalid_credentials"),
    TOKEN_EXPIRED("errors.authentication.token_expired"),
    INVALID_TOKEN("errors.authentication.invalid_token"),

    // Authorization errors (3xx)
    AUTHORIZATION_FAILED("errors.authorization.authorization_failed"),
    INSUFFICIENT_PRIVILEGES("errors.authorization.insufficient_privileges"),

    // Validation errors (4xx)
    VALIDATION_FAILED("errors.validation.validation_failed"),

    // Service errors (5xx)
    SERVICE_ERROR("errors.service.service_error"),
    EXTERNAL_SERVICE_ERROR("errors.service.external_service_error"),

    // System errors (9xx)
    SYSTEM_ERROR("errors.system.system_error"),
    ;

    public final String label;

    private ErrorCode(final String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
