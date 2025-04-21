import React from 'react';
import {createBrowserRouter, RouterProvider} from 'react-router-dom';
import ProtectedRoute from '@/components/auth/ProtectedRoute';

// Layouts
import MainLayout from '@/components/layouts/MainLayout';
import AuthLayout from '@/components/layouts/AuthLayout';

// Pages
const HomePage = React.lazy(() => import('@/pages/HomePage'));
const LoginPage = React.lazy(() => import('@/pages/auth/LoginPage'));
const RegisterPage = React.lazy(() => import('@/pages/auth/RegisterPage'));
const ForgotPasswordPage = React.lazy(() => import('@/pages/auth/ForgotPasswordPage'));
const ResetPasswordPage = React.lazy(() => import('@/pages/auth/ResetPasswordPage'));
const DashboardPage = React.lazy(() => import('@/pages/DashboardPage'));
const ProfilePage = React.lazy(() => import('@/pages/ProfilePage'));
const AdminPage = React.lazy(() => import('@/pages/admin/AdminPage'));
const NotFoundPage = React.lazy(() => import('@/pages/NotFoundPage'));
const UnauthorizedPage = React.lazy(() => import('@/pages/UnauthorizedPage'));

// Loading component for React.lazy
const LoadingFallback = () => (
    <div className="flex items-center justify-center min-h-screen">
        <div
            className="w-16 h-16 border-4 border-blue-500 border-solid rounded-full border-t-transparent animate-spin"></div>
    </div>
);

const router = createBrowserRouter([
    // Public routes
    {
        element: <MainLayout/>,
        children: [
            {
                path: '/',
                element: (
                    <React.Suspense fallback={<LoadingFallback/>}>
                        <HomePage/>
                    </React.Suspense>
                ),
            },
        ],
    },

    // Auth routes (login, register, etc.)
    {
        element: <ProtectedRoute requireAuth={false}/>,
        children: [
            {
                element: <AuthLayout/>,
                children: [
                    {
                        path: '/login',
                        element: (
                            <React.Suspense fallback={<LoadingFallback/>}>
                                <LoginPage/>
                            </React.Suspense>
                        ),
                    },
                    {
                        path: '/register',
                        element: (
                            <React.Suspense fallback={<LoadingFallback/>}>
                                <RegisterPage/>
                            </React.Suspense>
                        ),
                    },
                    {
                        path: '/forgot-password',
                        element: (
                            <React.Suspense fallback={<LoadingFallback/>}>
                                <ForgotPasswordPage/>
                            </React.Suspense>
                        ),
                    },
                    {
                        path: '/reset-password',
                        element: (
                            <React.Suspense fallback={<LoadingFallback/>}>
                                <ResetPasswordPage/>
                            </React.Suspense>
                        ),
                    },
                ],
            },
        ],
    },

// Protected routes (require authentication)
    {
        element: <ProtectedRoute/>,
        children: [
            {
                element: <MainLayout/>,
                children: [
                    {
                        path: '/dashboard',
                        element: (
                            <React.Suspense fallback={<LoadingFallback/>}>
                                <DashboardPage/>
                            </React.Suspense>
                        ),
                    },
                    {
                        path: '/profile',
                        element: (
                            <React.Suspense fallback={<LoadingFallback/>}>
                                <ProfilePage/>
                            </React.Suspense>
                        ),
                    },
                ],
            },
        ],
    },

// Admin routes
    {
        element: <ProtectedRoute requireAdmin={true}/>,
        children: [
            {
                element: <MainLayout/>,
                children: [
                    {
                        path: '/admin',
                        element: (
                            <React.Suspense fallback={<LoadingFallback/>}>
                                <AdminPage/>
                            </React.Suspense>
                        ),
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