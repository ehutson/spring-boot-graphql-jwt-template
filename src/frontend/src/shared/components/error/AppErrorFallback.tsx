import React from 'react';
import {Button} from '@/shared/components/ui/button.tsx';
import {AlertCircle, RefreshCw} from 'lucide-react';

interface AppErrorFallbackProps {
    error?: Error;
}

const AppErrorFallback: React.FC<AppErrorFallbackProps> = ({error}) => {
    return (
        <div className="min-h-screen flex items-center justify-center bg-background">
            <div className="text-center max-w-md mx-auto p-6">
                <AlertCircle className="w-16 h-16 text-destructive mx-auto mb-4"/>
                <h1 className="text-2xl font-bold mb-2">Application Error</h1>
                <p className="text-muted-foreground mb-6">
                    We're sorry, but something went wrong. Please try refreshing the page.
                </p>
                {error?.message && process.env.NODE_ENV === 'development' && (
                    <details className="mb-4 text-left">
                        <summary className="cursor-pointer text-sm text-muted-foreground">
                            Error details
                        </summary>
                        <pre className="mt-2 p-2 bg-muted rounded text-xs overflow-auto">
                            {error.message}
                        </pre>
                    </details>
                )}
                <Button onClick={() => window.location.reload()}>
                    <RefreshCw className="w-4 h-4 mr-2"/>
                    Reload Application
                </Button>
            </div>
        </div>
    );
};

export default AppErrorFallback;