import {FieldErrors, Path, useForm as useHookForm, UseFormProps} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {z} from 'zod';
import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {useErrorHandler} from '@/shared/hooks/useErrorHandler';
import {toast} from 'sonner';
import {ApolloError, FetchResult, MutationFunctionOptions, OperationVariables} from '@apollo/client';
import debounce from 'lodash/debounce';

interface UseFormOptions<T extends z.ZodType> extends UseFormProps<z.infer<T>> {
    debounceMs?: number;
    persistKey?: string; // For form draft persistence
    showSuccessToast?: boolean;
    showErrorToast?: boolean;
}

interface FormState {
    isSubmitting: boolean;
    isSubmitted: boolean;
    submitCount: number;
    isSuccessful: boolean;
    errors: Record<string, string>;
}

interface UseFormReturn<T extends z.ZodType> {
    formState: ReturnType<typeof useHookForm<z.TypeOf<T>>>['formState'] & FormState;
    handleSubmit: (
        onSubmit: (data: z.TypeOf<T>) => void | Promise<void>,
        onError?: (errors: FieldErrors<z.TypeOf<T>>) => void
    ) => (e?: React.BaseSyntheticEvent) => Promise<void>;
    handleGraphQLSubmit: <TData = any, TVariables extends OperationVariables = OperationVariables>(
        mutationFn: (options?: MutationFunctionOptions<TData, TVariables>) => Promise<FetchResult<TData>>,
        options?: {
            variables?: (data: z.TypeOf<T>) => TVariables;
            onCompleted?: (data: TData) => void | Promise<void>;
            onError?: (error: ApolloError) => void;
            successMessage?: string;
            errorMessage?: string | ((error: ApolloError) => string);
        }
    ) => (e?: React.BaseSyntheticEvent) => Promise<void>;
    setFieldError: (field: keyof z.TypeOf<T>, message: string) => void;
    clearFieldError: (field: keyof z.TypeOf<T>) => void;
    resetForm: () => void;
    register: ReturnType<typeof useHookForm<z.TypeOf<T>>>['register'];
    control: ReturnType<typeof useHookForm<z.TypeOf<T>>>['control'];
    watch: ReturnType<typeof useHookForm<z.TypeOf<T>>>['watch'];
    setValue: ReturnType<typeof useHookForm<z.TypeOf<T>>>['setValue'];
    getValues: ReturnType<typeof useHookForm<z.TypeOf<T>>>['getValues'];
    reset: ReturnType<typeof useHookForm<z.TypeOf<T>>>['reset'];
    trigger: ReturnType<typeof useHookForm<z.TypeOf<T>>>['trigger'];
}

export function useForm<T extends z.ZodType>(
    schema: T,
    options: UseFormOptions<T> = {}
): UseFormReturn<T> {
    const {
        debounceMs = 0,
        persistKey,
        showSuccessToast = true,
        showErrorToast = true,
        ...formOptions
    } = options;

    const {handleError} = useErrorHandler();
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
    const [formState, setFormState] = useState<FormState>({
        isSubmitting: false,
        isSubmitted: false,
        submitCount: 0,
        isSuccessful: false,
        errors: {}
    });

    // Initialize react-hook-form
    const form = useHookForm<z.infer<T>>({
        ...formOptions,
        resolver: zodResolver(schema),
    });

    // Load persisted form data if persistKey is provided
    useEffect(() => {
        if (persistKey) {
            try {
                const savedData = localStorage.getItem(`form_draft_${persistKey}`);
                if (savedData) {
                    const parsed = JSON.parse(savedData);
                    form.reset(parsed);
                }

            } catch (error) {
                console.error('Failed to load form draft:', error);
            }
        }
    }, [persistKey, form]);

    // Create debounced validation if needed
    const validateField = useMemo(
        () => debounce(async (
            fieldName: Path<z.TypeOf<T>> | Path<z.TypeOf<T>>[] | readonly Path<z.TypeOf<T>>[]
        ) => {
            await form.trigger(fieldName);
        }, debounceMs),
        [form, debounceMs]
    );


    // Watch for changes and persist if needed
    useEffect(() => {
        if (persistKey) {
            const subscription = form.watch((data) => {
                try {
                    localStorage.setItem(`form_draft_${persistKey}`, JSON.stringify(data));
                } catch (error) {
                    console.error('Failed to save form draft:', error);
                }
            });
            return () => subscription.unsubscribe();
        }
    }, [form, persistKey]);

    // Enhanced handleSubmit with error handling
    const handleSubmit = useCallback(
        (onSubmit: (data: z.infer<T>) => Promise<void> | void, onError?: (errors: FieldErrors<z.infer<T>>) => void) => {
            return form.handleSubmit(async (data) => {
                setFormState(prev => ({
                    ...prev,
                    isSubmitting: true,
                    isSubmitted: false,
                    isSuccessful: false,
                    errors: {}
                }));

                try {
                    await onSubmit(data);

                    setFormState(prev => ({
                        ...prev,
                        isSubmitting: false,
                        isSubmitted: true,
                        isSuccessful: true,
                        submitCount: prev.submitCount + 1
                    }));

                    // Clear persisted draft on successful submit
                    if (persistKey) {
                        localStorage.removeItem(`form_draft_${persistKey}`);
                    }
                } catch (error) {
                    setFormState(prev => ({
                        ...prev,
                        isSubmitting: false,
                        isSubmitted: true,
                        isSuccessful: false,
                        submitCount: prev.submitCount + 1
                    }));

                    if (showErrorToast) {
                        handleError(error, {
                            showToast: true,
                            feature: 'form-submission',
                            level: 'page'
                        });
                    }

                    throw error; // Re-throw to maintain error handling chain
                }
            }, onError);
        },
        [form, handleError, persistKey, showErrorToast]
    );

    // GraphQL-specific submit handler that uses existing Apollo mutation hooks
    const handleGraphQLSubmit = useCallback(
        <TData = any, TVariables extends OperationVariables = OperationVariables>(
            mutationFn: (options?: MutationFunctionOptions<TData, TVariables>) => Promise<FetchResult<TData>>,
            options: {
                variables?: (data: z.infer<T>) => TVariables;
                onCompleted?: (data: TData) => void | Promise<void>;
                onError?: (error: ApolloError) => void;
                successMessage?: string;
                errorMessage?: string | ((error: ApolloError) => string);
            } = {}
        ) => {
            return async (e?: React.BaseSyntheticEvent) => {
                e?.preventDefault();

                await handleSubmit(async (data) => {
                    try {
                        const variables = options.variables ? options.variables(data) : (data as unknown as TVariables);

                        const result = await mutationFn({variables});

                        if (result.data) {
                            if (options.onCompleted) {
                                await options.onCompleted(result.data);
                            }

                            if (showSuccessToast && options.successMessage) {
                                toast.success(options.successMessage);
                            }
                        } else if (result.errors && result.errors.length > 0) {
                            // Handle GraphQL errors that don't throw
                            const error = new ApolloError({
                                graphQLErrors: [...result.errors],
                            });
                            throw error;
                        }
                    } catch (error) {
                        if (error instanceof ApolloError) {
                            // Handle GraphQL field errors
                            if (error.graphQLErrors?.length > 0) {
                                const fieldErrors: Record<string, string> = {};

                                error.graphQLErrors.forEach((gqlError) => {
                                    if (gqlError.extensions?.field) {
                                        fieldErrors[gqlError.extensions.field as string] = gqlError.message;
                                    }
                                });

                                setFieldErrors(fieldErrors);
                            }

                            if (options.onError) {
                                options.onError(error);
                            } else if (showErrorToast) {
                                const message = typeof options.errorMessage === 'function'
                                    ? options.errorMessage(error)
                                    : options.errorMessage ?? 'An error occurred';
                                toast.error(message);
                            }
                        }

                        throw error;
                    }
                })();
            };
        },
        [handleSubmit, showSuccessToast, showErrorToast]
    );

    // Set field-level error
    const setFieldError = useCallback((field: keyof z.infer<T>, message: string) => {
        setFieldErrors(prev => ({...prev, [field as string]: message}));
        form.setError(field as any, {message});
    }, [form]);

    // Clear field-level error
    const clearFieldError = useCallback((field: keyof z.infer<T>) => {
        setFieldErrors(prev => {
            const next = {...prev};
            delete next[field as string];
            return next;
        });
        form.clearErrors(field as any);
    }, [form]);

    // Reset form and clear persisted data
    const resetForm = useCallback(() => {
        form.reset();
        setFieldErrors({});
        setFormState({
            isSubmitting: false,
            isSubmitted: false,
            submitCount: 0,
            isSuccessful: false,
            errors: {}
        });

        if (persistKey) {
            localStorage.removeItem(`form_draft_${persistKey}`);
        }
    }, [form, persistKey]);

    // Merge form states
    const mergedFormState = {
        ...form.formState,
        ...formState,
        errors: {
            ...Object.entries(form.formState.errors).reduce((acc, [key, error]) => ({
                ...acc,
                [key]: error?.message ?? ''
            }), {}),
            ...fieldErrors
        }
    };


    return {
        ...form,
        formState: mergedFormState,
        handleSubmit,
        handleGraphQLSubmit,
        setFieldError,
        clearFieldError,
        resetForm,
        //trigger: debounceMs > 0 ? validateField : form.trigger,
        trigger: (async (name: Parameters<typeof form.trigger>[0]) => {
            if (debounceMs > 0 && name !== undefined) {
                return validateField(name);
            }
            return form.trigger(name);
        }) as typeof form.trigger,
    };
}