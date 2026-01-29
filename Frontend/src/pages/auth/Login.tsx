import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { Button, Input } from '../../components/ui';
import './Auth.css';

export function Login() {
    const navigate = useNavigate();
    const { login } = useAuth();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        setError(null);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);

        try {
            await login(formData);
            navigate('/');
        } catch (err: any) {
            setError(err.response?.data?.message || 'Identifiants incorrects');
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
                            Système de Gestion Documentaire Moderne
                        </h1>
                        <p className="auth-branding-description">
                            Centralisez, sécurisez et rationalisez vos flux documentaires
                            avec une solution intuitive et performante.
                        </p>
                        <ul className="auth-features">
                            <li>✓ Gestion des documents et métadonnées</li>
                            <li>✓ Contrôle d'accès basé sur les rôles</li>
                            <li>✓ Recherche full-text intelligente</li>
                            <li>✓ Collaboration en temps réel</li>
                        </ul>
                    </div>
                </div>

                {/* Right side - Form */}
                <div className="auth-form-container">
                    <div className="auth-form-wrapper">
                        <div className="auth-form-header">
                            <h2 className="auth-form-title">Connexion</h2>
                            <p className="auth-form-subtitle">
                                Connectez-vous pour accéder à votre espace documentaire
                            </p>
                        </div>

                        {error && (
                            <div className="auth-error">
                                {error}
                            </div>
                        )}

                        <form className="auth-form" onSubmit={handleSubmit}>
                            <Input
                                label="Email"
                                type="email"
                                name="email"
                                placeholder="votre@email.com"
                                value={formData.email}
                                onChange={handleChange}
                                leftIcon={<Mail size={18} />}
                                required
                            />

                            <div className="auth-password-field">
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
                                    required
                                />
                            </div>

                            <div className="auth-options">
                                <label className="auth-remember">
                                    <input type="checkbox" />
                                    <span>Se souvenir de moi</span>
                                </label>
                                <a href="/forgot-password" className="auth-forgot">
                                    Mot de passe oublié ?
                                </a>
                            </div>

                            <Button
                                type="submit"
                                variant="primary"
                                fullWidth
                                isLoading={isLoading}
                            >
                                Se connecter
                            </Button>
                        </form>

                        <div className="auth-footer">
                            <span>Pas encore de compte ?</span>
                            <Link to="/register" className="auth-link">
                                Créer un compte
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Login;
