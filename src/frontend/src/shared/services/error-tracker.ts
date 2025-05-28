import {store} from '@/app/store.ts';

interface ErrorContext {
    level?: 'app' | 'feature' | 'page';
    feature?: string;
    componentStack?: string | null;
    errorBoundary?: boolean;
    metadata?: Record<string, any>;
}

interface ErrorReport {
    message: string;
    stack?: string;
    context?: ErrorContext;
    timestamp: string;
    url: string;
    userAgent: string;
    userId?: string;
    sessionId?: string;
    buildVersion?: string;
}

class ErrorTracker {
    private queue: ErrorReport[] = [];
    private isOnline = navigator.onLine;
    private maxQueueSize = 50;
    private flushInterval: number = 30000; // 30 seconds
    private endpoint = "/api/errors";

    constructor() {
        // Listen for online/offline events
        window.addEventListener('online', () => {
            this.isOnline = true
            this.flushQueue();
        });

        window.addEventListener('offline', () => {
            this.isOnline = false;
        });

        // Periodically flush the queue
        setInterval(() => {
            if (this.isOnline) {
                this.flushQueue();
            }
        }, this.flushInterval);

        // Flush on page unload
        window.addEventListener('beforeunload', () => {
            this.flushQueue(true);
        });
    }

    captureError(error: Error | string, context: ErrorContext = {}) {
        try {
            const errorReport = this.createErrorReport(error, context);

            // Always log to console in development
            if (process.env.NODE_ENV === 'development') {
                console.error('[ErrorTracker]', errorReport);
            }

            // Add to queue
            this.addToQueue(errorReport);

            // Flush immediately for critical errors
            if (context.level === 'app') {
                this.flushQueue();
            }
        } catch (trackingError) {
            // Don't let error tracking break the app
            console.error('ErrorTracker failed to capture error:', trackingError);
        }
    }

    private createErrorReport(error: Error | string, context: ErrorContext): ErrorReport {
        const state = store.getState();
        const user = state.auth?.user;

        return {
            message: typeof error === 'string' ? error : error.message,
            stack: typeof error === 'object' ? error.stack : undefined,
            context,
            timestamp: new Date().toISOString(),
            url: window.location.href,
            userAgent: navigator.userAgent,
            userId: user?.id,
            sessionId: this.getSessionId(),
            buildVersion: process.env.REACT_APP_VERSION ?? 'development',
        };
    }

    private addToQueue(report: ErrorReport) {
        this.queue.push(report);

        // Prevent the queue from growing indefinitely
        if (this.queue.length > this.maxQueueSize) {
            this.queue = this.queue.slice(-this.maxQueueSize);
        }
    }

    private async flushQueue(sync = false) {
        if (this.queue.length === 0) return;

        const errors = [...this.queue];
        this.queue = []; // Clear the queue immediately

        try {
            if (sync) {
                // Use sendBeacon for synchronous sending on page unload
                const blob = new Blob([JSON.stringify(errors)], {type: 'application/json'});
                navigator.sendBeacon(this.endpoint, blob);
            } else {
                // Normal async sending
                await this.sendErrors(errors);
            }
        } catch (error) {
            // Put errors back in the queue if sending fails
            this.queue = [...errors, ...this.queue];
            console.error('ErrorTracker failed to send error reports:', error);
        }
    }

    private async sendErrors(errors: ErrorReport[]) {
        // TODO: Implement the actual API call to the backend
        // For now, just log that we would send them
        console.log('[ErrorTracker] Would send errors to the backend:', errors);
    }

    private getSessionId(): string {
        // Generate or retrieve a session ID
        let sessionId = sessionStorage.getItem('errorTrackerSessionId');
        if (!sessionId) {
            sessionId = `session-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
            sessionStorage.setItem('errorTrackerSessionId', sessionId);
        }

        return sessionId;
    }

    // Public method to manually send errors
    flush() {
        return this.flushQueue();
    }

    // Configure the error tracker
    configure(options: {
        maxQueueSize?: number;
        flushInterval?: number;
        endpoint?: string;
    }) {
        if (options.maxQueueSize) this.maxQueueSize = options.maxQueueSize;
        if (options.flushInterval) this.flushInterval = options.flushInterval;
        if (options.endpoint) this.endpoint = options.endpoint;
    }
}

// Export singleton instance
export const errorTracker = new ErrorTracker();

// Also export the types for use in other files
export type {ErrorContext, ErrorReport};