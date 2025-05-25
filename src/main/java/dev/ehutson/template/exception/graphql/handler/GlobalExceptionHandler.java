package dev.ehutson.template.exception.graphql.handler;

import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import dev.ehutson.template.exception.graphql.handler.strategy.ExceptionStrategyResolver;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Clean, simple global exception handler using the strategy pattern.
 * All the complexity is handled by the ExceptionStrategyResolver.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler extends DefaultDataFetcherExceptionHandler {

    private final ExceptionStrategyResolver strategyResolver;

    @NotNull
    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
            @NotNull final DataFetcherExceptionHandlerParameters handlerParameters) {

        Throwable exception = handlerParameters.getException();
        if (exception == null) {
            exception = new IllegalStateException("Exception passed to ExceptionHandler was null");
        }

        try {
            GraphQLError graphqlError = strategyResolver.resolve(exception, handlerParameters);
            log.error("GraphQL exception at path {}: {}", handlerParameters.getPath(), graphqlError.getMessage(), exception);

            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult()
                            .error(graphqlError)
                            .build()
            );

        } catch (Exception e) {
            log.error("Error in exception handling", e);

            // Safe fallback
            GraphQLError fallbackError = GraphQLError.newError()
                    .message("An unexpected error occurred")
                    .path(handlerParameters.getPath())
                    .location(handlerParameters.getSourceLocation())
                    .build();

            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult()
                            .error(fallbackError)
                            .build()
            );
        }
    }
}