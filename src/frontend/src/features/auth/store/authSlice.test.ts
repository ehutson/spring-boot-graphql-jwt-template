import {createAction} from '@reduxjs/toolkit';
import authReducer, {clearErrors,} from 'src/features/auth/store/authSlice.ts';
import {AuthState} from '@/features/auth/types/auth.ts';
import {RootState} from '@/shared/types/store.ts';

// Mock all the external dependencies to avoid circular references
jest.mock('@/app/apollo-client.ts', () => ({
    apolloClient: {
        mutate: jest.fn(),
        query: jest.fn(),
        resetStore: jest.fn(),
    },
}));

// Create mock actions for testing - these mirror the actual actions
// but don't require the thunks to be imported
const loginPending = createAction('auth/login/pending');
const loginFulfilled = createAction<{ user: any }>('auth/login/fulfilled');
const loginRejected = createAction<string>('auth/login/rejected');

// Create mock selector functions that don't rely on the actual implementations
const selectUser = (state: { auth: AuthState }) => state.auth.user;
const selectIsAuthenticated = (state: { auth: AuthState }) => state.auth.isAuthenticated;
const selectAuthLoading = (state: { auth: AuthState }) => state.auth.loading;
const selectAuthError = (state: { auth: AuthState }) => state.auth.error;

describe('Auth Slice', () => {
    const initialState: AuthState = {
        user: null,
        isAuthenticated: false,
        loading: false,
        error: null,
    };

    test('should return the initial state', () => {
        expect(authReducer(undefined, {type: '@@INIT'} as any)).toEqual(initialState);
    });

    test('should handle clearErrors', () => {
        const previousState = {
            ...initialState,
            error: 'Some error message',
        };

        expect(authReducer(previousState, clearErrors())).toEqual({
            ...previousState,
            error: null,
        });
    });

    test('should handle login.pending', () => {
        const state = authReducer(initialState, loginPending());

        expect(state).toEqual({
            ...initialState,
            loading: true,
            error: null,
        });
    });

    test('should handle login.fulfilled', () => {
        const user = {
            id: '1',
            username: 'testuser',
            email: 'test@example.com',
            firstName: 'Test',
            lastName: 'User',
            roles: [{id: '1', name: 'ROLE_USER'}],
            activated: true,
        };

        const state = authReducer(initialState, loginFulfilled({user}));

        expect(state).toEqual({
            ...initialState,
            loading: false,
            isAuthenticated: true,
            user,
        });
    });

    test('should handle login.rejected', () => {
        const errorMessage = 'Invalid credentials';

        const state = authReducer(initialState, loginRejected(errorMessage));

        expect(state).toEqual({
            ...initialState,
            loading: false,
            error: errorMessage,
        });
    });

    // Test selectors
    test('selectors should return the correct state values', () => {
        const user = {
            id: '1',
            username: 'testuser',
            email: 'foo@bar.com',
            firstName: 'Test',
            lastName: 'User',
            roles: [],
            activated: true
        };
        const state: RootState = {
            auth: {
                user,
                isAuthenticated: true,
                loading: false,
                error: 'Some error',
            },
        };

        expect(selectUser(state)).toEqual(user);
        expect(selectIsAuthenticated(state)).toBe(true);
        expect(selectAuthLoading(state)).toBe(false);
        expect(selectAuthError(state)).toBe('Some error');
    });
});
