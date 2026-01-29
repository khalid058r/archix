import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff, User, Phone } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { Button, Input } from '../../components/ui';
import './Auth.css';

export function Register() {
    const navigate = useNavigate();
    const { register } = useAuth();

    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: '',
    });
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [errors, setErrors] = useState<Record<string, string>>({});

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        setErrors((prev) => ({ ...prev, [name]: '' }));
    };

    const validate = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.firstName.trim()) {
            newErrors.firstName = 'Le prénom est requis';
        }
        if (!formData.lastName.trim()) {
            newErrors.lastName = 'Le nom est requis';
        }
        if (!formData.email.trim()) {
            newErrors.email = 'L\'email est requis';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = 'Email invalide';
        }
        if (!formData.password) {
            newErrors.password = 'Le mot de passe est requis';
        } else if (formData.password.length < 8) {
            newErrors.password = 'Le mot de passe doit contenir au moins 8 caractères';
        } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/.test(formData.password)) {
            newErrors.password = 'Le mot de passe doit contenir une majuscule, une minuscule et un chiffre';
        }
        if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Les mots de passe ne correspondent pas';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validate()) return;

        setIsLoading(true);

        try {
            await register({
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email,
                phone: formData.phone || undefined,
                password: formData.password,
            });
            navigate('/');
        } catch (err: any) {
            setErrors({
                submit: err.response?.data?.message || 'Erreur lors de l\'inscription',
            });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                {/* Left side - Branding */}
                <div className="auth-branding">
                    <div className="auth-branding-content">
                        <div className="auth-logo">
                            <span className="auth-logo-icon">A</span>
                            <span className="auth-logo-text">ARCHIX-BASE</span>
                        </div>
                        <h1 className="auth-branding-title">
                            Rejoignez Archix-Base
                        </h1>
                        <p className="auth-branding-description">
                            Créez votre compte pour commencer à gérer vos documents
                            de manière efficace et sécurisée.
                        </p>
                        <ul className="auth-features">
                            <li>✓ Configuration rapide</li>
                            <li>✓ Interface intuitive</li>
                            <li>✓ Support multilingue</li>
                            <li>✓ Sécurité renforcée</li>
                        </ul>
                    </div>
                </div>

                {/* Right side - Form */}
                <div className="auth-form-container">
                    <div className="auth-form-wrapper">
                        <div className="auth-form-header">
                            <h2 className="auth-form-title">Inscription</h2>
                            <p className="auth-form-subtitle">
                                Créez votre compte en quelques étapes
                            </p>
                        </div>

                        {errors.submit && (
                            <div className="auth-error">
                                {errors.submit}
                            </div>
                        )}

                        <form className="auth-form" onSubmit={handleSubmit}>
                            <div className="auth-form-row">
                                <Input
                                    label="Prénom"
                                    type="text"
                                    name="firstName"
                                    placeholder="Jean"
                                    value={formData.firstName}
                                    onChange={handleChange}
                                    leftIcon={<User size={18} />}
                                    error={errors.firstName}
                                    required
                                />

                                <Input
                                    label="Nom"
                                    type="text"
                                    name="lastName"
                                    placeholder="Dupont"
                                    value={formData.lastName}
                                    onChange={handleChange}
                                    leftIcon={<User size={18} />}
                                    error={errors.lastName}
                                    required
                                />
                            </div>

                            <Input
                                label="Email"
                                type="email"
                                name="email"
                                placeholder="jean.dupont@email.com"
                                value={formData.email}
                                onChange={handleChange}
                                leftIcon={<Mail size={18} />}
                                error={errors.email}
                                required
                            />

                            <Input
                                label="Téléphone (optionnel)"
                                type="tel"
                                name="phone"
                                placeholder="+33 6 12 34 56 78"
                                value={formData.phone}
                                onChange={handleChange}
                                leftIcon={<Phone size={18} />}
                            />

                            <Input
                                label="Mot de passe"
                                type={showPassword ? 'text' : 'password'}
                                name="password"
                                placeholder="••••••••"
                                value={formData.password}
                                onChange={handleChange}
                                leftIcon={<Lock size={18} />}
                                rightIcon={
                                    <button
                                        type="button"
                                        className="auth-password-toggle"
                                        onClick={() => setShowPassword(!showPassword)}
                                        tabIndex={-1}
                                    >
                                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </button>
                                }
                                error={errors.password}
                                hint="8 chars min, majuscule, minuscule, chiffre"
                                required
                            />

                            <Input
                                label="Confirmer le mot de passe"
                                type={showPassword ? 'text' : 'password'}
                                name="confirmPassword"
                                placeholder="••••••••"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                leftIcon={<Lock size={18} />}
                                error={errors.confirmPassword}
                                required
                            />

                            <Button
                                type="submit"
                                variant="primary"
                                fullWidth
                                isLoading={isLoading}
                            >
                                Créer mon compte
                            </Button>
                        </form>

                        <div className="auth-footer">
                            <span>Déjà un compte ?</span>
                            <Link to="/login" className="auth-link">
                                Se connecter
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Register;
