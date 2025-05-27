import React from 'react';
import {Link, Outlet} from 'react-router-dom';
import {Toaster} from '@/shared/components/ui/sonner.tsx';

const AuthLayout: React.FC = () => {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-slate-100 dark:bg-slate-900">
            <div className="w-full max-w-md px-8 py-10 bg-white dark:bg-slate-800 rounded-lg shadow-lg">
                <div className="mb-6 text-center">
                    <Link to="/" className="inline-block">
                        <h1 className="text-2xl font-bold text-primary">Spring GraphQL Template</h1>
                    </Link>
                    <p className="text-sm text-muted-foreground mt-2">Secure authentication with GraphQL</p>
                </div>

                <Outlet/>
            </div>
            <Toaster position="top-right"/>
        </div>
    );
};

export default AuthLayout;