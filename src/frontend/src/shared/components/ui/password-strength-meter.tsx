import React, {useCallback, useMemo} from "react";
import {cn} from '@/shared/lib/utils.ts';

interface PasswordStrengthMeterProps {
    password: string;
    className?: string;
    showLabel?: boolean;
}

interface StrengthResult {
    strength: number;
    label: string;
    percentage: number;
}

const PasswordStrengthMeter: React.FC<PasswordStrengthMeterProps> = ({
                                                                         password, className, showLabel = true,
                                                                     }) => {

    const getPasswordStrength = useCallback((password: string): StrengthResult => {
        if (!password) {
            return {strength: 0, label: 'Enter a password', percentage: 0};
        }

        let strength = 0;

        // Length criteria
        if (password.length >= 8) strength++;
        if (password.length >= 12) strength++;

        // Character variety criteria
        if (/[A-Z]/.test(password) && /[a-z]/.test(password)) strength++
        if (/\d/.test(password)) strength++
        if (/[^A-Za-z0-9]/.test(password)) strength++

        const labels = ['Very Weak', 'Weak', 'Fair', 'Good', 'Strong', 'Very Strong'];
        const percentages = [10, 25, 45, 65, 85, 100];

        return {
            strength,
            label: labels[strength] || 'Very Weak',
            percentage: percentages[strength] || 10,
        }
    }, []);

    const strengthResult = useMemo(() => getPasswordStrength(password), [password, getPasswordStrength]);

    const getStrengthColor = (strength: number): string => {
        switch (strength) {
            case 0:
            case 1:
                return 'bg-destructive'; // Red for very weak/weak
            case 2:
                return 'bg-orange-500'; // Fair - orange
            case 3:
                return 'bg-yellow-500'; // Good - yellow
            case 4:
                return 'bg-green-500'; // Strong - green
            case 5:
                return 'bg-blue-500'; // Very Strong - blue
            default:
                return 'bg-muted';
        }
    }

    const getStrengthTextColor = (strength: number): string => {
        switch (strength) {
            case 0:
            case 1:
                return 'text-destructive'
            case 2:
                return 'text-orange-600'
            case 3:
                return 'text-yellow-600'
            case 4:
                return 'text-green-600'
            case 5:
                return 'text-green-700'
            default:
                return 'text-muted-foreground'
        }
    }

    if (!password) {
        return null
    }

    return (
        <div className={cn("flex items-center gap-2", className)}>
            <progress
                className="flex-1 bg-muted rounded-full h-2 overflow-hidden"
                aria-valuenow={strengthResult.percentage}
                aria-valuemin={0}
                aria-valuemax={5}
                aria-label={`Password strength: ${strengthResult.label}`}
            >
                <div
                    className={cn("h-2 rounded-full transition-all duration-300 ease-in-out", getStrengthColor(strengthResult.strength)
                    )}
                    style={{width: `${strengthResult.percentage}%`}}
                />
            </progress>
            {showLabel && (
                <span
                    className={cn("text-sm font-medium transition-colors duration-200", getStrengthTextColor(strengthResult.strength))}
                    aria-live="polite">
                    {strengthResult.label}
                </span>
            )}
        </div>
    );
}

export {PasswordStrengthMeter};