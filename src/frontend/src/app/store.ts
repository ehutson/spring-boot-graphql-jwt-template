import {configureStore} from "@reduxjs/toolkit";
import authReducer from "@/features/auth/authSlice";


export const store = configureStore({
    reducer: {
        auth: authReducer,
        // Add your other reducers here
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: false // For potential async data
        }),
});

// Infer types from the store
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
