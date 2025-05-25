package dev.ehutson.template.exception.factory;

import dev.ehutson.template.exception.*;

public class ExceptionFactory {

    private ExceptionFactory() {
        // Utility class
    }

    // Resource exceptions
    public static ResourceNotFoundExceptionBuilder notFound() {
        return new ResourceNotFoundExceptionBuilder();
    }

    public static ResourceAlreadyExistsExceptionBuilder alreadyExists() {
        return new ResourceAlreadyExistsExceptionBuilder();
    }

    // Authentication exceptions
    public static AuthenticationFailedExceptionBuilder authenticationFailed() {
        return new AuthenticationFailedExceptionBuilder();
    }

    public static InvalidCredentialsExceptionBuilder invalidCredentials() {
        return new InvalidCredentialsExceptionBuilder();
    }

    public static InvalidTokenExceptionBuilder invalidToken() {
        return new InvalidTokenExceptionBuilder();
    }

    public static TokenExpiredExceptionBuilder tokenExpired() {
        return new TokenExpiredExceptionBuilder();
    }

    // Authorization exceptions
    public static AuthorizationFailedExceptionBuilder authorizationFailed() {
        return new AuthorizationFailedExceptionBuilder();
    }

    public static InsufficientPrivilegesExceptionBuilder insufficientPrivileges() {
        return new InsufficientPrivilegesExceptionBuilder();
    }

    // Validation exceptions
    public static ValidationFailedExceptionBuilder validationFailed() {
        return new ValidationFailedExceptionBuilder();
    }

    // Service exceptions
    public static ServiceErrorExceptionBuilder serviceError() {
        return new ServiceErrorExceptionBuilder();
    }

    public static ExternalServiceErrorExceptionBuilder externalServiceError() {
        return new ExternalServiceErrorExceptionBuilder();
    }

    // System exceptions
    public static SystemErrorExceptionBuilder systemError() {
        return new SystemErrorExceptionBuilder();
    }

    // Base builder class with common functionality
    public abstract static class ExceptionBuilder<T extends ApplicationException, B extends ExceptionBuilder<T, B>> {
        protected String message;
        protected Throwable cause;
        protected Object[] args;

        @SuppressWarnings("unchecked")
        private B self() {
            return (B) this;
        }

        public B message(String message) {
            this.message = message;
            return self();
        }

        public B cause(Throwable cause) {
            this.cause = cause;
            return self();
        }

        public B args(Object... args) {
            this.args = args;
            return self();
        }

        public abstract T build();

        // Convenience method to build and throw
        public void throwException() {
            throw build();
        }
    }

    // Specific builder classes
    public static class ResourceNotFoundExceptionBuilder extends ExceptionBuilder<ResourceNotFoundException, ResourceNotFoundExceptionBuilder> {
        @Override
        public ResourceNotFoundException build() {
            if (message != null && cause != null && args != null) {
                return new ResourceNotFoundException(message, cause, args);
            } else if (message != null && cause != null) {
                return new ResourceNotFoundException(message, cause);
            } else if (message != null && args != null) {
                return new ResourceNotFoundException(message, args);
            } else if (message != null) {
                return new ResourceNotFoundException(message);
            } else {
                return new ResourceNotFoundException();
            }
        }
    }

    public static class ResourceAlreadyExistsExceptionBuilder extends ExceptionBuilder<ResourceAlreadyExistsException, ResourceAlreadyExistsExceptionBuilder> {
        @Override
        public ResourceAlreadyExistsException build() {
            if (message != null && cause != null && args != null) {
                return new ResourceAlreadyExistsException(message, cause, args);
            } else if (message != null && cause != null) {
                return new ResourceAlreadyExistsException(message, cause);
            } else if (message != null && args != null) {
                return new ResourceAlreadyExistsException(message, args);
            } else if (message != null) {
                return new ResourceAlreadyExistsException(message);
            } else {
                return new ResourceAlreadyExistsException("Resource already exists");
            }
        }
    }

    public static class AuthenticationFailedExceptionBuilder extends ExceptionBuilder<AuthenticationFailedException, AuthenticationFailedExceptionBuilder> {
        @Override
        public AuthenticationFailedException build() {
            if (message != null && cause != null && args != null) {
                return new AuthenticationFailedException(message, cause, args);
            } else if (message != null && cause != null) {
                return new AuthenticationFailedException(message, cause);
            } else if (message != null && args != null) {
                return new AuthenticationFailedException(message, args);
            } else if (message != null) {
                return new AuthenticationFailedException(message);
            } else {
                return new AuthenticationFailedException();
            }
        }
    }

    public static class InvalidCredentialsExceptionBuilder extends ExceptionBuilder<InvalidCredentialsException, InvalidCredentialsExceptionBuilder> {
        @Override
        public InvalidCredentialsException build() {
            if (message != null && cause != null && args != null) {
                return new InvalidCredentialsException(message, cause, args);
            } else if (message != null && cause != null) {
                return new InvalidCredentialsException(message, cause);
            } else if (message != null && args != null) {
                return new InvalidCredentialsException(message, args);
            } else if (message != null) {
                return new InvalidCredentialsException(message);
            } else {
                return new InvalidCredentialsException();
            }
        }
    }

    public static class InvalidTokenExceptionBuilder extends ExceptionBuilder<InvalidTokenException, InvalidTokenExceptionBuilder> {
        @Override
        public InvalidTokenException build() {
            if (message != null && cause != null && args != null) {
                return new InvalidTokenException(message, cause, args);
            } else if (message != null && cause != null) {
                return new InvalidTokenException(message, cause);
            } else if (message != null && args != null) {
                return new InvalidTokenException(message, args);
            } else if (message != null) {
                return new InvalidTokenException(message);
            } else {
                return new InvalidTokenException();
            }
        }
    }

    public static class TokenExpiredExceptionBuilder extends ExceptionBuilder<TokenExpiredException, TokenExpiredExceptionBuilder> {
        @Override
        public TokenExpiredException build() {
            if (message != null && cause != null && args != null) {
                return new TokenExpiredException(message, cause, args);
            } else if (message != null && cause != null) {
                return new TokenExpiredException(message, cause);
            } else if (message != null && args != null) {
                return new TokenExpiredException(message, args);
            } else if (message != null) {
                return new TokenExpiredException(message);
            } else {
                return new TokenExpiredException();
            }
        }
    }

    public static class AuthorizationFailedExceptionBuilder extends ExceptionBuilder<AuthorizationFailedException, AuthorizationFailedExceptionBuilder> {
        @Override
        public AuthorizationFailedException build() {
            if (message != null && cause != null && args != null) {
                return new AuthorizationFailedException(message, cause, args);
            } else if (message != null && cause != null) {
                return new AuthorizationFailedException(message, cause);
            } else if (message != null && args != null) {
                return new AuthorizationFailedException(message, args);
            } else if (message != null) {
                return new AuthorizationFailedException(message);
            } else {
                return new AuthorizationFailedException();
            }
        }
    }

    public static class InsufficientPrivilegesExceptionBuilder extends ExceptionBuilder<InsufficientPrivilegesException, InsufficientPrivilegesExceptionBuilder> {
        @Override
        public InsufficientPrivilegesException build() {
            if (message != null && cause != null && args != null) {
                return new InsufficientPrivilegesException(message, cause, args);
            } else if (message != null && cause != null) {
                return new InsufficientPrivilegesException(message, cause);
            } else if (message != null && args != null) {
                return new InsufficientPrivilegesException(message, args);
            } else if (message != null) {
                return new InsufficientPrivilegesException(message);
            } else {
                return new InsufficientPrivilegesException();
            }
        }
    }

    public static class ValidationFailedExceptionBuilder extends ExceptionBuilder<ValidationFailedException, ValidationFailedExceptionBuilder> {
        @Override
        public ValidationFailedException build() {
            if (message != null && cause != null && args != null) {
                return new ValidationFailedException(message, cause, args);
            } else if (message != null && cause != null) {
                return new ValidationFailedException(message, cause);
            } else if (message != null && args != null) {
                return new ValidationFailedException(message, args);
            } else if (message != null) {
                return new ValidationFailedException(message);
            } else {
                return new ValidationFailedException("Validation failed");
            }
        }
    }

    public static class ServiceErrorExceptionBuilder extends ExceptionBuilder<ServiceErrorException, ServiceErrorExceptionBuilder> {
        @Override
        public ServiceErrorException build() {
            if (message != null && cause != null && args != null) {
                return new ServiceErrorException(message, cause, args);
            } else if (message != null && cause != null) {
                return new ServiceErrorException(message, cause);
            } else if (message != null && args != null) {
                return new ServiceErrorException(message, args);
            } else if (message != null) {
                return new ServiceErrorException(message);
            } else {
                return new ServiceErrorException();
            }
        }
    }

    public static class ExternalServiceErrorExceptionBuilder extends ExceptionBuilder<ExternalServiceErrorException, ExternalServiceErrorExceptionBuilder> {
        @Override
        public ExternalServiceErrorException build() {
            if (message != null && cause != null && args != null) {
                return new ExternalServiceErrorException(message, cause, args);
            } else if (message != null && cause != null) {
                return new ExternalServiceErrorException(message, cause);
            } else if (message != null && args != null) {
                return new ExternalServiceErrorException(message, args);
            } else if (message != null) {
                return new ExternalServiceErrorException(message);
            } else {
                return new ExternalServiceErrorException();
            }
        }
    }

    public static class SystemErrorExceptionBuilder extends ExceptionBuilder<SystemErrorException, SystemErrorExceptionBuilder> {
        @Override
        public SystemErrorException build() {
            if (message != null && cause != null && args != null) {
                return new SystemErrorException(message, cause, args);
            } else if (message != null && cause != null) {
                return new SystemErrorException(message, cause);
            } else if (message != null && args != null) {
                return new SystemErrorException(message, args);
            } else if (message != null) {
                return new SystemErrorException(message);
            } else {
                return new SystemErrorException();
            }
        }
    }
}
