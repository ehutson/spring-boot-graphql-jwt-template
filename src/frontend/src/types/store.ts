import {AuthState} from './auth';

// Define the shape of the entire Redux store
export interface RootState {
    auth: AuthState;
    // Add other state slices here as needed
}

// Define the type for dispatch function from the store
export type AppDispatch = any; // This will be properly set in the actual store