import React from 'react';
import {useForm} from 'react-hook-form';
import {Link, useLocation, useNavigate} from 'react-router-dom';
import {z} from 'zod';
import {zodResolver} from '@hookform/resolvers/zod';
import {Button} from '@/components/ui/button.tsx';
import {Input} from '@/components/ui/input.tsx';
import {Label} from '@/components/ui/label.tsx';
import {useAuth} from '@/hooks/useAuth.ts';
import {toast} from 'sonner';

// Validation schema
const loginSchema = z.object({
    username: z.string().min(1, 'Username is required'),
    password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const LoginPage: React.FC = () => {
    const {login, error, clearErrors} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // Get redirect path from location state of default to dashboard
    const from = (location.state)?.from?.pathname || '/dashboard';

    const {
        register,
        handleSubmit,
        formState: {errors, isSubmitting},
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            username: '',
            password: '',
        },
    });

    // Clear any auth errors when the component mounts
    React.useEffect(() => {
        clearErrors();
        return () => clearErrors();
    }, [clearErrors]);

    // Show error toast when error state changes
    React.useEffect(() => {
        if (error) {
            toast.error(error);
        }
    }, [error]);

    const onSubmit = async (data: LoginFormValues) => {
        try {
            const result = await login(data.username, data.password);
            if (result.meta.requestStatus === 'fulfilled') {
                toast.success('Login successful');
                navigate(from);
            }
        } catch (err) {
            console.error('Login error:', err);
        }
    };

    return (
        <div>
            <h2 className="text-2xl font-bold text-center mb-6">Sign in</h2>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div className="space-y-2">
                    <Label htmlFor="username">Username</Label>
                    <Input
                        id="username"
                        placeholder="Your username"
                        {...register('username')}
                        autoComplete="username"
                    />
                    {errors.username && <p className="text-red-500">{errors.username.message}</p>}
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
                        {...register('password')}
                        autoComplete="current-password"
                    />
                    {errors.password && (
                        <p className="text-sm text-red-500">{errors.password.message}</p>
                    )}
                </div>

                <Button type="submit" className="w-full" disabled={isSubmitting}>
                    {isSubmitting ? 'Signing in...' : 'Sign in'}
                </Button>
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