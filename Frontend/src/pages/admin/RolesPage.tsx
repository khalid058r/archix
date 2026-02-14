import { useState } from 'react';
import { Shield, Info, Save, AlertTriangle } from 'lucide-react';
import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { RoleBadge } from '../../components/admin';
import toast from 'react-hot-toast';

interface Permission {
    key: string;
    label: string;
    description: string;
    category: string;
}

interface RolePermissions {
    [permission: string]: boolean;
}

interface Role {
    name: string;
    label: string;
    description: string;
    isSystem: boolean;
    permissions: RolePermissions;
}

const permissions: Permission[] = [
    // Organisations
    { key: 'org.view', label: 'Voir organisations', description: 'Voir la liste des organisations', category: 'Organisations' },
    { key: 'org.create', label: 'Créer organisations', description: 'Créer de nouvelles organisations', category: 'Organisations' },
    { key: 'org.edit', label: 'Modifier organisations', description: 'Modifier les paramètres des organisations', category: 'Organisations' },
    { key: 'org.delete', label: 'Supprimer organisations', description: 'Supprimer des organisations', category: 'Organisations' },
    
    // Utilisateurs
    { key: 'users.view', label: 'Voir utilisateurs', description: 'Voir la liste des utilisateurs', category: 'Utilisateurs' },
    { key: 'users.create', label: 'Créer utilisateurs', description: 'Créer de nouveaux utilisateurs', category: 'Utilisateurs' },
    { key: 'users.edit', label: 'Modifier utilisateurs', description: 'Modifier les profils utilisateurs', category: 'Utilisateurs' },
    { key: 'users.delete', label: 'Supprimer utilisateurs', description: 'Supprimer des utilisateurs', category: 'Utilisateurs' },
    { key: 'users.roles', label: 'Gérer rôles utilisateurs', description: 'Assigner/retirer des rôles', category: 'Utilisateurs' },
    
    // Départements
    { key: 'dept.view', label: 'Voir départements', description: 'Voir la liste des départements', category: 'Départements' },
    { key: 'dept.create', label: 'Créer départements', description: 'Créer de nouveaux départements', category: 'Départements' },
    { key: 'dept.edit', label: 'Modifier départements', description: 'Modifier les départements', category: 'Départements' },
    { key: 'dept.delete', label: 'Supprimer départements', description: 'Supprimer des départements', category: 'Départements' },
    
    // Documents
    { key: 'docs.view', label: 'Voir documents', description: 'Voir tous les documents', category: 'Documents' },
    { key: 'docs.create', label: 'Créer documents', description: 'Uploader des documents', category: 'Documents' },
    { key: 'docs.edit', label: 'Modifier documents', description: 'Modifier les métadonnées des documents', category: 'Documents' },
    { key: 'docs.delete', label: 'Supprimer documents', description: 'Supprimer des documents', category: 'Documents' },
    { key: 'docs.approve', label: 'Approuver documents', description: 'Valider/rejeter des documents', category: 'Documents' },
    { key: 'docs.download', label: 'Télécharger documents', description: 'Télécharger les fichiers', category: 'Documents' },
    
    // Namespaces
    { key: 'ns.view', label: 'Voir namespaces', description: 'Voir les espaces de noms', category: 'Namespaces' },
    { key: 'ns.create', label: 'Créer namespaces', description: 'Créer des espaces de noms', category: 'Namespaces' },
    { key: 'ns.edit', label: 'Modifier namespaces', description: 'Modifier les espaces de noms', category: 'Namespaces' },
    { key: 'ns.delete', label: 'Supprimer namespaces', description: 'Supprimer des espaces de noms', category: 'Namespaces' },
    
    // Système
    { key: 'audit.view', label: 'Voir audit logs', description: 'Consulter les journaux d\'audit', category: 'Système' },
    { key: 'settings.view', label: 'Voir paramètres', description: 'Consulter les paramètres', category: 'Système' },
    { key: 'settings.edit', label: 'Modifier paramètres', description: 'Modifier les paramètres système', category: 'Système' },
    { key: 'storage.manage', label: 'Gérer stockage', description: 'Gérer les quotas et le stockage', category: 'Système' },
];

const defaultRoles: Role[] = [
    {
        name: 'SUPER_ADMIN',
        label: 'Super Admin',
        description: 'Accès total au système, gestion multi-tenant',
        isSystem: true,
        permissions: Object.fromEntries(permissions.map(p => [p.key, true]))
    },
    {
        name: 'ADMIN',
        label: 'Admin',
        description: 'Administration complète de l\'organisation',
        isSystem: true,
        permissions: Object.fromEntries(permissions.filter(p => !p.key.startsWith('org.')).map(p => [p.key, true]))
    },
    {
        name: 'MANAGER',
        label: 'Manager',
        description: 'Gestion d\'équipe et validation de documents',
        isSystem: true,
        permissions: {
            'users.view': true, 'users.edit': false, 'users.create': false, 'users.delete': false, 'users.roles': false,
            'dept.view': true, 'dept.create': false, 'dept.edit': true, 'dept.delete': false,
            'docs.view': true, 'docs.create': true, 'docs.edit': true, 'docs.delete': true, 'docs.approve': true, 'docs.download': true,
            'ns.view': true, 'ns.create': true, 'ns.edit': true, 'ns.delete': false,
            'audit.view': false, 'settings.view': false, 'settings.edit': false, 'storage.manage': false,
            'org.view': false, 'org.create': false, 'org.edit': false, 'org.delete': false,
        }
    },
    {
        name: 'USER',
        label: 'Utilisateur',
        description: 'Utilisateur standard avec droits de création',
        isSystem: true,
        permissions: {
            'users.view': false, 'users.edit': false, 'users.create': false, 'users.delete': false, 'users.roles': false,
            'dept.view': true, 'dept.create': false, 'dept.edit': false, 'dept.delete': false,
            'docs.view': true, 'docs.create': true, 'docs.edit': true, 'docs.delete': true, 'docs.approve': false, 'docs.download': true,
            'ns.view': true, 'ns.create': false, 'ns.edit': false, 'ns.delete': false,
            'audit.view': false, 'settings.view': false, 'settings.edit': false, 'storage.manage': false,
            'org.view': false, 'org.create': false, 'org.edit': false, 'org.delete': false,
        }
    },
    {
        name: 'READER',
        label: 'Lecteur',
        description: 'Accès en lecture seule aux documents',
        isSystem: true,
        permissions: {
            'users.view': false, 'users.edit': false, 'users.create': false, 'users.delete': false, 'users.roles': false,
            'dept.view': true, 'dept.create': false, 'dept.edit': false, 'dept.delete': false,
            'docs.view': true, 'docs.create': false, 'docs.edit': false, 'docs.delete': false, 'docs.approve': false, 'docs.download': true,
            'ns.view': true, 'ns.create': false, 'ns.edit': false, 'ns.delete': false,
            'audit.view': false, 'settings.view': false, 'settings.edit': false, 'storage.manage': false,
            'org.view': false, 'org.create': false, 'org.edit': false, 'org.delete': false,
        }
    },
    {
        name: 'GUEST',
        label: 'Invité',
        description: 'Accès temporaire et limité',
        isSystem: true,
        permissions: {
            'users.view': false, 'users.edit': false, 'users.create': false, 'users.delete': false, 'users.roles': false,
            'dept.view': false, 'dept.create': false, 'dept.edit': false, 'dept.delete': false,
            'docs.view': true, 'docs.create': false, 'docs.edit': false, 'docs.delete': false, 'docs.approve': false, 'docs.download': false,
            'ns.view': true, 'ns.create': false, 'ns.edit': false, 'ns.delete': false,
            'audit.view': false, 'settings.view': false, 'settings.edit': false, 'storage.manage': false,
            'org.view': false, 'org.create': false, 'org.edit': false, 'org.delete': false,
        }
    },
];

const RolesPage = () => {
    const [roles, setRoles] = useState<Role[]>(defaultRoles);
    const [selectedRole, setSelectedRole] = useState<Role | null>(null);
    const [hasChanges, setHasChanges] = useState(false);

    const categories = [...new Set(permissions.map(p => p.category))];

    const handlePermissionChange = (permKey: string, value: boolean) => {
        if (!selectedRole || selectedRole.isSystem) return;
        
        setSelectedRole({
            ...selectedRole,
            permissions: {
                ...selectedRole.permissions,
                [permKey]: value
            }
        });
        setHasChanges(true);
    };

    const handleSave = () => {
        if (!selectedRole) return;
        
        setRoles(roles.map(r => r.name === selectedRole.name ? selectedRole : r));
        setHasChanges(false);
        toast.success('Permissions sauvegardées');
    };

    const getPermissionsByCategory = (category: string) => {
        return permissions.filter(p => p.category === category);
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-start">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <Shield className="text-primary" />
                        Gestion des Rôles & Permissions
                    </h1>
                    <p className="text-gray-500 mt-1">
                        Configurez les permissions pour chaque rôle de votre organisation
                    </p>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
                {/* Roles List */}
                <Card className="p-4 lg:col-span-1">
                    <h2 className="font-semibold text-gray-900 mb-4">Rôles</h2>
                    <div className="space-y-2">
                        {roles.map(role => (
                            <button
                                key={role.name}
                                onClick={() => setSelectedRole(role)}
                                className={`w-full text-left p-3 rounded-lg border transition-all ${
                                    selectedRole?.name === role.name
                                        ? 'border-primary bg-primary/5'
                                        : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                                }`}
                            >
                                <div className="flex items-center justify-between">
                                    <RoleBadge role={role.name} />
                                    {role.isSystem && (
                                        <span className="text-xs text-gray-400">Système</span>
                                    )}
                                </div>
                                <p className="text-xs text-gray-500 mt-2">{role.description}</p>
                            </button>
                        ))}
                    </div>
                </Card>

                {/* Permissions Matrix */}
                <Card className="p-6 lg:col-span-3">
                    {selectedRole ? (
                        <>
                            <div className="flex items-center justify-between mb-6">
                                <div>
                                    <div className="flex items-center gap-3">
                                        <RoleBadge role={selectedRole.name} size="md" />
                                        <h2 className="text-lg font-semibold text-gray-900">
                                            {selectedRole.label}
                                        </h2>
                                    </div>
                                    <p className="text-sm text-gray-500 mt-1">{selectedRole.description}</p>
                                </div>
                                {hasChanges && !selectedRole.isSystem && (
                                    <Button variant="primary" onClick={handleSave}>
                                        <Save size={16} className="mr-2" />
                                        Sauvegarder
                                    </Button>
                                )}
                            </div>

                            {selectedRole.isSystem && (
                                <div className="flex items-center gap-2 p-3 bg-yellow-50 border border-yellow-200 rounded-lg mb-6">
                                    <AlertTriangle size={18} className="text-yellow-600" />
                                    <span className="text-sm text-yellow-700">
                                        Les rôles système ne peuvent pas être modifiés
                                    </span>
                                </div>
                            )}

                            <div className="space-y-6">
                                {categories.map(category => (
                                    <div key={category}>
                                        <h3 className="font-medium text-gray-900 mb-3 pb-2 border-b">
                                            {category}
                                        </h3>
                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                            {getPermissionsByCategory(category).map(perm => {
                                                const hasPermission = selectedRole.permissions[perm.key] ?? false;
                                                
                                                return (
                                                    <div 
                                                        key={perm.key}
                                                        className={`flex items-center justify-between p-3 rounded-lg border ${
                                                            hasPermission ? 'bg-green-50 border-green-200' : 'bg-gray-50 border-gray-200'
                                                        }`}
                                                    >
                                                        <div className="flex-1 min-w-0">
                                                            <div className="font-medium text-sm text-gray-900">{perm.label}</div>
                                                            <div className="text-xs text-gray-500 truncate">{perm.description}</div>
                                                        </div>
                                                        <label className="relative inline-flex items-center cursor-pointer ml-4">
                                                            <input
                                                                type="checkbox"
                                                                checked={hasPermission}
                                                                onChange={(e) => handlePermissionChange(perm.key, e.target.checked)}
                                                                disabled={selectedRole.isSystem}
                                                                className="sr-only peer"
                                                            />
                                                            <div className={`w-11 h-6 rounded-full peer transition-colors ${
                                                                selectedRole.isSystem 
                                                                    ? 'bg-gray-300 cursor-not-allowed' 
                                                                    : 'bg-gray-200 peer-focus:ring-4 peer-focus:ring-primary/20 peer-checked:bg-primary'
                                                            } after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-full peer-checked:after:border-white`}></div>
                                                        </label>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </>
                    ) : (
                        <div className="flex flex-col items-center justify-center h-64 text-gray-500">
                            <Shield size={48} className="mb-4 opacity-50" />
                            <p>Sélectionnez un rôle pour voir ses permissions</p>
                        </div>
                    )}
                </Card>
            </div>

            {/* Legend */}
            <Card className="p-4">
                <div className="flex items-center gap-2 mb-3">
                    <Info size={18} className="text-blue-500" />
                    <h3 className="font-medium text-gray-900">Légende des rôles</h3>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
                    {roles.map(role => (
                        <div key={role.name} className="text-center">
                            <RoleBadge role={role.name} />
                            <p className="text-xs text-gray-500 mt-1">{role.description}</p>
                        </div>
                    ))}
                </div>
            </Card>
        </div>
    );
};

export default RolesPage;
