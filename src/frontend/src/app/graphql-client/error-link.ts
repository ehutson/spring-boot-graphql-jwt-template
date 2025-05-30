import {onError} from "@apollo/client/link/error";
import {errorTracker} from "@/shared/services/error-tracker.ts";
import {store} from "@/app/store.ts";
import {logout} from "@/features/auth/store/authSlice.ts";


export const errorLink = onError(({graphQLErrors, networkError, operation}) => {
    if (graphQLErrors) {
        graphQLErrors.forEach(({message, locations, path, extensions}) => {
            console.error(
                `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`,
            );

            // Track GraphQL errors
            errorTracker.captureError(
                new Error(`GraphQL Error: ${message}`),
                {
                    level: 'page',
                    feature: 'graphql',
                    metadata: {
                        operation: operation.operationName,
                        variables: operation.variables,
                        locations,
                        path,
                        extensions
                    }
                }
            );

            // TODO:  Make sure that these line up with the error codes in the backend
            // Handle authentication errors - JWT expired or invalid
            if (
                extensions?.code === 'UNAUTHENTICATED' ||
                message.includes('not authenticated') ||
                message.includes('token expired')
            ) {
                // Force logout the user
                store.dispatch(logout());
            }
        });
    }

    if (networkError) {
        console.error(`[Network error]: ${networkError}`);

        // Track network errors
        errorTracker.captureError(
            new Error(`Network Error: ${networkError.message || networkError}`),
            {
                level: 'page',
                feature: 'graphql-network',
                metadata: {
                    operation: operation.operationName,
                    variables: operation.variables,
                    statusCode: 'statusCode' in networkError ? networkError.statusCode : undefined
                }
            }
        );

        // Handle network errors that might be auth related
        if ('statusCode' in networkError && networkError.statusCode === 401) {
            // Force logout the user
            store.dispatch(logout());
        }
    }
});