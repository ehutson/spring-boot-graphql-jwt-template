import React from "react";
import {Link} from "react-router-dom";
import {z} from "zod";
import {useMutation} from "@apollo/client";
import {REQUEST_PASSWORD_RESET_MUTATION} from "@/features/auth/api/auth-operations.ts";
import {Button} from "@/shared/components/ui/button.tsx";
import {Input} from "@/shared/components/ui/input.tsx";
import {Label} from "@/shared/components/ui/label.ts";
import {useForm} from '@/shared/hooks/useForm.ts';
import {toast} from 'sonner';
import {CheckCircle, Mail} from "lucide-react";

// Validation schema
const forgotPasswordSchema = z.object({
    email: z.string()
        .min(1, "Email is required")
        .email('Please enter a valid email address'),
});

type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;

const ForgotPasswordPage: React.FC = () => {
    const [emailSent, setEmailSent] = React.useState(false);
    const [submittedEmail, setSubmittedEmail] = React.useState('');

    const form = useForm(forgotPasswordSchema, {
        defaultValues: {
            email: '',
        },
        debounceMs: 300, // Debounce validation
    });


    const [requestPasswordReset, {loading}] = useMutation(REQUEST_PASSWORD_RESET_MUTATION, {
        onCompleted: () => {
            setEmailSent(true);
            setSubmittedEmail(form.getValues('email'))
            toast.success('Password reset email sent successfully!');
        },
        onError: (error) => {
            if (error.message.includes('not found') || error.message.includes('does not exist')) {
                form.setFieldError('email', 'No account found with this email address');
            } else {
                toast.error('Failed to send password reset email.  Please try again later.');
            }
        }
    });

    const handleSubmit = form.handleSubmit(async (data: ForgotPasswordFormValues) => {
        await requestPasswordReset({
            variables: {email: data.email}
        });
    });

    const handleResendEmail = () => {
        setEmailSent(false);
        form.setValue('email', submittedEmail);
    };

    if (emailSent) {
        return (
            <div className="text-center space-y-6">
                <div
                    className="mx-auto w-16 h-16 bg-green-100 dark:bg-green-900/20 rounded-full flex items-center justify-center">
                    <CheckCircle className="w-8 h-8 text-green-600 dark:text-green-400"/>
                </div>

                <div className="space-y-2">
                    <h2 className="text-2xl font-bold">Check your email</h2>
                    <p className="text-muted-foreground">
                        We've sent password reset instructions to:
                    </p>
                    <p className="font-medium text-foreground">{submittedEmail}</p>
                </div>

                <div className="space-y-4 text-sm">
                    <div className="p-4 bg-muted/50 rounded-lg space-y-2">
                        <div className="flex items-center gap-2 text-muted-foreground">
                            <Mail className="w-4 h-4"/>
                            <span>What to do next:</span>
                        </div>
                        <ul className="text-left text-muted-foreground space-y-1 pl-6">
                            <li>• Check your email inbox for a reset link</li>
                            <li>• Click the link in the email to reset your password</li>
                            <li>• The link will expire in 1 hour for security</li>
                            <li>• Check your spam folder if you don't see the email</li>
                        </ul>
                    </div>

                    <div className="flex flex-col gap-3">
                        <Button
                            variant="outline"
                            onClick={handleResendEmail}
                            className="w-full"
                        >
                            Send to a different email
                        </Button>

                        <Link to="/login">
                            <Button variant="ghost" className="w-full">
                                Back to sign in
                            </Button>
                        </Link>
                    </div>
                </div>
            </div>
        );
    }


    return (
        <div className="space-y-6">
            <div className="text-center space-y-2">
                <h2 className="text-2xl font-bold">Forgot your password?</h2>
                <p className="text-muted-foreground">
                    Enter your email address and we'll send you a link to reset your password.
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                    <Label htmlFor="email">Email address</Label>
                    <Input
                        id="email"
                        type="email"
                        placeholder="john.doe@example.com"
                        {...form.register('email', {
                            onBlur: () => void form.trigger('email'),
                        })}
                        autoComplete="email"
                        autoFocus
                        aria-invalid={!!form.formState.errors.email}
                        aria-describedby={form.formState.errors.email ? 'email-error' : undefined}
                    />
                    {form.formState.errors.email && (
                        <p id="email-error" className="text-sm text-red-500">
                            {form.formState.errors.email.message}
                        </p>
                    )}
                </div>

                <Button
                    type="submit"
                    className="w-full"
                    disabled={loading || form.formState.isSubmitting || !form.formState.isValid}
                >
                    {loading || form.formState.isSubmitting ? 'Sending...' : 'Send reset link'}
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
                    Don't have an account?{' '}
                    <Link to="/register" className="text-primary hover:underline">
                        Sign up
                    </Link>
                </div>
            </div>

            {/* Help section */}
            <div className="mt-8 p-4 bg-muted/30 rounded-lg">
                <h3 className="text-sm font-medium mb-2">Need help?</h3>
                <p className="text-xs text-muted-foreground">
                    If you're having trouble with password reset, make sure you're using the email
                    address associated with your account. For additional support, contact our team.
                </p>
            </div>
        </div>
    );
}

export default ForgotPasswordPage;
