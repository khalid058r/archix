import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { toast } from 'react-hot-toast';

import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { Button } from '../../components/ui/Button/Button';
import { useRegisterMutation } from '../../api/endpoints/authApi';

// Validation Schema
const registerSchema = z.object({
    firstName: z.string().min(2, 'Le prénom doit contenir au moins 2 caractères'),
    lastName: z.string().min(2, 'Le nom doit contenir au moins 2 caractères'),
    email: z.string().email('Email invalide'),
    phone: z.string().optional(),
    password: z.string()
        .min(8, 'Minimum 8 caractères')
        .regex(/[A-Z]/, 'Au moins 1 majuscule')
        .regex(/[a-z]/, 'Au moins 1 minuscule')
        .regex(/[0-9]/, 'Au moins 1 chiffre')
        .regex(/[^A-Za-z0-9]/, 'Au moins 1 caractère spécial'),
    confirmPassword: z.string(),
    acceptTerms: z.boolean().refine(val => val === true, {
        message: 'Vous devez accepter les conditions générales'
    }),
}).refine((data) => data.password === data.confirmPassword, {
    message: "Les mots de passe ne correspondent pas",
    path: ["confirmPassword"],
});

type RegisterFormValues = z.infer<typeof registerSchema>;

const RegisterPage = () => {
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const [registerUser, { isLoading }] = useRegisterMutation();

    const {
        register,
        handleSubmit,
        formState: { errors },
        watch
    } = useForm<RegisterFormValues>({
        resolver: zodResolver(registerSchema),
        defaultValues: {
            firstName: '',
            lastName: '',
            email: '',
            phone: '',
            password: '',
            confirmPassword: '',
            acceptTerms: undefined,
        },
    });

    const password = watch('password', '');

    const onSubmit = async (data: RegisterFormValues) => {
        try {
            await registerUser({
                firstName: data.firstName,
                lastName: data.lastName,
                email: data.email,
                phone: data.phone,
                password: data.password,
                confirmPassword: data.confirmPassword
            }).unwrap();

            toast.success('Compte créé avec succès !');
            navigate('/login');
        } catch (err: any) {
            console.error('Registration failed', err);
            const msg = err.data?.message || 'Erreur lors de l\'inscription';
            toast.error(msg);
        }
    };

    const PasswordRequirement = ({ met, text }: { met: boolean, text: string }) => (
        <div className={`flex items-center text-xs ${met ? 'text-success' : 'text-gray-500'}`}>
            <span className="mr-1.5">{met ? '✓' : '•'}</span>
            {text}
        </div>
    );

    const hasMinLength = password.length >= 8;
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecial = /[^A-Za-z0-9]/.test(password);

    return (
        <Card className="w-full max-w-lg mx-auto">
            <div className="mb-6 text-center">
                <h2 className="text-xl font-semibold text-gray-900">Créer un compte</h2>
                <p className="mt-1 text-sm text-gray-600">Rejoignez Archix-Base dès aujourd'hui</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                    <Input
                        label="Prénom"
                        placeholder="Jean"
                        error={errors.firstName?.message}
                        {...register('firstName')}
                    />
                    <Input
                        label="Nom"
                        placeholder="Dupont"
                        error={errors.lastName?.message}
                        {...register('lastName')}
                    />
                </div>

                <Input
                    label="Email"
                    type="email"
                    placeholder="votre.email@entreprise.com"
                    error={errors.email?.message}
                    {...register('email')}
                />

                <Input
                    label="Téléphone (Optionnel)"
                    type="tel"
                    placeholder="+33 6 12 34 56 78"
                    error={errors.phone?.message}
                    {...register('phone')}
                />

                <div className="relative">
                    <Input
                        label="Mot de passe"
                        type={showPassword ? 'text' : 'password'}
                        placeholder="••••••••"
                        error={errors.password?.message}
                        {...register('password')}
                    />
                    <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-[34px] text-gray-500 hover:text-gray-700 focus:outline-none"
                    >
                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                </div>

                {/* Password Strength Indicators */}
                <div className="bg-gray-50 p-3 rounded-md grid grid-cols-2 gap-2 mt-1 mb-2">
                    <PasswordRequirement met={hasMinLength} text="Min. 8 caractères" />
                    <PasswordRequirement met={hasUpper} text="1 majuscule" />
                    <PasswordRequirement met={hasLower} text="1 minuscule" />
                    <PasswordRequirement met={hasNumber} text="1 chiffre" />
                    <PasswordRequirement met={hasSpecial} text="1 caractère spécial" />
                </div>

                <div className="relative">
                    <Input
                        label="Confirmer le mot de passe"
                        type={showConfirmPassword ? 'text' : 'password'}
                        placeholder="••••••••"
                        error={errors.confirmPassword?.message}
                        {...register('confirmPassword')}
                    />
                    <button
                        type="button"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        className="absolute right-3 top-[34px] text-gray-500 hover:text-gray-700 focus:outline-none"
                    >
                        {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                </div>

                <div className="flex items-start">
                    <div className="flex h-5 items-center">
                        <input
                            id="cgu"
                            type="checkbox"
                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                            {...register('acceptTerms')}
                        />
                    </div>
                    <div className="ml-3 text-sm">
                        <label htmlFor="cgu" className="text-gray-600">
                            J'accepte les <a href="#" className="font-medium text-primary hover:underline">Conditions Générales d'Utilisation</a>
                        </label>
                        {errors.acceptTerms && (
                            <p className="mt-1 text-xs text-error">{errors.acceptTerms.message}</p>
                        )}
                    </div>
                </div>

                <Button
                    type="submit"
                    className="w-full"
                    isLoading={isLoading}
                >
                    CRÉER MON COMPTE
                </Button>

                <div className="mt-6 text-center text-sm text-gray-600">
                    Déjà un compte ?{' '}
                    <Link
                        to="/login"
                        className="font-medium text-primary hover:text-primary-dark hover:underline"
                    >
                        Se connecter
                    </Link>
                </div>
            </form>
        </Card>
    );
};

export default RegisterPage;
