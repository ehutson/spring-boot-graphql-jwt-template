import React from 'react';

const ResetPasswordPage: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen text-center">
            <h1 className="text-9xl font-bold text-primary">Reset Password</h1>
            <p className="mt-4 text-muted-foreground max-w-md">
                Please enter your new password.
            </p>
            {/* Add your form here */}
        </div>
    );
}

export default ResetPasswordPage;
