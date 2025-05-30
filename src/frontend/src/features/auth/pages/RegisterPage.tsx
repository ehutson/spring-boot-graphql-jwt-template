import React from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {z} from 'zod';
import {Button} from '@/shared/components/ui/button.tsx';
import {Input} from '@/shared/components/ui/input.tsx';
import {Label} from '@/shared/components/ui/label.tsx';
import {useAuth} from '@/features/auth/hooks/useAuth.ts';
import {useForm} from '@/shared/hooks/useForm.ts';

// Validation schema
const registerSchema = z.object({
    username: z.string()
        .min(3, 'Username must be at least 3 characters long')
        .max(20, 'Username must be at most 20 characters long')
        .regex(/^\w+$/, 'Username can only contain letters, numbers, and underscores'),
    email: z.string()
        .email('Invalid email address')
        .min(1, 'Email is required'),
    password: z.string()
        .min(8, 'Password must be at least 8 characters long')
        .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
        .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
        .regex(/\d/, 'Password must contain at least one number'),
    confirmPassword: z.string().min(1, 'Confirm your password'),
    firstName: z.string()
        .min(1, 'First name is required')
        .max(50, 'First name must be at most 50 characters long'),
    lastName: z.string()
        .min(1, 'Last name is required')
        .max(50, 'Last name must be at most 50 characters long'),
}).refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
});

type RegisterFormValues = z.infer<typeof registerSchema>;

const RegisterPage: React.FC = () => {
    const {register: registerUser, clearErrors} = useAuth();
    const navigate = useNavigate();

    const form = useForm(registerSchema, {
        defaultValues: {
            username: '',
            email: '',
            password: '',
            confirmPassword: '',
            firstName: '',
            lastName: '',
        },
        persistKey: 'register-form', // Persists form data
        debounceMs: 500, // Debounce validation for better UX
    });

    // Clear any auth errors when the component mounts
    React.useEffect(() => {
        clearErrors();
        return () => {
            clearErrors();
            form.resetForm(); // Clear form data on unmount
        };
    }, [clearErrors, form]);

    const handleRegister = form.handleSubmit(async (data: RegisterFormValues) => {
        const {confirmPassword, ...registerData} = data;
        const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

        const result = await registerUser({
            ...registerData,
            timezone,
            langKey: navigator.language.split('-')[0] || 'en',
        });

        if (result.meta.requestStatus === 'fulfilled') {
            navigate('/dashboard');
        } else if (result.meta.requestStatus === 'rejected') {
            // Handle specific field errors
            const errorMessage = result.payload as string;
            if (errorMessage?.includes('username') || errorMessage?.includes('Username')) {
                form.setFieldError('username', errorMessage);
            } else if (errorMessage?.includes('email') || errorMessage?.includes('Email')) {
                form.setFieldError('email', errorMessage);
            } else if (errorMessage?.includes('password') || errorMessage?.includes('Password')) {
                form.setFieldError('password', errorMessage);
            }
        }
    });

    // Password strength indicator
    const getPasswordStrength = (password: string): { strength: number; label: string } => {
        let strength = 0;
        if (password.length >= 8) strength++;
        if (password.length >= 12) strength++;
        if (/[A-Z]/.test(password) && /[a-z]/.test(password)) strength++;
        if (/\d/.test(password)) strength++;
        if (/[^A-Za-z0-9]/.test(password)) strength++;

        const labels = ['Weak', 'Fair', 'Good', 'Strong', 'Very Strong'];
        return {strength, label: labels[strength] || 'Weak'};
    };

    const passwordValue = form.watch('password');
    const passwordStrength = passwordValue ? getPasswordStrength(passwordValue) : null;

    return (
        <div>
            <h2 className="text-2xl font-bold text-center mb-6">Create account</h2>

            <form onSubmit={handleRegister} className="space-y-4">
                <div className="space-y-2">
                    <Label htmlFor="username">Username</Label>
                    <Input
                        id="username"
                        placeholder="Your username"
                        {...form.register('username', {
                            onBlur: () => void form.trigger('username'),
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
                    <Label htmlFor="email">Email</Label>
                    <Input
                        id="email"
                        type="email"
                        placeholder="john.doe@example.com"
                        {...form.register('email', {
                            onBlur: () => void form.trigger('email'),
                        })}
                        autoComplete="email"
                        aria-invalid={!!form.formState.errors.email}
                        aria-describedby={form.formState.errors.email ? 'email-error' : undefined}
                    />
                    {form.formState.errors.email && (
                        <p id="email-error" className="text-sm text-red-500">
                            {form.formState.errors.email.message}
                        </p>
                    )}
                </div>

                <div className="space-y-2">
                    <Label htmlFor="password">Password</Label>
                    <Input
                        id="password"
                        type="password"
                        placeholder="••••••••"
                        {...form.register('password', {
                            onBlur: () => void form.trigger('password'),
                        })}
                        autoComplete="new-password"
                        aria-invalid={!!form.formState.errors.password}
                        aria-describedby={form.formState.errors.password ? 'password-error' : undefined}
                    />
                    {passwordStrength && (
                        <div className="flex items-center gap-2">
                            <div className="flex-1 bg-gray-200 rounded-full h-2">
                                <div
                                    className={`h-2 rounded-full transition-all ${
                                        passwordStrength.strength === 0 ? 'bg-red-500 w-1/5' :
                                            passwordStrength.strength === 1 ? 'bg-orange-500 w-2/5' :
                                                passwordStrength.strength === 2 ? 'bg-yellow-500 w-3/5' :
                                                    passwordStrength.strength === 3 ? 'bg-green-500 w-4/5' :
                                                        'bg-green-600 w-full'
                                    }`}
                                />
                            </div>
                            <span className="text-sm text-muted-foreground">
                                {passwordStrength.label}
                            </span>
                        </div>
                    )}
                    {form.formState.errors.password && (
                        <p id="password-error" className="text-sm text-red-500">
                            {form.formState.errors.password.message}
                        </p>
                    )}
                </div>

                <div className="space-y-2">
                    <Label htmlFor="confirmPassword">Confirm Password</Label>
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

                <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <Label htmlFor="firstName">First Name</Label>
                        <Input
                            id="firstName"
                            placeholder="John"
                            {...form.register('firstName', {
                                onBlur: () => void form.trigger('firstName'),
                            })}
                            autoComplete="given-name"
                            aria-invalid={!!form.formState.errors.firstName}
                            aria-describedby={form.formState.errors.firstName ? 'firstName-error' : undefined}
                        />
                        {form.formState.errors.firstName && (
                            <p id="firstName-error" className="text-sm text-red-500">
                                {form.formState.errors.firstName.message}
                            </p>
                        )}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="lastName">Last Name</Label>
                        <Input
                            id="lastName"
                            placeholder="Doe"
                            {...form.register('lastName', {
                                onBlur: () => void form.trigger('lastName'),
                            })}
                            autoComplete="family-name"
                            aria-invalid={!!form.formState.errors.lastName}
                            aria-describedby={form.formState.errors.lastName ? 'lastName-error' : undefined}
                        />
                        {form.formState.errors.lastName && (
                            <p id="lastName-error" className="text-sm text-red-500">
                                {form.formState.errors.lastName.message}
                            </p>
                        )}
                    </div>
                </div>

                <Button
                    type="submit"
                    className="w-full"
                    disabled={form.formState.isSubmitting || !form.formState.isDirty}
                >
                    {form.formState.isSubmitting ? 'Creating account...' : 'Create account'}
                </Button>

                {/* Show progress indicator for multistep forms in the future */}
                {form.formState.isSubmitted && !form.formState.isSuccessful && (
                    <p className="text-sm text-center text-muted-foreground">
                        {form.formState.submitCount > 2 && (
                            <span>
                                Having trouble?{' '}
                                <Link to="/login" className="text-primary hover:underline">
                                    Try logging in instead
                                </Link>
                            </span>
                        )}
                    </p>
                )}
            </form>

            <div className="mt-6 text-center">
                <p className="text-sm text-muted-foreground">
                    Already have an account?{' '}
                    <Link to="/login" className="text-primary hover:underline">
                        Sign in
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default RegisterPage;