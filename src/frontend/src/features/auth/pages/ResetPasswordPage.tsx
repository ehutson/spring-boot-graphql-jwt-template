import React from 'react';
import {Link, useNavigate, useSearchParams} from 'react-router-dom';
import {z} from 'zod';
import {useMutation} from '@apollo/client';
import {Button} from '@/shared/components/ui/button.tsx';
import {Input} from '@/shared/components/ui/input.tsx';
import {Label} from '@/shared/components/ui/label.tsx';
import {useForm} from '@/shared/hooks/useForm.ts';
import {RESET_PASSWORD_MUTATION} from '@/features/auth/api/auth-operations.ts';
import {PasswordStrengthMeter} from '@/shared/components/ui/password-strength-meter.tsx';
import {toast} from 'sonner';
import {AlertCircle, CheckCircle, KeyRound} from 'lucide-react';

// Validation schema
const resetPasswordSchema = z.object({
    newPassword: z.string()
        .min(8, 'Password must be at least 8 characters long')
        .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
        .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
        .regex(/\d/, 'Password must contain at least one number'),
    confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
});

type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;

const ResetPasswordPage: React.FC = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [isSuccess, setIsSuccess] = React.useState(false);
    const [tokenError, setTokenError] = React.useState<string | null>(null);

    // Extract token from URL parameters
    const token = searchParams.get('token');

    const [resetPassword, {loading}] = useMutation(RESET_PASSWORD_MUTATION, {
        onCompleted: (data) => {
            if (data.resetPassword) {
                setIsSuccess(true);
                toast.success('Password reset successfully');
                // Redirect to login after a short delay
                setTimeout(() => {
                    navigate('/login', {
                        state: {message: 'Password reset successfully. Please sign in with your new password.'}
                    });
                }, 3000);
            }
        },
        onError: (error) => {
            // Handle specific errors
            if (error.message.includes('token') && error.message.includes('expired')) {
                setTokenError('This password reset link has expired. Please request a new one.');
            } else if (error.message.includes('token') && error.message.includes('invalid')) {
                setTokenError('This password reset link is invalid. Please request a new one.');
            } else if (error.message.includes('password')) {
                // Handle password validation errors from backend
                form.setFieldError('newPassword', error.message);
            } else {
                toast.error('Failed to reset password. Please try again.');
            }
        }
    });

    const form = useForm(resetPasswordSchema, {
        defaultValues: {
            newPassword: '',
            confirmPassword: '',
        },
        debounceMs: 300,
    });

    // Check if token exists on component mount
    React.useEffect(() => {
        if (!token) {
            setTokenError('No reset token provided. Please use the link from your email.');
        }
    }, [token]);

    const handleSubmit = form.handleSubmit(async (data: ResetPasswordFormValues) => {
        if (!token) {
            setTokenError('No reset token available. Please request a new password reset.');
            return;
        }

        await resetPassword({
            variables: {
                input: {
                    newPassword: data.newPassword,
                    token: token,
                }
            }
        });
    });

    const passwordValue = form.watch('newPassword');

    // Success state
    if (isSuccess) {
        return (
            <div className="text-center space-y-6">
                <div
                    className="mx-auto w-16 h-16 bg-green-100 dark:bg-green-900/20 rounded-full flex items-center justify-center">
                    <CheckCircle className="w-8 h-8 text-green-600 dark:text-green-400"/>
                </div>

                <div className="space-y-2">
                    <h2 className="text-2xl font-bold">Password reset successful!</h2>
                    <p className="text-muted-foreground">
                        Your password has been updated successfully.
                    </p>
                </div>

                <div className="space-y-4">
                    <div className="p-4 bg-muted/50 rounded-lg">
                        <p className="text-sm text-muted-foreground">
                            Redirecting you to the sign-in page in a few seconds...
                        </p>
                    </div>

                    <Button asChild className="w-full">
                        <Link to="/login">
                            Continue to sign in
                        </Link>
                    </Button>
                </div>
            </div>
        );
    }

    // Token error state
    if (tokenError) {
        return (
            <div className="text-center space-y-6">
                <div
                    className="mx-auto w-16 h-16 bg-red-100 dark:bg-red-900/20 rounded-full flex items-center justify-center">
                    <AlertCircle className="w-8 h-8 text-red-600 dark:text-red-400"/>
                </div>

                <div className="space-y-2">
                    <h2 className="text-2xl font-bold">Reset link issue</h2>
                    <p className="text-muted-foreground">
                        {tokenError}
                    </p>
                </div>

                <div className="space-y-3">
                    <Button asChild className="w-full">
                        <Link to="/forgot-password">
                            Request new reset link
                        </Link>
                    </Button>

                    <Button asChild variant="outline" className="w-full">
                        <Link to="/login">
                            Back to sign in
                        </Link>
                    </Button>
                </div>
            </div>
        );
    }

    // Form state
    return (
        <div className="space-y-6">
            <div className="text-center space-y-2">
                <div className="mx-auto w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center mb-4">
                    <KeyRound className="w-6 h-6 text-primary"/>
                </div>
                <h2 className="text-2xl font-bold">Reset your password</h2>
                <p className="text-muted-foreground">
                    Enter your new password below. Make sure it's strong and secure.
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                    <Label htmlFor="newPassword">New Password</Label>
                    <Input
                        id="newPassword"
                        type="password"
                        placeholder="••••••••"
                        {...form.register('newPassword', {
                            onBlur: () => void form.trigger('newPassword'),
                        })}
                        autoComplete="new-password"
                        autoFocus
                        aria-invalid={!!form.formState.errors.newPassword}
                        aria-describedby={form.formState.errors.newPassword ? 'newPassword-error' : undefined}
                    />
                    <PasswordStrengthMeter password={passwordValue}/>
                    {form.formState.errors.newPassword && (
                        <p id="newPassword-error" className="text-sm text-red-500">
                            {form.formState.errors.newPassword.message}
                        </p>
                    )}
                </div>

                <div className="space-y-2">
                    <Label htmlFor="confirmPassword">Confirm New Password</Label>
                    <Input
                        id="confirmPassword"
                        type="password"
                        placeholder="••••••••"
                        {...form.register('confirmPassword', {
                            onBlur: () => void form.trigger('confirmPassword'),
                        })}
                        autoComplete="new-password"
                        aria-invalid={!!form.formState.errors.confirmPassword}
                        aria-describedby={form.formState.errors.confirmPassword ? 'confirmPassword-error' : undefined}
                    />
                    {form.formState.errors.confirmPassword && (
                        <p id="confirmPassword-error" className="text-sm text-red-500">
                            {form.formState.errors.confirmPassword.message}
                        </p>
                    )}
                </div>

                <Button
                    type="submit"
                    className="w-full"
                    disabled={loading || form.formState.isSubmitting || !form.formState.isValid || !token}
                >
                    {loading || form.formState.isSubmitting ? 'Resetting password...' : 'Reset password'}
                </Button>
            </form>

            <div className="text-center space-y-4">
                <div className="text-sm">
                    <Link
                        to="/login"
                        className="text-primary hover:underline"
                    >
                        Back to sign in
                    </Link>
                </div>

                <div className="text-sm text-muted-foreground">
                    Remember your password?{' '}
                    <Link to="/login" className="text-primary hover:underline">
                        Sign in instead
                    </Link>
                </div>
            </div>

            {/* Security notice */}
            <div className="mt-8 p-4 bg-muted/30 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Security tips</h3>
                <ul className="text-xs text-muted-foreground space-y-1">
                    <li>• Use a unique password you haven't used elsewhere</li>
                    <li>• Include a mix of letters, numbers, and symbols</li>
                    <li>• Make it at least 8 characters long</li>
                    <li>• Consider using a password manager</li>
                </ul>
            </div>
        </div>
    );
};

export default ResetPasswordPage;