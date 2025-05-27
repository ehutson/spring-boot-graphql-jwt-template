import React from "react";

const ForgotPasswordPage: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen text-center">
            <h1 className="text-9xl font-bold text-primary">Forgot Password</h1>
            <p className="mt-4 text-muted-foreground max-w-md">
                Please enter your email address to reset your password.
            </p>
            {/* Add your form here */}
        </div>
    );
}

export default ForgotPasswordPage;
