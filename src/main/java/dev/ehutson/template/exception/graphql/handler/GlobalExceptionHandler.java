package dev.ehutson.template.exception.graphql.handler;

import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.dgs.exceptions.DgsException;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import dev.ehutson.template.exception.ApplicationException;
import dev.ehutson.template.exception.ErrorCode;
import dev.ehutson.template.service.MessageService;
import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Component
public class GlobalExceptionHandler extends DefaultDataFetcherExceptionHandler {

    private final GlobalExceptionHandlerConfig config;
    private final MessageService messageService;

    @NotNull
    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(@NotNull final DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = handlerParameters.getException();
        if (exception == null) {
            exception = new Throwable("Exception passed to ExceptionHandler was null");
        }

        GraphQLError graphqlError = switch (exception) {
            case ApplicationException customException -> handleApplicationException(customException, handlerParameters);
            case DgsException dgsException -> handleDgsException(dgsException, handlerParameters);
            default -> handleGenericException(exception, handlerParameters);
        };

        logException(handlerParameters, graphqlError, exception);
        return CompletableFuture.completedFuture(DataFetcherExceptionHandlerResult.newResult().error(graphqlError).build());
    }

    private GraphQLError handleApplicationException(final ApplicationException exception, final DataFetcherExceptionHandlerParameters handlerParameters) {
        ErrorCode code = exception.getCode();
        String message = getLocalizedMessage(code, exception);

        return TypedGraphQLError.newBuilder()
                .message(message)
                .path(handlerParameters.getPath())
                .location(handlerParameters.getSourceLocation())
                .errorType(exception.getErrorType())
                .extensions(createExtensions(code, exception))
                .build();
    }

    private GraphQLError handleDgsException(final DgsException exception, final DataFetcherExceptionHandlerParameters handlerParameters) {
        ErrorCode code = ErrorCode.SYSTEM_ERROR;
        String message = getLocalizedMessage(code, exception);

        return TypedGraphQLError.newBuilder()
                .message(message)
                .path(handlerParameters.getPath())
                .location(handlerParameters.getSourceLocation())
                .errorType(exception.getErrorType())
                .extensions(createExtensions(code, exception))
                .build();
    }

    private GraphQLError handleGenericException(final Throwable exception, final DataFetcherExceptionHandlerParameters handlerParameters) {
        ErrorCode code = ErrorCode.SYSTEM_ERROR;
        String message = getLocalizedMessage(code, exception);

        return TypedGraphQLError.newBuilder()
                .message(message)
                .path(handlerParameters.getPath())
                .location(handlerParameters.getSourceLocation())
                .errorType((ErrorClassification) null)
                .extensions(createExtensions(code, exception))
                .build();
    }


    private Map<String, Object> createExtensions(final ErrorCode code, final Throwable throwable) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("code", code.toString());
        if (config.isStackTraceEnabled()) {
            extensions.put("stackTrace", getStackTrace(throwable));
        }
        return extensions;
    }

    private String getStackTrace(final Throwable throwable) {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter, true)
        ) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            return "Unable to get stacktrace for exception.";
        }
    }

    private String getLocalizedMessage(final ErrorCode code, final Throwable exception) {
        try {
            if (exception instanceof ApplicationException applicationException) {
                return messageService.getMessage(code, applicationException.getMessageArgs());
            } else {
                return messageService.getMessage(code);
            }
        } catch (Exception e) {
            // do nothing
        }
        return exception.getMessage();
    }
}
