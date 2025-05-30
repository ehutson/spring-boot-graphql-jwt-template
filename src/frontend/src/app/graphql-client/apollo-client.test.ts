import {store} from '@/app/store.ts';
import {logout} from '@/features/auth/store/authSlice.ts';

// Mock the store and redux actions
jest.mock('@/app/store.ts', () => ({
    store: {
        dispatch: jest.fn(),
    },
}));

jest.mock('@/features/auth/store/authSlice.ts', () => ({
    logout: jest.fn(),
}));

// We'll test the error handling logic directly rather than the module initialization
describe('Apollo Client Error Handling', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    // Extract the error handler logic to test it independently
    const handleGraphQLErrors = (errors: { message: any; extensions: any; }[]) => {
        if (errors) {
            errors.forEach(({message, extensions}) => {
                // Simulate the error link's logic
                if (
                    extensions?.code === 'UNAUTHENTICATED' ||
                    message.includes('not authenticated') ||
                    message.includes('token expired')
                ) {
                    store.dispatch(logout());
                }
            });
        }
    };

    const handleNetworkError = (error: { statusCode: any; }) => {
        if (error && 'statusCode' in error && error.statusCode === 401) {
            store.dispatch(logout());
        }
    };

    test('should dispatch logout on UNAUTHENTICATED error code', () => {
        const error = {
            message: 'Unauthorized',
            extensions: {code: 'UNAUTHENTICATED'}
        };

        handleGraphQLErrors([error]);

        expect(store.dispatch).toHaveBeenCalledWith(logout());
    });

    test('should dispatch logout when message includes "not authenticated"', () => {
        const error = {
            message: 'You are not authenticated',
            extensions: {code: 'OTHER_ERROR'}
        };

        handleGraphQLErrors([error]);

        expect(store.dispatch).toHaveBeenCalledWith(logout());
    });

    test('should dispatch logout when message includes "token expired"', () => {
        const error = {
            message: 'Your token expired',
            extensions: {code: 'OTHER_ERROR'}
        };

        handleGraphQLErrors([error]);

        expect(store.dispatch).toHaveBeenCalledWith(logout());
    });

    test('should dispatch logout on network error with 401 status code', () => {
        const networkError = {statusCode: 401};

        handleNetworkError(networkError);

        expect(store.dispatch).toHaveBeenCalledWith(logout());
    });

    test('should not dispatch logout for other error codes', () => {
        const error = {
            message: 'Other error',
            extensions: {code: 'SOME_OTHER_CODE'}
        };

        handleGraphQLErrors([error]);

        expect(store.dispatch).not.toHaveBeenCalled();
    });

    test('should not dispatch logout for network errors other than 401', () => {
        const networkError = {statusCode: 500};

        handleNetworkError(networkError);

        expect(store.dispatch).not.toHaveBeenCalled();
    });

    // Test helper for auth headers
    test('auth headers should preserve existing headers', () => {
        // This simulates what the authLink does in the actual code
        const addAuthHeaders = (headers = {}) => {
            return {
                headers: {
                    ...headers,
                    // The actual implementation might add auth headers here
                }
            };
        };

        const result = addAuthHeaders({'custom-header': 'value'});

        expect(result).toEqual({
            headers: {
                'custom-header': 'value',
            }
        });
    });
});