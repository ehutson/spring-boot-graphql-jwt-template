import {configureStore} from "@reduxjs/toolkit";
import {RootState} from "@/types/store.ts";


export function createTestStore(
    preloadedState: Partial<RootState> = {},
    reducers = {}) {
    return configureStore({
        reducer: reducers,
        preloadedState: preloadedState as any,
    });
}
