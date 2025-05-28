import {Component, ErrorInfo, ReactNode} from 'react';
import {errorTracker} from '@/shared/services/error-tracker';
import AppErrorFallback from "@/shared/components/error/AppErrorFallback.tsx";
import FeatureErrorFallback from "@/shared/components/error/FeatureErrorFallback.tsx";
import PageErrorFallback from "@/shared/components/error/PageErrorFallback.tsx";

interface Props {
    children: ReactNode;
    fallback?: ReactNode;
    level?: 'app' | 'feature' | 'page';
    featureName?: string;
    resetKeys?: Array<string | number>;
    onReset?: () => void;
    isolate?: boolean;
}

interface State {
    hasError: boolean;
    error?: Error;
    errorInfo?: ErrorInfo;
    errorCount: number;
}

export class ErrorBoundary extends Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            hasError: false,
            errorCount: 0,
        };
    }

    static getDerivedStateFromError(error: Error): Partial<State> {
        return {
            hasError: true,
            error
        };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        const {level = 'page', featureName} = this.props;

        // Log to console with structured data
        console.error(`[ErrorBoundary-${level}${featureName ? '-' + featureName : ''}]:`, {
            error,
            errorInfo,
            componentStack: errorInfo.componentStack,
            level,
            feature: featureName,
            timestamp: new Date().toISOString(),
            url: window.location.href,
            userAgent: navigator.userAgent
        });

        // Track error (prepared for future backend integration)
        errorTracker.captureError(error, {
            level,
            feature: featureName,
            componentStack: errorInfo.componentStack,
            errorBoundary: true,
            metadata: {
                url: window.location.href,
                timestamp: new Date().toISOString()
            }
        });

        this.setState(prevState => ({
            errorInfo,
            errorCount: prevState.errorCount + 1
        }));
    }

    componentDidUpdate(prevProps: Readonly<Props>) {
        const {resetKeys} = this.props;
        const {hasError} = this.state;

        // Reset the error boundary if resetKeys change
        if (hasError && resetKeys && prevProps.resetKeys !== resetKeys) {
            const hasResetKeyChanged = resetKeys.some(
                (key, index) => key !== prevProps.resetKeys?.[index]
            );

            if (hasResetKeyChanged) {
                this.resetErrorBoundary();
            }
        }
    }

    resetErrorBoundary = () => {
        const {onReset} = this.props;

        // Call onReset callback if provided
        onReset?.();

        // Reset state
        this.setState({
            hasError: false,
            error: undefined,
            errorInfo: undefined,
            errorCount: 0
        });
    }

    render() {
        const {hasError, error, errorCount} = this.state;
        const {children, fallback, level = 'page', isolate = true} = this.props;

        if (hasError) {
            // Use custom fallback if provided
            if (fallback) {
                return fallback;
            }

            // Different UI based on error boundary level
            if (level === 'app') {
                return <AppErrorFallback error={error}/>;
            }

            if (level === 'feature') {
                return <FeatureErrorFallback resetErrorBoundary={this.resetErrorBoundary}/>;
            }

            // Page level error (default)
            return <PageErrorFallback resetErrorBoundary={this.resetErrorBoundary} errorCount={errorCount}/>;
        }

        // If isolate is false and there's an error, don't render the children
        // This prevents cascading errors in development
        if (!isolate && hasError) {
            return null;
        }

        return children;
    }
}