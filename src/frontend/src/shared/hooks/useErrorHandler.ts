// src/shared/hooks/useErrorHandler.ts
import {useCallback} from 'react';
import {useLocation} from 'react-router-dom';
import {errorTracker} from '@/shared/services/error-tracker';
import {toast} from 'sonner';

interface ErrorHandlerOptions {
    showToast?: boolean;
    toastMessage?: string;
    level?: 'page' | 'feature';
    feature?: string;
    rethrow?: boolean;
}

export function useErrorHandler() {
    const location = useLocation();

    const handleError = useCallback((
        error: Error | unknown,
        options: ErrorHandlerOptions = {}
    ) => {
        const {
            showToast = true,
            toastMessage,
            level = 'page',
            feature,
            rethrow = false
        } = options;

        // Convert unknown errors to Error objects
        const errorObj = error instanceof Error
            ? error
            : new Error(String(error));

        // Track the error
        errorTracker.captureError(errorObj, {
            level,
            feature: feature ?? location.pathname,
            metadata: {
                route: location.pathname,
                showedToast: showToast
            }
        });

        // Show user-friendly toast if requested
        if (showToast) {
            const message = toastMessage ?? getErrorMessage(errorObj);
            toast.error(message);
        }

        // Rethrow if requested (useful for error boundaries)
        if (rethrow) {
            throw errorObj;
        }
    }, [location]);

    const handleAsyncError = useCallback(async <T, >(
        asyncFn: () => Promise<T>,
        options: ErrorHandlerOptions = {}
    ): Promise<T | undefined> => {
        try {
            return await asyncFn();
        } catch (error) {
            handleError(error, options);
            return undefined;
        }
    }, [handleError]);

    return {
        handleError,
        handleAsyncError
    };
}

// Helper function to get user-friendly error messages
function getErrorMessage(error: Error): string {
    // Check for known error types
    if (error.message.includes('Network')) {
        return 'Network error. Please check your connection.';
    }

    if (error.message.includes('401') || error.message.includes('Unauthorized')) {
        return 'Authentication error. Please log in again.';
    }

    if (error.message.includes('403') || error.message.includes('Forbidden')) {
        return 'You don\'t have permission to perform this action.';
    }

    if (error.message.includes('404') || error.message.includes('Not found')) {
        return 'The requested resource was not found.';
    }

    if (error.message.includes('500') || error.message.includes('Server error')) {
        return 'Server error. Please try again later.';
    }

    // Default message
    return 'An unexpected error occurred. Please try again.';
}