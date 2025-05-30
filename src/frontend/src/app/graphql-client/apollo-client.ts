import {ApolloClient, from} from "@apollo/client";
import {loadDevMessages, loadErrorMessages} from "@apollo/client/dev";
import {errorLink} from "src/app/graphql-client/error-link.ts";
import env from '@/shared/config/env.ts';
import {cacheConfiguration, defaultCachingOptions} from "@/app/graphql-client/cache-configuration.ts";
import {httpLink} from "@/app/graphql-client/http-link.ts";
import {authLink} from "@/app/graphql-client/auth-link.ts";

if (env.isDevelopment) {
    loadDevMessages();
    loadErrorMessages();
}

export const apolloClient = new ApolloClient({
    link: from([errorLink, authLink, httpLink]),
    cache: cacheConfiguration,
    defaultOptions: defaultCachingOptions,
});