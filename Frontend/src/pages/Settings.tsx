import React, { useState } from 'react';
import { User, Lock, Bell, Palette, Moon, Sun } from 'lucide-react';
import {
    Button,
    Card,
    CardHeader,
    CardTitle,
    CardContent,
    Input,
    useToast
} from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services';
import './Settings.css';

export function Settings() {
    const { user } = useAuth();
    const { toast } = useToast();

    const [activeTab, setActiveTab] = useState('profile');
    const [isDarkMode, setIsDarkMode] = useState(
        document.documentElement.getAttribute('data-theme') === 'dark'
    );

    // Password change state
    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });
    const [isChangingPassword, setIsChangingPassword] = useState(false);

    const handleThemeToggle = () => {
        const newTheme = isDarkMode ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        setIsDarkMode(!isDarkMode);
    };

    const handlePasswordChange = async (e: React.FormEvent) => {
        e.preventDefault();

        if (passwordData.newPassword !== passwordData.confirmPassword) {
            toast.error('Les mots de passe ne correspondent pas');
            return;
        }

        if (passwordData.newPassword.length < 8) {
            toast.error('Le mot de passe doit contenir au moins 8 caractères');
            return;
        }

        // Check password complexity
        const hasUppercase = /[A-Z]/.test(passwordData.newPassword);
        const hasLowercase = /[a-z]/.test(passwordData.newPassword);
        const hasDigit = /\d/.test(passwordData.newPassword);

        if (!hasUppercase || !hasLowercase || !hasDigit) {
            toast.error('Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre');
            return;
        }

        try {
            setIsChangingPassword(true);
            await authService.changePassword({
                currentPassword: passwordData.currentPassword,
                newPassword: passwordData.newPassword,
                confirmPassword: passwordData.confirmPassword
            });
            toast.success('Mot de passe modifié avec succès');
            setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
        } catch (error) {
            toast.error('Erreur lors du changement de mot de passe');
        } finally {
            setIsChangingPassword(false);
        }
    };

    const tabs = [
        { id: 'profile', label: 'Profil', icon: User },
        { id: 'security', label: 'Sécurité', icon: Lock },
        { id: 'notifications', label: 'Notifications', icon: Bell },
        { id: 'appearance', label: 'Apparence', icon: Palette },
    ];

    return (
        <div className="settings-page">
            {/* Header */}
            <div className="page-header">
                <div className="page-header-content">
                    <h1 className="page-title">Paramètres</h1>
                    <p className="page-description">Gérez votre compte et vos préférences</p>
                </div>
            </div>

            <div className="settings-layout">
                {/* Sidebar */}
                <div className="settings-sidebar">
                    <nav className="settings-nav">
                        {tabs.map((tab) => (
                            <button
                                key={tab.id}
                                className={`settings-nav-item ${activeTab === tab.id ? 'active' : ''}`}
                                onClick={() => setActiveTab(tab.id)}
                            >
                                <tab.icon size={18} />
                                <span>{tab.label}</span>
                            </button>
                        ))}
                    </nav>
                </div>

                {/* Content */}
                <div className="settings-content">
                    {activeTab === 'profile' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Informations du profil</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="settings-form">
                                    <div className="settings-form-row">
                                        <Input
                                            label="Prénom"
                                            value={user?.firstName || ''}
                                            disabled
                                        />
                                        <Input
                                            label="Nom"
                                            value={user?.lastName || ''}
                                            disabled
                                        />
                                    </div>
                                    <Input
                                        label="Email"
                                        type="email"
                                        value={user?.email || ''}
                                        disabled
                                    />
                                    <Input
                                        label="Téléphone"
                                        value={user?.phone || ''}
                                        disabled
                                    />
                                    <p className="text-sm text-secondary mt-2">
                                        Contactez l'administrateur pour modifier ces informations.
                                    </p>
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {activeTab === 'security' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Changer le mot de passe</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <form className="settings-form" onSubmit={handlePasswordChange}>
                                    <Input
                                        label="Mot de passe actuel"
                                        type="password"
                                        value={passwordData.currentPassword}
                                        onChange={(e) => setPasswordData(p => ({ ...p, currentPassword: e.target.value }))}
                                        required
                                    />
                                    <Input
                                        label="Nouveau mot de passe"
                                        type="password"
                                        value={passwordData.newPassword}
                                        onChange={(e) => setPasswordData(p => ({ ...p, newPassword: e.target.value }))}
                                        hint="Minimum 8 caractères"
                                        required
                                    />
                                    <Input
                                        label="Confirmer le mot de passe"
                                        type="password"
                                        value={passwordData.confirmPassword}
                                        onChange={(e) => setPasswordData(p => ({ ...p, confirmPassword: e.target.value }))}
                                        required
                                    />
                                    <Button
                                        type="submit"
                                        variant="primary"
                                        isLoading={isChangingPassword}
                                    >
                                        Modifier le mot de passe
                                    </Button>
                                </form>
                            </CardContent>
                        </Card>
                    )}

                    {activeTab === 'notifications' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Préférences de notification</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-secondary">
                                    Les paramètres de notification seront disponibles prochainement.
                                </p>
                            </CardContent>
                        </Card>
                    )}

                    {activeTab === 'appearance' && (
                        <Card>
                            <CardHeader>
                                <CardTitle>Thème</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="theme-toggle-section">
                                    <div className="theme-info">
                                        <span className="theme-label">Mode sombre</span>
                                        <span className="theme-description">
                                            Activez le mode sombre pour réduire la fatigue oculaire
                                        </span>
                                    </div>
                                    <button
                                        className={`theme-toggle-btn ${isDarkMode ? 'dark' : 'light'}`}
                                        onClick={handleThemeToggle}
                                    >
                                        {isDarkMode ? <Moon size={20} /> : <Sun size={20} />}
                                        <span>{isDarkMode ? 'Sombre' : 'Clair'}</span>
                                    </button>
                                </div>
                            </CardContent>
                        </Card>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Settings;
