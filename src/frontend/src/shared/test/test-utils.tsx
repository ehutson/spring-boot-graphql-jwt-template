// src/utils/test-utils.tsx
import React, { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { BrowserRouter } from 'react-router-dom';
import { ApolloProvider } from '@apollo/client';
//import authReducer from '@/features/auth/authSlice';
import { apolloClient } from '@/app/apollo-client.ts';

// Create a custom renderer that includes providers
interface ExtendedRenderOptions extends Omit<RenderOptions, 'queries'> {
    preloadedState?: any;
    store?: any;
    route?: string;
}

export function renderWithProviders(
    ui: ReactElement,
    {
        preloadedState = {},
        store = configureStore({
            reducer: {
                //auth: authReducer,
                // Add other reducers as needed
            },
            preloadedState,
        }),
        route = '/',
        ...renderOptions
    }: ExtendedRenderOptions = {}
) {
    // Set the URL before rendering
    window.history.pushState({}, 'Test page', route);

    function Wrapper({ children }: Readonly<{ children: React.ReactNode }>) {
        return (
            <Provider store={store}>
                <ApolloProvider client={apolloClient}>
                    <BrowserRouter>{children}</BrowserRouter>
                </ApolloProvider>
            </Provider>
        );
    }

    return {
        store,
        ...render(ui, { wrapper: Wrapper, ...renderOptions }),
    };
}

// Create mock data for tests
export const mockUser = {
    id: 'user-1',
    username: 'testuser',
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'User',
    roles: [{ id: 'role-1', name: 'ROLE_USER' }],
    activated: true,
};

export const mockAdminUser = {
    id: 'admin-1',
    username: 'adminuser',
    email: 'admin@example.com',
    firstName: 'Admin',
    lastName: 'User',
    roles: [
        { id: 'role-1', name: 'ROLE_USER' },
        { id: 'role-2', name: 'ROLE_ADMIN' },
    ],
    activated: true,
};

// Mock the apollo client for tests
jest.mock('@/app/apollo-client.ts', () => ({
    apolloClient: {
        mutate: jest.fn(),
        query: jest.fn(),
        resetStore: jest.fn(),
    },
}));