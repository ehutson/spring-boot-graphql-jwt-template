const env = {
    graphqlEndpoint: import.meta.env.VITE_GRAPHQL_ENDPOINT || 'http://localhost:8097/graphql',
    appName: import.meta.env.VITE_APP_NAME || 'GraphQL Template',
    isDevelopment: import.meta.env.DEV,
    isProduction: import.meta.env.PROD,
} as const;

export default env;