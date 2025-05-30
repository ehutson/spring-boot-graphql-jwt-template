import {ApolloProvider} from "@apollo/client";
import {Provider as ReduxProvider} from "react-redux";
import {store} from "@/app/store";
import {apolloClient} from "@/app/graphql-client/apollo-client.ts";
import {AppRouter} from "@/app/router";
import {useTokenRefresh} from "@/features/auth/hooks/useRefreshToken.ts";
import {errorTracker} from "@/shared/services/error-tracker.ts";
import {ErrorBoundary} from "@/shared/components/error/ErrorBoundary.tsx";
import env from '@/shared/config/env';

import './App.css'

// Configure the error tracker on app start
errorTracker.configure({
    endpoint: `${env.apiBaseUrl}/api/errors`,
    maxQueueSize: 100,
    flushInterval: 60000 // 1 minute
});

// Global error handler for unhandled promise rejections
window.addEventListener('unhandledrejection', (event) => {
    errorTracker.captureError(
        new Error(`Unhandled promise rejection: ${event.reason}`),
        {
            level: 'app',
            metadata: {
                reason: event.reason,
                promise: event.promise
            }
        }
    );
});

function AppContent() {
    useTokenRefresh();
    return <AppRouter/>;
}

function App() {
    return (
        <ErrorBoundary level="app">
            <ReduxProvider store={store}>
                <ApolloProvider client={apolloClient}>
                    <AppContent/>
                </ApolloProvider>
            </ReduxProvider>
        </ErrorBoundary>
    )
}

export default App
