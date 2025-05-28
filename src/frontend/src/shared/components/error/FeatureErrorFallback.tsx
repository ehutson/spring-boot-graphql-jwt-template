import React from 'react';
import {Button} from '@/shared/components/ui/button.tsx';
import {AlertCircle, Home} from 'lucide-react';

interface FeatureErrorFallbackProps {
    resetErrorBoundary?: () => void;
}

const FeatureErrorFallback: React.FC<FeatureErrorFallbackProps> = ({resetErrorBoundary}) => {
    return (
        <div className="p-6 bg-destructive/10 border border-destructive/20 rounded-lg">
            <div className="flex items-start gap-4">
                <AlertCircle className="w-5 h-5 text-destructive shrink-0 mt-0.5"/>
                <div className="flex-1">
                    <h3 className="font-semibold mb-1">Feature Unavailable</h3>
                    <p className="text-sm text-muted-foreground mb-4">
                        This section encountered an error and cannot be displayed.
                    </p>
                    <div className="flex gap-2">
                        <Button
                            size="sm"
                            variant="outline"
                            onClick={resetErrorBoundary}
                        >
                            Try Again
                        </Button>
                        <Button
                            size="sm"
                            variant="outline"
                            onClick={() => window.location.href = '/'}
                        >
                            <Home className="w-4 h-4 mr-2"/>
                            Go Home
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FeatureErrorFallback;