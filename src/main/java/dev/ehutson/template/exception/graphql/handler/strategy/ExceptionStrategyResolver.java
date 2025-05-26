package dev.ehutson.template.exception.graphql.handler.strategy;

import com.netflix.graphql.types.errors.TypedGraphQLError;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.exception.graphql.handler.GlobalExceptionHandlerConfig;
import dev.ehutson.template.service.message.MessageService;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple resolver that handles all the common GraphQL error creation logic.
 * Strategies just need to identify exceptions and provide error codes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionStrategyResolver {

    private final List<ExceptionHandlerStrategy> strategies;
    private final MessageService messageService;
    private final GlobalExceptionHandlerConfig config;

    /**
     * Resolves exception to GraphQLError using the strategy pattern.
     * Handles all the boilerplate so strategies can stay simple.
     */
    public GraphQLError resolve(Throwable exception, DataFetcherExceptionHandlerParameters parameters) {
        // Find the strategy
        ExceptionHandlerStrategy strategy = findStrategy(exception);

        // Get error code from strategy
        ErrorCode errorCode = strategy.getErrorCode(exception);

        // Get message (custom or localized)
        String message = getErrorMessage(strategy, exception, errorCode);

        // Create the GraphQL error with all the boilerplate handled here
        return TypedGraphQLError.newBuilder()
                .message(message)
                .path(parameters.getPath())
                .location(parameters.getSourceLocation())
                .errorType(getErrorType(exception))
                .extensions(createExtensions(errorCode, exception))
                .build();
    }

    private ExceptionHandlerStrategy findStrategy(Throwable exception) {
        return strategies.stream()
                .sorted(Comparator.comparingInt(ExceptionHandlerStrategy::getPriority))
                .filter(strategy -> strategy.canHandle(exception))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No strategy found for: " + exception.getClass().getName()));
    }

    private String getErrorMessage(ExceptionHandlerStrategy strategy, Throwable exception, ErrorCode errorCode) {
        // Try custom message first
        String customMessage = strategy.getCustomMessage(exception);
        if (customMessage != null) {
            return customMessage;
        }

        // Use localized message for error code
        try {
            if (exception instanceof ApplicationException appEx) {
                return messageService.getMessage(appEx);
            }
            return messageService.getMessage(errorCode);
        } catch (Exception e) {
            log.warn("Failed to get localized message for {}", errorCode, e);
            return exception.getMessage() != null ? exception.getMessage() : "An error occurred";
        }
    }

    private graphql.ErrorType getErrorType(Throwable exception) {
        if (exception instanceof ApplicationException appEx) {
            return appEx.getErrorType();
        }
        return graphql.ErrorType.DataFetchingException;
    }

    private Map<String, Object> createExtensions(ErrorCode errorCode, Throwable exception) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("code", errorCode.toString());

        if (config.isStackTraceEnabled()) {
            extensions.put("stackTrace", getStackTrace(exception));
        }

        return extensions;
    }

    private String getStackTrace(Throwable exception) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            exception.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Could not generate stack trace";
        }
    }
}