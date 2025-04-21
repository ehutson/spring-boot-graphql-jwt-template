import React from 'react';
import {Navigate, Outlet, useLocation} from 'react-router-dom';
import {useAuth} from '@/hooks/useAuth';

interface ProtectedRouteProps {
    requireAuth?: boolean;
    requireAdmin?: boolean;
    redirectTo?: string;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
                                                           requireAuth = true,
                                                           requireAdmin = false,
                                                           redirectTo = '/login',
                                                       }) => {
    const {isAuthenticated, loading, isAdmin} = useAuth();
    const location = useLocation();

    // Show loading state or spinner while checking authentication
    if (loading) {
        return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
    }

    // For routes that require authentication
    if (requireAuth && !isAuthenticated) {
        return <Navigate to={redirectTo} state={{from: location}} replace/>;
    }

    // For routes that require admin access
    if (requireAdmin && !isAdmin) {
        return <Navigate to="/unauthorized" replace/>;
    }

    // For routes that should NOT be accessed when authenticated (like login page)
    if (!requireAuth && isAuthenticated) {
        return <Navigate to="/dashboard" replace/>;
    }

    // If all checks pass, render the child routes
    return <Outlet/>;
};

export default ProtectedRoute;