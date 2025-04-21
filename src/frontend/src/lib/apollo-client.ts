import {ApolloClient, createHttpLink, from, InMemoryCache} from "@apollo/client";
import {onError} from "@apollo/client/link/error";
import {setContext} from "@apollo/client/link/context";
import {store} from "@/app/store";
import {logout} from "@/features/auth/authSlice";
import {loadDevMessages, loadErrorMessages} from "@apollo/client/dev";
import fetch from "cross-fetch";

if (process.env.NODE_ENV !== 'production') {
    loadDevMessages();
    loadErrorMessages();
}

const httpLink = createHttpLink({
    uri: "http://localhost:8097/graphql",
    credentials: 'include', // Include cookies with requests
    fetch: fetch, // Use cross-fetch for Node.js compatibility
});

// Error handling link
const errorLink = onError(({graphQLErrors, networkError}) => {
    if (graphQLErrors) {
        graphQLErrors.forEach(({message, locations, path, extensions}) => {
            console.error(
                `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`,
            );

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

        // Handle network errors that might be auth related
        if ('statusCode' in networkError && networkError.statusCode === 401) {
            // Force logout the user
            store.dispatch(logout());
        }
    }
});

// Auth link to handle authentication
const authLink = setContext((_, {headers}) => {
    // We don't need to set the token here, as it is already set in the cookie
    // The browser will automatically include the cookie in the request
    return {
        headers: {
            ...headers,
        },
    };
});

// Create the Apollo Client
export const apolloClient = new ApolloClient({
    link: from([errorLink, authLink, httpLink]),
    cache: new InMemoryCache(),
    defaultOptions: {
        watchQuery: {
            fetchPolicy: 'network-only',
            errorPolicy: 'all',
        },
        query: {
            fetchPolicy: 'network-only',
            errorPolicy: 'all',
        },
        mutate: {
            errorPolicy: 'all',
        },
    },
});