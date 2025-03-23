package dev.ehutson.template.exception;

import graphql.GraphQLError;
import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.dgs.exceptions.DgsException;
import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GlobalExceptionHandler extends DefaultDataFetcherExceptionHandler {

    //@Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters parameters) {
        Throwable exception = parameters.getException();

        if (exception instanceof CustomException) {
            return handleCustomException((CustomException) exception, parameters);
        } else if (exception instanceof AuthenticationException) {
            return handleAuthenticationException((AuthenticationException) exception, parameters);
        } else if (exception instanceof AccessDeniedException) {
            return handleAccessDeniedException((AccessDeniedException) exception, parameters);
        } else if (exception instanceof DgsException) {
            return handleDgsException((DgsException) exception, parameters);
        } else {
            return handleGenericException(exception, parameters);
        }
    }

    private DataFetcherExceptionHandlerResult handleCustomException(CustomException exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("Custom exception occurred: {}", exception.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message(exception.getMessage())
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .errorType(exception.getErrorType())
                .extensions(createExtensions("CUSTOM_ERROR", exception.getCode()))
                .build();

        return DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();
    }

    private DataFetcherExceptionHandlerResult handleAuthenticationException(AuthenticationException exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("Authentication exception occurred: {}", exception.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message("Authentication required")
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .extensions(createExtensions("UNAUTHENTICATED", "401"))
                .build();

        return DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();
    }

    private DataFetcherExceptionHandlerResult handleAccessDeniedException(AccessDeniedException exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("Access denied exception occurred: {}", exception.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message("Access denied")
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .extensions(createExtensions("FORBIDDEN", "403"))
                .build();

        return DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();
    }

    private DataFetcherExceptionHandlerResult handleDgsException(DgsException exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("DGS exception occurred: {}", exception.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message(exception.getMessage())
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .extensions(createExtensions("DGS_ERROR", "500"))
                .build();

        return DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();
    }

    private DataFetcherExceptionHandlerResult handleGenericException(Throwable exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("Unexpected exception occurred", exception);

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message("Internal server error")
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .extensions(createExtensions("INTERNAL_SERVER_ERROR", "500"))
                .build();

        return DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();
    }

    private Map<String, Object> createExtensions(String errorType, String code) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorType", errorType);
        extensions.put("code", code);
        return extensions;
    }
}
