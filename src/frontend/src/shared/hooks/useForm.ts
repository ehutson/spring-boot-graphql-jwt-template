import {useForm as useHookForm, UseFormProps} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {z} from 'zod';
import {useCallback} from 'react';
import {errorHandler} from '@/shared/utils/error-handler';
import {toast} from 'sonner';

export function useForm<T extends z.ZodType>(
    schema: T,
    options?: UseFormProps<z.infer<T>>
) {
    const form = useHookForm<z.infer<T>>({
        ...options,
        resolver: zodResolver(schema),
    });

    const handleSubmit = useCallback(
        (onSubmit: (data: z.infer<T>) => Promise<void>) => {
            return form.handleSubmit(async (data) => {
                try {
                    await onSubmit(data);
                } catch (error) {
                    const message = errorHandler.getErrorMessage(error);
                    toast.error(message);
                    console.error('Form submission error:', error);
                }
            });
        },
        [form]
    );

    return {
        ...form,
        handleSubmit,
    };
}