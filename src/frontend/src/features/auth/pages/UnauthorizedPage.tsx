import React from 'react';
import {Link} from 'react-router-dom';
import {Button} from '@/shared/components/ui/button.tsx';

const UnauthorizedPage: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-[70vh] text-center">
            <h1 className="text-9xl font-bold text-primary">403</h1>
            <h2 className="text-3xl font-bold mt-6">Access Denied</h2>
            <p className="text-muted-foreground mt-4 max-w-md">
                You don't have permission to access this page.
            </p>
            <div className="mt-8 space-x-4">
                <Button asChild>
                    <Link to="/">Return Home</Link>
                </Button>
                <Button asChild variant="outline">
                    <Link to="/dashboard">Go to Dashboard</Link>
                </Button>
            </div>
        </div>
    );
};

export default UnauthorizedPage;