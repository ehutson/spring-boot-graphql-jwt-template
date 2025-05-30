import {createHttpLink} from "@apollo/client";
import env from "@/shared/config/env.ts";
import fetch from "cross-fetch";

export const httpLink = createHttpLink({
    uri: env.graphqlEndpoint, // Use the GraphQL API URL from the environment config
    credentials: 'include', // Include cookies with requests
    fetch: fetch, // Use cross-fetch for Node.js compatibility
});
