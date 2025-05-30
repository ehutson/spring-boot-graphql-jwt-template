import {useEffect, useRef} from 'react';
import {apolloClient} from '@/app/graphql-client/apollo-client.ts';
import {REFRESH_TOKEN_MUTATION} from '@/features/auth/api/auth-operations';

export function useTokenRefresh() {
    const refreshTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        const scheduleRefresh = () => {
            // Refresh 5 minutes before expiry
            const refreshIn = 10 * 60 * 1000; // 10 minutes

            refreshTimeoutRef.current = setTimeout(async () => {
                try {
                    const {data} = await apolloClient.mutate({
                        mutation: REFRESH_TOKEN_MUTATION,
                    });

                    if (data.refreshToken.success) {
                        scheduleRefresh(); // Schedule next refresh
                    }
                } catch (error) {
                    console.error('Token refresh failed:', error);
                    // Let the error link handle logout
                }
            }, refreshIn);
        };

        scheduleRefresh();

        return () => {
            if (refreshTimeoutRef.current) {
                clearTimeout(refreshTimeoutRef.current);
            }
        };
    }, []);
}
