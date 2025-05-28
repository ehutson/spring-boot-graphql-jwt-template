import React from 'react';
import {Button} from '@/shared/components/ui/button.tsx';
import {AlertCircle, Home, RefreshCw} from 'lucide-react';

interface PageErrorFallbackProps {
    resetErrorBoundary?: () => void;
    errorCount?: number;
}

const PageErrorFallback: React.FC<PageErrorFallbackProps> = ({resetErrorBoundary, errorCount}) => {
    if (errorCount === undefined) {
        errorCount = 0; // Default to 0 if not provided
    }

    return (
        <div className="flex flex-col items-center justify-center min-h-[50vh] text-center p-6">
            <AlertCircle className="w-12 h-12 text-destructive mb-4"/>
            <h2 className="text-xl font-bold mb-2">Something went wrong</h2>
            <p className="text-muted-foreground mb-6 max-w-md">
                {errorCount > 2
                    ? "This page is experiencing repeated errors. Please try again later or contact support."
                    : "An error occurred while loading this page. You can try again or return home."}
            </p>
            <div className="flex gap-3">
                {errorCount <= 2 && (
                    <Button
                        variant="default"
                        onClick={resetErrorBoundary}
                    >
                        <RefreshCw className="w-4 h-4 mr-2"/>
                        Try Again
                    </Button>
                )}
                <Button
                    variant={errorCount > 2 ? "default" : "outline"}
                    onClick={() => window.location.href = '/'}
                >
                    <Home className="w-4 h-4 mr-2"/>
                    Return Home
                </Button>
            </div>
        </div>
    );
};

export default PageErrorFallback;