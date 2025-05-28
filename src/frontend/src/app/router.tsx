import React from 'react';
import {createBrowserRouter, RouterProvider} from 'react-router-dom';
import ProtectedRoute from '@/features/auth/components/ProtectedRoute';
import {ErrorBoundary} from '@/shared/components/error/ErrorBoundary';

// Layouts
import MainLayout from '@/shared/components/layouts/MainLayout.tsx';
import AuthLayout from '@/shared/components/layouts/AuthLayout.tsx';

// Pages
const HomePage = React.lazy(() => import('@/features/home/pages/HomePage.tsx'));
const LoginPage = React.lazy(() => import('@/features/auth/pages/LoginPage.tsx'));
const RegisterPage = React.lazy(() => import('@/features/auth/pages/RegisterPage.tsx'));
const ForgotPasswordPage = React.lazy(() => import('@/features/auth/pages/ForgotPasswordPage.tsx'));
const ResetPasswordPage = React.lazy(() => import('@/features/auth/pages/ResetPasswordPage.tsx'));
const DashboardPage = React.lazy(() => import('@/features/dashboard/pages/DashboardPage.tsx'));
const ProfilePage = React.lazy(() => import('@/features/profile/pages/ProfilePage.tsx'));
const AdminPage = React.lazy(() => import('@/features/admin/pages/AdminPage.tsx'));
const NotFoundPage = React.lazy(() => import('@/shared/pages/NotFoundPage.tsx'));
const UnauthorizedPage = React.lazy(() => import('@/features/auth/pages/UnauthorizedPage.tsx'));

// Loading component for React.lazy
const LoadingFallback = () => (
    <div className="flex items-center justify-center min-h-screen">
        <div
            className="w-16 h-16 border-4 border-blue-500 border-solid rounded-full border-t-transparent animate-spin"></div>
    </div>
);

// Wrapper component for lazy-loaded pages with error boundary
const LazyPage: React.FC<{
    component: React.LazyExoticComponent<React.FC>;
    featureName?: string;
}> = ({component: Component, featureName}) => (
    <ErrorBoundary level="page" featureName={featureName}>
        <React.Suspense fallback={<LoadingFallback/>}>
            <Component/>
        </React.Suspense>
    </ErrorBoundary>
);

const router = createBrowserRouter([
    // Public routes
    {
        element: <MainLayout/>,
        children: [
            {
                path: '/',
                element: <LazyPage component={HomePage} featureName="home"/>,
            },
        ],
    },

    // Auth routes (login, register, etc.)
    {
        element: (
            <ErrorBoundary level="feature" featureName="auth">
                <ProtectedRoute requireAuth={false}/>
            </ErrorBoundary>
        ),
        children: [
            {
                element: <AuthLayout/>,
                children: [
                    {
                        path: '/login',
                        element: <LazyPage component={LoginPage} featureName="login"/>,
                    },
                    {
                        path: '/register',
                        element: <LazyPage component={RegisterPage} featureName="register"/>,
                    },
                    {
                        path: '/forgot-password',
                        element: <LazyPage component={ForgotPasswordPage} featureName="forgot-password"/>,
                    },
                    {
                        path: '/reset-password',
                        element: <LazyPage component={ResetPasswordPage} featureName="reset-password"/>,
                    },
                ],
            },
        ],
    },

// Protected routes (require authentication)
    {
        element: (
            <ErrorBoundary level="feature" featureName="protected">
                <ProtectedRoute/>
            </ErrorBoundary>
        ),
        children: [
            {
                path: '/dashboard',
                element: <LazyPage component={DashboardPage} featureName="dashboard"/>,
            },
            {
                path: '/profile',
                element: <LazyPage component={ProfilePage} featureName="profile"/>,
            },
        ],
    },

// Admin routes
    {
        element: (
            <ErrorBoundary level="feature" featureName="admin">
                <ProtectedRoute requireAdmin={true}/>
            </ErrorBoundary>
        ),
        children: [
            {
                element: <MainLayout/>,
                children: [
                    {
                        path: '/admin',
                        element: <LazyPage component={AdminPage} featureName="admin-dashboard"/>,
                    },
                ],
            },
        ],
    },

// Error/Fallback routes
    {
        path: '/unauthorized',
        element: (
            <React.Suspense fallback={<LoadingFallback/>}>
                <UnauthorizedPage/>
            </React.Suspense>
        ),
    },
    {
        path: '*',
        element: (
            <React.Suspense fallback={<LoadingFallback/>}>
                <NotFoundPage/>
            </React.Suspense>
        ),
    },
]);

export const AppRouter: React.FC = () => {
    return <RouterProvider router={router}/>;
};