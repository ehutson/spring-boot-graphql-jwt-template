import React from 'react';
import {createBrowserRouter, RouterProvider} from 'react-router-dom';
import ProtectedRoute from '@/features/auth/components/ProtectedRoute.tsx';

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