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
import { useLoginMutation } from '../../api/endpoints/authApi';
import { useAppDispatch } from '../../store/hooks';
import { setCredentials, setOrganizations } from '../../store/slices/authSlice';
import { organizationService } from '../../services/organization.service';

const loginSchema = z.object({
    email: z.string().email('Email invalide'),
    password: z.string().min(1, 'Mot de passe requis'),
    rememberMe: z.boolean().optional(),
});

type LoginFormValues = z.infer<typeof loginSchema>;

const LoginPage = () => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const [showPassword, setShowPassword] = useState(false);
    const [login, { isLoading }] = useLoginMutation();

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: '',
            password: '',
            rememberMe: false,
        },
    });

    const onSubmit = async (data: LoginFormValues) => {
        try {
            const response = await login(data).unwrap();
            dispatch(setCredentials({ user: response.user, accessToken: response.token }));

            // Fetch and set organizations after login
            try {
                const orgs = await organizationService.getAll();
                const orgSummaries = orgs.map(o => ({ id: o.id, name: o.name, slug: o.slug, description: o.description, logoUrl: o.logoUrl }));
                dispatch(setCredentials({ user: response.user, accessToken: response.token, organizations: orgSummaries }));
            } catch (orgErr) {
                console.warn('Failed to fetch organizations:', orgErr);
            }

            toast.success(`Bienvenue, ${response.user.firstName} !`);
            navigate('/dashboard');
        } catch (err: any) {
            console.error('Login failed', err);
            const msg = err.data?.message || 'Échec de la connexion';
            toast.error(msg);
        }
    };

    return (
        <Card className="w-full">
            <div className="mb-6 text-center">
                <h2 className="text-xl font-semibold text-gray-900">Connexion</h2>
                <p className="mt-1 text-sm text-gray-600">Accédez à votre espace sécurisé</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <Input
                    label="Email"
                    type="email"
                    placeholder="votre.email@entreprise.com"
                    error={errors.email?.message}
                    {...register('email')}
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

                <div className="flex items-center justify-between">
                    <label className="flex items-center space-x-2 cursor-pointer">
                        <input
                            type="checkbox"
                            className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                            {...register('rememberMe')}
                        />
                        <span className="text-sm text-gray-600">Se souvenir de moi</span>
                    </label>
                    <Link
                        to="/forgot-password"
                        className="text-sm font-medium text-primary hover:text-primary-dark hover:underline"
                    >
                        Mot de passe oublié ?
                    </Link>
                </div>

                <Button
                    type="submit"
                    className="w-full"
                    isLoading={isLoading}
                >
                    SE CONNECTER
                </Button>

                <div className="mt-6 text-center text-sm text-gray-600">
                    Pas encore de compte ?{' '}
                    <Link
                        to="/register"
                        className="font-medium text-primary hover:text-primary-dark hover:underline"
                    >
                        Créer un compte
                    </Link>
                </div>
            </form>
        </Card>
    );
};

export default LoginPage;
