// src/shared/utils/error-handler.ts
export class AppError extends Error {
    constructor(
        message: string,
        public code: string,
        public statusCode?: number,
        public isOperational = true
    ) {
        super(message);
        Object.setPrototypeOf(this, AppError.prototype);
    }
}

export const errorHandler = {
    handle: (error: unknown): AppError => {
        if (error instanceof AppError) return error;

        if (error instanceof Error) {
            // GraphQL errors
            if ('graphQLErrors' in error) {
                const gqlError = (error as any).graphQLErrors[0];
                return new AppError(
                    gqlError.message,
                    gqlError.extensions?.code || 'GRAPHQL_ERROR',
                    gqlError.extensions?.statusCode
                );
            }

            // Network errors
            if ('networkError' in error) {
                return new AppError(
                    'Network error occurred',
                    'NETWORK_ERROR',
                    500
                );
            }

            return new AppError(error.message, 'UNKNOWN_ERROR');
        }

        return new AppError('An unexpected error occurred', 'UNKNOWN_ERROR');
    },

    getErrorMessage: (error: unknown): string => {
        const appError = errorHandler.handle(error);

        // User-friendly messages
        const errorMessages: Record<string, string> = {
            'UNAUTHENTICATED': 'Please log in to continue',
            'FORBIDDEN': 'You don\'t have permission to perform this action',
            'NETWORK_ERROR': 'Connection error. Please check your internet connection',
            'VALIDATION_ERROR': 'Please check your input and try again',
        };

        return errorMessages[appError.code] || appError.message;
    }
};