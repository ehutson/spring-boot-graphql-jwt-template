const env = {
    // API Configuration
    graphqlEndpoint: import.meta.env.FE_GRAPHQL_ENDPOINT || 'http://localhost:8097/graphql',
    apiBaseUrl: import.meta.env.FE_API_BASE_URL || 'http://localhost:8097',

    // Application Configuration
    appName: import.meta.env.FE_APP_NAME || 'GraphQL Template',
    appVersion: import.meta.env.FE_APP_VERSION || '1.0.0',

    // Environment Detection
    isDevelopment: import.meta.env.DEV,
    isProduction: import.meta.env.PROD,
    mode: import.meta.env.MODE,

    // Feature Flags
    enableErrorTracking: import.meta.env.FE_ENABLE_ERROR_TRACKING === 'true',
    enableAnalytics: import.meta.env.FE_ENABLE_ANALYTICS === 'true',

    // Development Configuration
    showDebugInfo: import.meta.env.FE_SHOW_DEBUG_INFO === 'true' && import.meta.env.DEV,
} as const;

// Type safety for environment variables
/*
interface ImportMetaEnv {
    readonly FE_GRAPHQL_ENDPOINT: string;
    readonly FE_API_BASE_URL: string;
    readonly FE_APP_NAME: string;
    readonly FE_APP_VERSION: string;
    readonly FE_ENABLE_ERROR_TRACKING: string;
    readonly FE_ENABLE_ANALYTICS: string;
    readonly FE_SHOW_DEBUG_INFO: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}
*/

// Validation function to ensure required env vars are present
function validateEnvironment() {
    const requiredVars = ['FE_GRAPHQL_ENDPOINT', 'FE_APP_NAME'] as const;

    for (const varName of requiredVars) {
        if (!import.meta.env[varName]) {
            throw new Error(`Missing required environment variable: ${varName}`);
        }
    }
}

// Validate on module load (only in production)
if (import.meta.env.PROD) {
    validateEnvironment();
}

export default env;