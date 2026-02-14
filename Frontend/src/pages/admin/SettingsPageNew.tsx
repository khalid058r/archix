import React, { useState } from 'react';
import {
    Settings, Save, Building2, Shield, Mail, HardDrive, FileText,
    Bell, Palette, AlertTriangle
} from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import toast from 'react-hot-toast';
import { useAppSelector } from '../../store/hooks';
import { useBulkUpdateSettingsMutation } from '../../api/endpoints/settingsApi';

interface SettingsSection {
    id: string;
    label: string;
    icon: React.ReactNode;
}

const sections: SettingsSection[] = [
    { id: 'general', label: 'Général', icon: <Settings size={18} /> },
    { id: 'organization', label: 'Organisation', icon: <Building2 size={18} /> },
    { id: 'security', label: 'Sécurité', icon: <Shield size={18} /> },
    { id: 'email', label: 'Email', icon: <Mail size={18} /> },
    { id: 'storage', label: 'Stockage', icon: <HardDrive size={18} /> },
    { id: 'documents', label: 'Documents', icon: <FileText size={18} /> },
    { id: 'notifications', label: 'Notifications', icon: <Bell size={18} /> },
    { id: 'appearance', label: 'Apparence', icon: <Palette size={18} /> },
];

export const SettingsPageNew = () => {
    const currentOrg = useAppSelector((state) => state.auth.currentOrganization);
    const [activeSection, setActiveSection] = useState('general');
    const [hasChanges, setHasChanges] = useState(false);
    const [bulkUpdate] = useBulkUpdateSettingsMutation();

    // Settings state
    const [settings, setSettings] = useState({
        // General
        appName: 'Archix',
        language: 'fr',
        timezone: 'Europe/Paris',
        dateFormat: 'DD/MM/YYYY',

        // Organization
        orgName: currentOrg?.name || '',
        orgDescription: '',
        orgLogo: '',
        supportEmail: 'support@archix.com',

        // Security
        passwordMinLength: 8,
        passwordRequireUppercase: true,
        passwordRequireNumbers: true,
        passwordRequireSpecial: true,
        sessionTimeout: 30,
        maxLoginAttempts: 5,
        lockoutDuration: 15,
        mfaEnabled: false,
        mfaRequired: false,

        // Email
        smtpHost: '',
        smtpPort: 587,
        smtpUser: '',
        smtpPassword: '',
        emailFrom: 'noreply@archix.com',
        emailFromName: 'Archix',

        // Storage
        maxFileSize: 100,
        allowedMimeTypes: 'pdf,docx,xlsx,png,jpg,jpeg,gif',
        defaultQuota: 10,
        compressionEnabled: true,

        // Documents
        defaultRetention: 365,
        autoArchive: true,
        archiveAfterDays: 365,
        requireApproval: false,
        versioningEnabled: true,
        maxVersions: 10,

        // Notifications
        emailNotifications: true,
        documentUploaded: true,
        documentApproved: true,
        documentRejected: true,
        newUserJoined: true,
        weeklyDigest: false,

        // Appearance
        primaryColor: '#6366f1',
        accentColor: '#22c55e',
        darkMode: false,
        compactMode: false,
    });

    const updateSetting = (key: string, value: unknown) => {
        setSettings({ ...settings, [key]: value });
        setHasChanges(true);
    };

    const handleSave = async () => {
        try {
            // Flatten settings to key-value pairs for the API
            const flatSettings: Record<string, string> = {};
            Object.entries(settings).forEach(([key, value]) => {
                flatSettings[key] = String(value);
            });
            await bulkUpdate(flatSettings).unwrap();
            toast.success('Paramètres sauvegardés');
            setHasChanges(false);
        } catch (error) {
            toast.error('Erreur lors de la sauvegarde');
        }
    };

    const renderSection = () => {
        switch (activeSection) {
            case 'general':
                return (
                    <div className="space-y-6">
                        <div>
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Paramètres généraux</h3>
                            <div className="grid gap-4 max-w-xl">
                                <Input
                                    label="Nom de l'application"
                                    value={settings.appName}
                                    onChange={(e) => updateSetting('appName', e.target.value)}
                                />
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Langue</label>
                                    <select
                                        value={settings.language}
                                        onChange={(e) => updateSetting('language', e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                                    >
                                        <option value="fr">Français</option>
                                        <option value="en">English</option>
                                        <option value="es">Español</option>
                                        <option value="de">Deutsch</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Fuseau horaire</label>
                                    <select
                                        value={settings.timezone}
                                        onChange={(e) => updateSetting('timezone', e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                                    >
                                        <option value="Europe/Paris">Europe/Paris (UTC+1)</option>
                                        <option value="Europe/London">Europe/London (UTC+0)</option>
                                        <option value="America/New_York">America/New_York (UTC-5)</option>
                                        <option value="Asia/Tokyo">Asia/Tokyo (UTC+9)</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Format de date</label>
                                    <select
                                        value={settings.dateFormat}
                                        onChange={(e) => updateSetting('dateFormat', e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                                    >
                                        <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                                        <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                                        <option value="YYYY-MM-DD">YYYY-MM-DD</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                );

            case 'organization':
                return (
                    <div className="space-y-6">
                        <div>
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Informations de l'organisation</h3>
                            <div className="grid gap-4 max-w-xl">
                                <Input
                                    label="Nom de l'organisation"
                                    value={settings.orgName}
                                    onChange={(e) => updateSetting('orgName', e.target.value)}
                                />
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                                    <textarea
                                        value={settings.orgDescription}
                                        onChange={(e) => updateSetting('orgDescription', e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50 h-24 resize-none"
                                        placeholder="Description de votre organisation..."
                                    />
                                </div>
                                <Input
                                    label="URL du logo"
                                    value={settings.orgLogo}
                                    onChange={(e) => updateSetting('orgLogo', e.target.value)}
                                    placeholder="https://..."
                                />
                                <Input
                                    label="Email de support"
                                    type="email"
                                    value={settings.supportEmail}
                                    onChange={(e) => updateSetting('supportEmail', e.target.value)}
                                />
                            </div>
                        </div>
                    </div>
                );

            case 'security':
                return (
                    <div className="space-y-6">
                        <div>
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Politique de mot de passe</h3>
                            <div className="grid gap-4 max-w-xl">
                                <Input
                                    label="Longueur minimale"
                                    type="number"
                                    value={settings.passwordMinLength}
                                    onChange={(e) => updateSetting('passwordMinLength', parseInt(e.target.value))}
                                    min={6}
                                    max={32}
                                />
                                <div className="space-y-3">
                                    <label className="flex items-center gap-3 cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.passwordRequireUppercase}
                                            onChange={(e) => updateSetting('passwordRequireUppercase', e.target.checked)}
                                            className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                                        />
                                        <span className="text-sm text-gray-700">Exiger une majuscule</span>
                                    </label>
                                    <label className="flex items-center gap-3 cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.passwordRequireNumbers}
                                            onChange={(e) => updateSetting('passwordRequireNumbers', e.target.checked)}
                                            className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                                        />
                                        <span className="text-sm text-gray-700">Exiger un chiffre</span>
                                    </label>
                                    <label className="flex items-center gap-3 cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.passwordRequireSpecial}
                                            onChange={(e) => updateSetting('passwordRequireSpecial', e.target.checked)}
                                            className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                                        />
                                        <span className="text-sm text-gray-700">Exiger un caractère spécial</span>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div className="border-t pt-6">
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Session et connexion</h3>
                            <div className="grid gap-4 max-w-xl">
                                <Input
                                    label="Délai d'expiration de session (minutes)"
                                    type="number"
                                    value={settings.sessionTimeout}
                                    onChange={(e) => updateSetting('sessionTimeout', parseInt(e.target.value))}
                                    min={5}
                                    max={480}
                                />
                                <Input
                                    label="Tentatives de connexion max"
                                    type="number"
                                    value={settings.maxLoginAttempts}
                                    onChange={(e) => updateSetting('maxLoginAttempts', parseInt(e.target.value))}
                                    min={3}
                                    max={10}
                                />
                                <Input
                                    label="Durée de verrouillage (minutes)"
                                    type="number"
                                    value={settings.lockoutDuration}
                                    onChange={(e) => updateSetting('lockoutDuration', parseInt(e.target.value))}
                                    min={5}
                                    max={60}
                                />
                            </div>
                        </div>

                        <div className="border-t pt-6">
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Authentification à deux facteurs (2FA)</h3>
                            <div className="space-y-4 max-w-xl">
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div>
                                        <div className="font-medium text-gray-900">Activer la 2FA</div>
                                        <div className="text-sm text-gray-500">Permettre aux utilisateurs d'activer la 2FA</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.mfaEnabled}
                                            onChange={(e) => updateSetting('mfaEnabled', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>
                                {settings.mfaEnabled && (
                                    <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                        <div>
                                            <div className="font-medium text-gray-900">2FA obligatoire</div>
                                            <div className="text-sm text-gray-500">Forcer tous les utilisateurs à utiliser la 2FA</div>
                                        </div>
                                        <label className="relative inline-flex items-center cursor-pointer">
                                            <input
                                                type="checkbox"
                                                checked={settings.mfaRequired}
                                                onChange={(e) => updateSetting('mfaRequired', e.target.checked)}
                                                className="sr-only peer"
                                            />
                                            <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                        </label>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                );

            case 'email':
                return (
                    <div className="space-y-6">
                        <div>
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Configuration SMTP</h3>
                            <div className="grid gap-4 max-w-xl">
                                <Input
                                    label="Serveur SMTP"
                                    value={settings.smtpHost}
                                    onChange={(e) => updateSetting('smtpHost', e.target.value)}
                                    placeholder="smtp.example.com"
                                />
                                <Input
                                    label="Port"
                                    type="number"
                                    value={settings.smtpPort}
                                    onChange={(e) => updateSetting('smtpPort', parseInt(e.target.value))}
                                />
                                <Input
                                    label="Utilisateur"
                                    value={settings.smtpUser}
                                    onChange={(e) => updateSetting('smtpUser', e.target.value)}
                                />
                                <Input
                                    label="Mot de passe"
                                    type="password"
                                    value={settings.smtpPassword}
                                    onChange={(e) => updateSetting('smtpPassword', e.target.value)}
                                />
                            </div>
                        </div>

                        <div className="border-t pt-6">
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Expéditeur</h3>
                            <div className="grid gap-4 max-w-xl">
                                <Input
                                    label="Email d'expédition"
                                    type="email"
                                    value={settings.emailFrom}
                                    onChange={(e) => updateSetting('emailFrom', e.target.value)}
                                />
                                <Input
                                    label="Nom d'expédition"
                                    value={settings.emailFromName}
                                    onChange={(e) => updateSetting('emailFromName', e.target.value)}
                                />
                                <Button variant="outline" onClick={() => toast.success('Email de test envoyé')}>
                                    Envoyer un email de test
                                </Button>
                            </div>
                        </div>
                    </div>
                );

            case 'storage':
                return (
                    <div className="space-y-6">
                        <div>
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Limites de fichiers</h3>
                            <div className="grid gap-4 max-w-xl">
                                <Input
                                    label="Taille maximale de fichier (MB)"
                                    type="number"
                                    value={settings.maxFileSize}
                                    onChange={(e) => updateSetting('maxFileSize', parseInt(e.target.value))}
                                    min={1}
                                    max={500}
                                />
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Types de fichiers autorisés
                                    </label>
                                    <textarea
                                        value={settings.allowedMimeTypes}
                                        onChange={(e) => updateSetting('allowedMimeTypes', e.target.value)}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50 h-20 resize-none font-mono"
                                        placeholder="pdf,docx,xlsx,png,jpg"
                                    />
                                    <p className="text-xs text-gray-500 mt-1">Extensions séparées par des virgules</p>
                                </div>
                                <Input
                                    label="Quota par défaut (GB)"
                                    type="number"
                                    value={settings.defaultQuota}
                                    onChange={(e) => updateSetting('defaultQuota', parseInt(e.target.value))}
                                    min={1}
                                    max={1000}
                                />
                            </div>
                        </div>

                        <div className="border-t pt-6">
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Optimisation</h3>
                            <div className="space-y-4 max-w-xl">
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div>
                                        <div className="font-medium text-gray-900">Compression automatique</div>
                                        <div className="text-sm text-gray-500">Compresser les images lors de l'upload</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.compressionEnabled}
                                            onChange={(e) => updateSetting('compressionEnabled', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                );

            case 'documents':
                return (
                    <div className="space-y-6">
                        <div>
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Rétention et archivage</h3>
                            <div className="grid gap-4 max-w-xl">
                                <Input
                                    label="Durée de conservation par défaut (jours)"
                                    type="number"
                                    value={settings.defaultRetention}
                                    onChange={(e) => updateSetting('defaultRetention', parseInt(e.target.value))}
                                    min={30}
                                    max={3650}
                                />
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div>
                                        <div className="font-medium text-gray-900">Archivage automatique</div>
                                        <div className="text-sm text-gray-500">Archiver les documents après une période d'inactivité</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.autoArchive}
                                            onChange={(e) => updateSetting('autoArchive', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>
                                {settings.autoArchive && (
                                    <Input
                                        label="Archiver après (jours d'inactivité)"
                                        type="number"
                                        value={settings.archiveAfterDays}
                                        onChange={(e) => updateSetting('archiveAfterDays', parseInt(e.target.value))}
                                        min={30}
                                        max={365}
                                    />
                                )}
                            </div>
                        </div>

                        <div className="border-t pt-6">
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Workflow</h3>
                            <div className="space-y-4 max-w-xl">
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div>
                                        <div className="font-medium text-gray-900">Validation requise</div>
                                        <div className="text-sm text-gray-500">Les documents doivent être approuvés avant publication</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.requireApproval}
                                            onChange={(e) => updateSetting('requireApproval', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div className="border-t pt-6">
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Versioning</h3>
                            <div className="space-y-4 max-w-xl">
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div>
                                        <div className="font-medium text-gray-900">Versioning activé</div>
                                        <div className="text-sm text-gray-500">Conserver l'historique des modifications</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.versioningEnabled}
                                            onChange={(e) => updateSetting('versioningEnabled', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>
                                {settings.versioningEnabled && (
                                    <Input
                                        label="Nombre max de versions"
                                        type="number"
                                        value={settings.maxVersions}
                                        onChange={(e) => updateSetting('maxVersions', parseInt(e.target.value))}
                                        min={1}
                                        max={100}
                                    />
                                )}
                            </div>
                        </div>
                    </div>
                );

            case 'notifications':
                return (
                    <div className="space-y-6">
                        <div>
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Notifications par email</h3>
                            <div className="space-y-4 max-w-xl">
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div>
                                        <div className="font-medium text-gray-900">Activer les notifications email</div>
                                        <div className="text-sm text-gray-500">Envoyer des emails pour les événements importants</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.emailNotifications}
                                            onChange={(e) => updateSetting('emailNotifications', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>

                                {settings.emailNotifications && (
                                    <div className="space-y-3 pl-4 border-l-2 border-gray-200">
                                        {[
                                            { key: 'documentUploaded', label: 'Document uploadé' },
                                            { key: 'documentApproved', label: 'Document approuvé' },
                                            { key: 'documentRejected', label: 'Document rejeté' },
                                            { key: 'newUserJoined', label: 'Nouvel utilisateur' },
                                        ].map(item => (
                                            <label key={item.key} className="flex items-center gap-3 cursor-pointer">
                                                <input
                                                    type="checkbox"
                                                    checked={settings[item.key as keyof typeof settings] as boolean}
                                                    onChange={(e) => updateSetting(item.key, e.target.checked)}
                                                    className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                                                />
                                                <span className="text-sm text-gray-700">{item.label}</span>
                                            </label>
                                        ))}
                                    </div>
                                )}

                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg mt-4">
                                    <div>
                                        <div className="font-medium text-gray-900">Résumé hebdomadaire</div>
                                        <div className="text-sm text-gray-500">Recevoir un résumé chaque semaine</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.weeklyDigest}
                                            onChange={(e) => updateSetting('weeklyDigest', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                );

            case 'appearance':
                return (
                    <div className="space-y-6">
                        <div>
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Thème</h3>
                            <div className="grid gap-4 max-w-xl">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">Couleur principale</label>
                                    <div className="flex items-center gap-3">
                                        <input
                                            type="color"
                                            value={settings.primaryColor}
                                            onChange={(e) => updateSetting('primaryColor', e.target.value)}
                                            className="w-12 h-12 rounded-lg cursor-pointer border border-gray-200"
                                        />
                                        <Input
                                            value={settings.primaryColor}
                                            onChange={(e) => updateSetting('primaryColor', e.target.value)}
                                            className="w-32 font-mono"
                                        />
                                    </div>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">Couleur d'accent</label>
                                    <div className="flex items-center gap-3">
                                        <input
                                            type="color"
                                            value={settings.accentColor}
                                            onChange={(e) => updateSetting('accentColor', e.target.value)}
                                            className="w-12 h-12 rounded-lg cursor-pointer border border-gray-200"
                                        />
                                        <Input
                                            value={settings.accentColor}
                                            onChange={(e) => updateSetting('accentColor', e.target.value)}
                                            className="w-32 font-mono"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="border-t pt-6">
                            <h3 className="text-lg font-medium text-gray-900 mb-4">Interface</h3>
                            <div className="space-y-4 max-w-xl">
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div>
                                        <div className="font-medium text-gray-900">Mode sombre</div>
                                        <div className="text-sm text-gray-500">Utiliser un thème sombre</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.darkMode}
                                            onChange={(e) => updateSetting('darkMode', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>
                                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                    <div>
                                        <div className="font-medium text-gray-900">Mode compact</div>
                                        <div className="text-sm text-gray-500">Réduire les espacements pour afficher plus de contenu</div>
                                    </div>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={settings.compactMode}
                                            onChange={(e) => updateSetting('compactMode', e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                );

            default:
                return null;
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <Settings className="text-primary" />
                        Paramètres
                    </h1>
                    <p className="text-gray-500">Configuration de l'application et de l'organisation</p>
                </div>
                {hasChanges && (
                    <Button variant="primary" onClick={handleSave}>
                        <Save size={16} className="mr-2" />
                        Sauvegarder les modifications
                    </Button>
                )}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                {/* Sidebar */}
                <Card className="p-2 lg:col-span-1 h-fit">
                    <nav className="space-y-1">
                        {sections.map(section => (
                            <button
                                key={section.id}
                                onClick={() => setActiveSection(section.id)}
                                className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${activeSection === section.id
                                        ? 'bg-primary/10 text-primary'
                                        : 'text-gray-600 hover:bg-gray-100'
                                    }`}
                            >
                                {section.icon}
                                {section.label}
                            </button>
                        ))}
                    </nav>
                </Card>

                {/* Content */}
                <Card className="p-6 lg:col-span-3">
                    {renderSection()}
                </Card>
            </div>

            {/* Danger Zone */}
            <Card className="p-6 border-red-200">
                <div className="flex items-start gap-4">
                    <div className="p-3 bg-red-100 rounded-lg text-red-600">
                        <AlertTriangle size={24} />
                    </div>
                    <div className="flex-1">
                        <h3 className="text-lg font-semibold text-red-900">Zone de danger</h3>
                        <p className="text-sm text-red-700 mt-1">
                            Ces actions sont irréversibles et peuvent affecter tous les utilisateurs.
                        </p>
                        <div className="flex gap-3 mt-4">
                            <Button
                                variant="outline"
                                className="text-red-600 border-red-200 hover:bg-red-50"
                                onClick={() => toast('Fonctionnalité à venir')}
                            >
                                Réinitialiser les paramètres
                            </Button>
                            <Button
                                variant="outline"
                                className="text-red-600 border-red-200 hover:bg-red-50"
                                onClick={() => toast('Fonctionnalité à venir')}
                            >
                                Supprimer l'organisation
                            </Button>
                        </div>
                    </div>
                </div>
            </Card>
        </div>
    );
};

export default SettingsPageNew;
