import {createAsyncThunk, createSlice, PayloadAction} from "@reduxjs/toolkit";
import {apolloClient} from "@/app/apollo-client.ts";
import {RootState} from "@/shared/types/store.ts";
import {AuthState, User} from "@/features/auth/types/auth.ts";
import {LOGIN_MUTATION, LOGOUT_MUTATION, ME_QUERY, REGISTER_MUTATION} from "@/features/auth/api/auth-operations.ts";


const initialState: AuthState = {
    user: null,
    isAuthenticated: false,
    loading: false,
    error: null,
};


// Async Thunks
export const login = createAsyncThunk(
    'auth/login',
    async ({username, password}: { username: string; password: string }, {rejectWithValue}) => {
        try {
            const {data} = await apolloClient.mutate({
                mutation: LOGIN_MUTATION,
                variables: {input: {username, password}},
            });

            if (!data.login.success) {
                return rejectWithValue(data.login.message || 'Login failed');
            }

            return data.login;
        } catch (error) {
            if (error instanceof Error) {
                return rejectWithValue(error.message);
            }
            return rejectWithValue('An unknown error occurred');
        }
    }
);

export const register = createAsyncThunk(
    'auth/register',
    async (
        input: {
            username: string;
            email: string;
            password: string;
            firstName: string;
            lastName: string;
            langKey?: string;
            timezone?: string;
        },
        {rejectWithValue}
    ) => {
        try {
            const {data} = await apolloClient.mutate({
                mutation: REGISTER_MUTATION,
                variables: {
                    input
                }
            });

            if (!data.register.success) {
                return rejectWithValue(data.register.message || 'Registration failed');
            }

            return data.register;
        } catch (error) {
            if (error instanceof Error) {
                return rejectWithValue(error.message);
            }
            return rejectWithValue('An unknown error occurred');
        }
    }
);

export const logout = createAsyncThunk(
    'auth/logout',
    async (_, {rejectWithValue}) => {
        try {
            await apolloClient.mutate({
                mutation: LOGOUT_MUTATION
            });

            // Clear Apollo cache on logout
            await apolloClient.resetStore();

            return true;
        } catch (error) {
            if (error instanceof Error) {
                return rejectWithValue(error.message);
            }
            return rejectWithValue('An unknown error occurred');
        }
    }
);

export const getCurrentUser = createAsyncThunk(
    'auth/getCurrentUser',
    async (_, {rejectWithValue}) => {
        try {
            const {data} = await apolloClient.query({
                query: ME_QUERY,
                fetchPolicy: 'network-only'
            });

            if (!data.me) {
                return rejectWithValue('User not found');
            }

            return data.me;
        } catch (error) {
            if (error instanceof Error) {
                return rejectWithValue(error.message);
            }
            return rejectWithValue('An unknown error occurred');
        }
    }
);


// Slice
const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        clearErrors: (state) => {
            state.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            // Login
            .addCase(login.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(login.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.isAuthenticated = true;
                state.user = action.payload.user;
            })
            .addCase(login.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            // Register
            .addCase(register.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(register.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.isAuthenticated = true;
                state.user = action.payload.user;
            })
            .addCase(register.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            // Logout
            .addCase(logout.fulfilled, (state) => {
                state.isAuthenticated = false;
                state.user = null;
            })
            // Get current user
            .addCase(getCurrentUser.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(getCurrentUser.fulfilled, (state, action: PayloadAction<User>) => {
                state.loading = false;
                state.isAuthenticated = true;
                state.user = action.payload;
            })
            .addCase(getCurrentUser.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            });
    }
});

// Selectors
export const selectAuth = (state: RootState) => state.auth;
export const selectUser = (state: RootState) => state.auth.user;
export const selectIsAuthenticated = (state: RootState) => state.auth.isAuthenticated;
export const selectAuthLoading = (state: RootState) => state.auth.loading;
export const selectAuthError = (state: RootState) => state.auth.error;

// Extract the action creators
export const {clearErrors} = authSlice.actions;

export default authSlice.reducer;
