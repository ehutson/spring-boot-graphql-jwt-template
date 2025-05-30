import {DefaultOptions, InMemoryCache} from "@apollo/client";

export const cacheConfiguration = new InMemoryCache({
    typePolicies: {
        User: {
            fields: {
                roles: {
                    merge: false // Replace roles with the new data instead of merging
                }
            }
        },
        UserConnection: {
            fields: {
                edges: {
                    // For pagination -- append new edges to the existing ones
                    merge(incoming, existing = []) {
                        return [...existing, ...incoming];
                    }
                }
            }
        },
        Query: {
            fields: {
                // Add any specific type policies here if needed
                user: {
                    read(_, {args, toReference}) {
                        // This is an example of how to read a user from the cache
                        return toReference({
                            __typename: 'User',
                            id: args?.id,
                        });
                    }
                }
            },
        },
    },
});

export const defaultCachingOptions: DefaultOptions = {
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
};