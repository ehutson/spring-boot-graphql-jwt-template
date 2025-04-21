import {useDispatch, useSelector} from "react-redux";
import {useCallback, useEffect} from "react";
import {AppDispatch} from "@/types/store";
import {
    clearErrors,
    getCurrentUser,
    login,
    logout,
    register,
    selectAuthError,
    selectAuthLoading,
    selectIsAuthenticated,
    selectUser
} from "@/features/auth/authSlice";

export const useAuth = () => {
    const dispatch = useDispatch<AppDispatch>();
    //const auth = useSelector(selectAuth);
    const user = useSelector(selectUser);
    const isAuthenticated = useSelector(selectIsAuthenticated);
    const loading = useSelector(selectAuthLoading);
    const error = useSelector(selectAuthError);

    // Check authentication status when hook is first used
    useEffect(() => {
        if (!isAuthenticated && !loading && !error) {
            dispatch(getCurrentUser());
        }
    }, [dispatch, isAuthenticated, loading, error]);

    const loginUser = useCallback(
        (username: string, password: string) => {
            return dispatch(login({username, password}));
        }, [dispatch]
    );

    const registerUser = useCallback(
        (userData: {
            username: string;
            email: string;
            password: string;
            firstName: string;
            lastName: string;
            langKey?: string;
            timezone?: string;

        }) => {
            return dispatch(register(userData));
        }, [dispatch]
    );

    const logoutUser = useCallback(() => {
        dispatch(logout());
    }, [dispatch]);

    const clearAuthErrors = useCallback(() => {
        dispatch(clearErrors());
    }, [dispatch]);

    // Check if user has a specific role
    const hasRole = useCallback((roleName: string) => {
        if (!user?.roles) return false;
        return user.roles.some((role => role.name === roleName));
    }, [user]);

    return {
        user,
        isAuthenticated,
        loading,
        error,
        login: loginUser,
        register: registerUser,
        logout: logoutUser,
        clearErrors: clearAuthErrors,
        hasRole,
        isAdmin: hasRole('ROLE_ADMIN'),
    };
};