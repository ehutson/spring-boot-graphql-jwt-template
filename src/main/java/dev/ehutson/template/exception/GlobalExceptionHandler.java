package dev.ehutson.template.exception;

import com.netflix.graphql.dgs.exceptions.DgsException;
import graphql.GraphQLError;
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
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class GlobalExceptionHandler implements DataFetcherExceptionHandler {

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters parameters) {
        Throwable exception = parameters.getException();

        return switch (exception) {
            case CustomException customException -> handleCustomException(customException, parameters);
            case AuthenticationException authenticationException ->
                    handleAuthenticationException(authenticationException, parameters);
            case AccessDeniedException accessDeniedException ->
                    handleAccessDeniedException(accessDeniedException, parameters);
            case DgsException dgsException -> handleDgsException(dgsException, parameters);
            case null, default -> handleGenericException(exception, parameters);
        };
    }

    private CompletableFuture<DataFetcherExceptionHandlerResult> handleCustomException(CustomException exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("Custom exception occurred: {}", exception.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message(exception.getMessage())
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .errorType(exception.getErrorType())
                .extensions(createExtensions("CUSTOM_ERROR", exception.getCode()))
                .build();

        DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();

        return CompletableFuture.completedFuture(result);
    }

    private CompletableFuture<DataFetcherExceptionHandlerResult> handleAuthenticationException(AuthenticationException exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("Authentication exception occurred: {}", exception.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message("Authentication required")
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .extensions(createExtensions("UNAUTHENTICATED", "401"))
                .build();

        DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();

        return CompletableFuture.completedFuture(result);
    }

    private CompletableFuture<DataFetcherExceptionHandlerResult> handleAccessDeniedException(AccessDeniedException exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("Access denied exception occurred: {}", exception.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message("Access denied")
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .extensions(createExtensions("FORBIDDEN", "403"))
                .build();

        DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();

        return CompletableFuture.completedFuture(result);
    }

    private CompletableFuture<DataFetcherExceptionHandlerResult> handleDgsException(DgsException exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("DGS exception occurred: {}", exception.getMessage());

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message(exception.getMessage())
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .extensions(createExtensions("DGS_ERROR", "500"))
                .build();

        DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();

        return CompletableFuture.completedFuture(result);
    }

    private CompletableFuture<DataFetcherExceptionHandlerResult> handleGenericException(Throwable exception, DataFetcherExceptionHandlerParameters parameters) {
        log.error("Unexpected exception occurred", exception);

        GraphQLError error = GraphqlErrorBuilder.newError()
                .message("Internal server error")
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .extensions(createExtensions("INTERNAL_SERVER_ERROR", "500"))
                .build();

        DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build();

        return CompletableFuture.completedFuture(result);
    }

    private Map<String, Object> createExtensions(String errorType, String code) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorType", errorType);
        extensions.put("code", code);
        return extensions;
    }
}
