import React from 'react';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import {z} from 'zod';
import {Button} from '@/shared/components/ui/button.tsx';
import {Input} from '@/shared/components/ui/input.tsx';
import {Label} from '@/shared/components/ui/label.tsx';
import {useAuth} from '@/features/auth/hooks/useAuth.ts';
import {useForm} from '@/shared/hooks/useForm.ts';
import {toast} from 'sonner';
import {CheckCircle} from 'lucide-react';

// Validation schema
const loginSchema = z.object({
    username: z.string().min(1, 'Username is required'),
    password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const LoginPage: React.FC = () => {
    const {login: loginUser, clearErrors} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // Get the redirect path from the location state or default to dashboard
    const from = (location.state)?.from?.pathname ?? '/dashboard';

    // Check for a success message from password reset
    const successMessage = (location.state)?.message;

    const form = useForm(loginSchema, {
        defaultValues: {
            username: '',
            password: '',
        },
        persistKey: 'login-form', // Persists form data in case the user navigates away
        debounceMs: 300, // Debounce validation
    });

    // Clear any auth errors when the component mounts and show a success message if present
    React.useEffect(() => {
        clearErrors();

        if (successMessage) {
            toast.success(successMessage);
            // clean the message from the location state to prevent showing it again
            window.history.replaceState({}, document.title);
        }

        return () => {
            clearErrors();
            form.resetForm(); // Clear form data on unmounting
        };
    }, [clearErrors, form, successMessage]);

    const handleLogin = form.handleSubmit(async (data: LoginFormValues) => {
        const result = await loginUser(data.username, data.password);

        if (result.meta.requestStatus === 'fulfilled') {
            navigate(from);
        } else if (result.meta.requestStatus === 'rejected') {
            // Handle specific field errors
            const errorMessage = result.payload as string;
            if (errorMessage?.includes('username')) {
                form.setFieldError('username', errorMessage);
            } else if (errorMessage?.includes('password')) {
                form.setFieldError('password', errorMessage);
            }
        }
    });

    return (
        <div>
            <h2 className="text-2xl font-bold text-center mb-6">Sign in</h2>

            {/* Success message banner for password reset */}
            {successMessage && (
                <div
                    className="mb-6 p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                    <div className="flex items-center gap-2">
                        <CheckCircle className="w-5 h-5 text-green-600 dark:text-green-400"/>
                        <p className="text-sm text-green-800 dark:text-green-200">
                            {successMessage}
                        </p>
                    </div>
                </div>
            )}

            <form onSubmit={handleLogin} className="space-y-4">
                <div className="space-y-2">
                    <Label htmlFor="username">Username</Label>
                    <Input
                        id="username"
                        placeholder="Your username"
                        {...form.register('username', {
                            onBlur: () => void form.trigger('username'), // Trigger validation on blur
                        })}
                        autoComplete="username"
                        aria-invalid={!!form.formState.errors.username}
                        aria-describedby={form.formState.errors.username ? 'username-error' : undefined}
                    />
                    {form.formState.errors.username && (
                        <p id="username-error" className="text-sm text-red-500">
                            {form.formState.errors.username.message}
                        </p>
                    )}
                </div>

                <div className="space-y-2">
                    <div className="flex items-center justify-between">
                        <Label htmlFor="password">Password</Label>
                        <Link
                            to="/forgot-password"
                            className="text-sm text-primary hover:underline"
                        >
                            Forgot password?
                        </Link>
                    </div>
                    <Input
                        id="password"
                        type="password"
                        placeholder="••••••••"
                        {...form.register('password', {
                            onBlur: () => void form.trigger('password'), // Trigger validation on blur
                        })}
                        autoComplete="current-password"
                        aria-invalid={!!form.formState.errors.password}
                        aria-describedby={form.formState.errors.password ? 'password-error' : undefined}
                    />
                    {form.formState.errors.password && (
                        <p id="password-error" className="text-sm text-red-500">
                            {form.formState.errors.password.message}
                        </p>
                    )}
                </div>

                <Button
                    type="submit"
                    className="w-full"
                    disabled={form.formState.isSubmitting}
                >
                    {form.formState.isSubmitting ? 'Signing in...' : 'Sign in'}
                </Button>

                {/* Show submission status */}
                {form.formState.isSubmitted && !form.formState.isSuccessful && (
                    <p className="text-sm text-center text-muted-foreground">
                        {form.formState.submitCount > 2 && (
                            <span>
                                Still having trouble?{' '}
                                <Link to="/forgot-password" className="text-primary hover:underline">
                                    Reset your password
                                </Link>
                            </span>
                        )}
                    </p>
                )}
            </form>

            <div className="mt-6 text-center">
                <p className="text-sm text-muted-foreground">
                    Don't have an account?{' '}
                    <Link to="/register" className="text-primary hover:underline">
                        Sign up
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default LoginPage;