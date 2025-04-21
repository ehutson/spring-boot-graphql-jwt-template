// src/hooks/useAuth.test.ts
import { renderHook, act } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { useAuth } from './useAuth';
import { AuthState } from '@/types/auth';

// Mock the Redux actions
jest.mock('@/features/auth/authSlice', () => ({
    // Actions
    login: jest.fn(() => ({ type: 'auth/login/mock' })),
    logout: jest.fn(() => ({ type: 'auth/logout/mock' })),
    register: jest.fn(() => ({ type: 'auth/register/mock' })),
    getCurrentUser: jest.fn(() => ({ type: 'auth/getCurrentUser/mock' })),
    clearErrors: jest.fn(() => ({ type: 'auth/clearErrors' })),

    // Selectors
    selectUser: (state: { auth: { user: any; }; }) => state.auth.user,
    selectIsAuthenticated: (state: { auth: { isAuthenticated: any; }; }) => state.auth.isAuthenticated,
    selectAuthLoading: (state: { auth: { loading: any; }; }) => state.auth.loading,
    selectAuthError: (state: { auth: { error: any; }; }) => state.auth.error,

    // Default export (reducer)
    __esModule: true,
    default: jest.fn((state = {
        user: null,
        isAuthenticated: false,
        loading: false,
        error: null
    }, action) => {
        // Simple mock reducer
        switch (action.type) {
            case 'auth/login/mock':
                return { ...state, isAuthenticated: true };
            case 'auth/logout/mock':
                return { ...state, isAuthenticated: false, user: null };
            case 'auth/clearErrors':
                return { ...state, error: null };
            default:
                return state;
        }
    }),
}));

describe('useAuth Hook', () => {
    // Get the mocked functions
    const { login, logout, getCurrentUser, clearErrors } = jest.requireMock('@/features/auth/authSlice');

    // Create a test store for the hook
    const createTestStore = (initialState: Partial<AuthState> = {}) => {
        return configureStore({
            reducer: {
                auth: jest.requireMock('@/features/auth/authSlice').default
            },
            preloadedState: {
                auth: {
                    user: null,
                    isAuthenticated: false,
                    loading: false,
                    error: null,
                    ...initialState
                }
            }
        });
    };

    // Setup function to render the hook with Redux Provider
    const renderAuthHook = (initialState: Partial<AuthState> = {}) => {
        const store = createTestStore(initialState);

        return {
            ...renderHook(() => useAuth(), {
                wrapper: ({ children }) => (
                    <Provider store={store}>{children}</Provider>
                )
            }),
            store
        };
    };

    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('should call getCurrentUser when not authenticated', () => {
        renderAuthHook();
        expect(getCurrentUser).toHaveBeenCalled();
    });

    test('should not call getCurrentUser when authenticated', () => {
        renderAuthHook({
            isAuthenticated: true,
            user: { id: '1', username: 'test', roles: [], email: '', firstName: '', lastName: '', activated: true }
        });

        expect(getCurrentUser).not.toHaveBeenCalled();
    });

    test('should call login with correct params', async () => {
        login.mockReturnValue({ meta: { requestStatus: 'fulfilled' } });

        const { result } = renderAuthHook();

        await act(async () => {
            await result.current.login('testuser', 'password');
        });

        expect(login).toHaveBeenCalledWith({ username: 'testuser', password: 'password' });
    });

    test('should call logout', () => {
        const { result } = renderAuthHook();

        act(() => {
            result.current.logout();
        });

        expect(logout).toHaveBeenCalled();
    });

    test('should call clearErrors', () => {
        const { result } = renderAuthHook({ error: 'Some error' });

        act(() => {
            result.current.clearErrors();
        });

        expect(clearErrors).toHaveBeenCalled();
    });

    test('should check if user has role correctly', () => {
        const user = {
            id: '1',
            username: 'test',
            roles: [{ id: '1', name: 'ROLE_USER' }, { id: '2', name: 'ROLE_ADMIN' }],
            email: '',
            firstName: '',
            lastName: '',
            activated: true
        };

        const { result } = renderAuthHook({ user });

        expect(result.current.hasRole('ROLE_ADMIN')).toBe(true);
        expect(result.current.hasRole('ROLE_MODERATOR')).toBe(false);
        expect(result.current.isAdmin).toBe(true);
    });
});