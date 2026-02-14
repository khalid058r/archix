import React, { useState, useMemo } from 'react';
import { 
    FolderTree, Plus, Search, Edit2, Trash2, 
    MoreVertical, ChevronRight, ChevronDown, Lock, Unlock,
    FileText, Users, FolderOpen, Shield
} from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { Modal } from '../../components/ui/Modal/Modal';
import toast from 'react-hot-toast';
import { useAppSelector } from '../../store/hooks';

interface Namespace {
    id: string;
    name: string;
    slug: string;
    description: string;
    parentId: string | null;
    isPublic: boolean;
    documentsCount: number;
    usersCount: number;
    createdAt: string;
    updatedAt: string;
    children?: Namespace[];
}

interface NamespacePermission {
    roleId: string;
    roleName: string;
    canView: boolean;
    canCreate: boolean;
    canEdit: boolean;
    canDelete: boolean;
    canManage: boolean;
}

// Mock data
const mockNamespaces: Namespace[] = [
    {
        id: '1',
        name: 'Ressources Humaines',
        slug: 'ressources-humaines',
        description: 'Documents RH et contrats',
        parentId: null,
        isPublic: false,
        documentsCount: 145,
        usersCount: 12,
        createdAt: '2024-01-10',
        updatedAt: '2024-01-20',
        children: [
            {
                id: '1-1',
                name: 'Contrats',
                slug: 'contrats',
                description: 'Contrats de travail',
                parentId: '1',
                isPublic: false,
                documentsCount: 89,
                usersCount: 5,
                createdAt: '2024-01-11',
                updatedAt: '2024-01-18',
            },
            {
                id: '1-2',
                name: 'Fiches de paie',
                slug: 'fiches-de-paie',
                description: 'Bulletins de salaire',
                parentId: '1',
                isPublic: false,
                documentsCount: 56,
                usersCount: 3,
                createdAt: '2024-01-12',
                updatedAt: '2024-01-19',
            },
        ],
    },
    {
        id: '2',
        name: 'Comptabilité',
        slug: 'comptabilite',
        description: 'Documents comptables et financiers',
        parentId: null,
        isPublic: false,
        documentsCount: 234,
        usersCount: 8,
        createdAt: '2024-01-08',
        updatedAt: '2024-01-21',
        children: [
            {
                id: '2-1',
                name: 'Factures',
                slug: 'factures',
                description: 'Factures clients et fournisseurs',
                parentId: '2',
                isPublic: false,
                documentsCount: 178,
                usersCount: 6,
                createdAt: '2024-01-09',
                updatedAt: '2024-01-20',
            },
        ],
    },
    {
        id: '3',
        name: 'Communication',
        slug: 'communication',
        description: 'Documents de communication interne et externe',
        parentId: null,
        isPublic: true,
        documentsCount: 67,
        usersCount: 25,
        createdAt: '2024-01-05',
        updatedAt: '2024-01-22',
    },
    {
        id: '4',
        name: 'Juridique',
        slug: 'juridique',
        description: 'Documents légaux et contrats',
        parentId: null,
        isPublic: false,
        documentsCount: 89,
        usersCount: 4,
        createdAt: '2024-01-03',
        updatedAt: '2024-01-17',
    },
];

const mockPermissions: NamespacePermission[] = [
    { roleId: '1', roleName: 'Admin', canView: true, canCreate: true, canEdit: true, canDelete: true, canManage: true },
    { roleId: '2', roleName: 'Manager', canView: true, canCreate: true, canEdit: true, canDelete: false, canManage: false },
    { roleId: '3', roleName: 'Éditeur', canView: true, canCreate: true, canEdit: true, canDelete: false, canManage: false },
    { roleId: '4', roleName: 'Lecteur', canView: true, canCreate: false, canEdit: false, canDelete: false, canManage: false },
];

interface NamespaceItemProps {
    namespace: Namespace;
    level: number;
    onEdit: (ns: Namespace) => void;
    onDelete: (ns: Namespace) => void;
    onPermissions: (ns: Namespace) => void;
    expandedIds: Set<string>;
    toggleExpand: (id: string) => void;
}

const NamespaceItem: React.FC<NamespaceItemProps> = ({ 
    namespace, level, onEdit, onDelete, onPermissions, expandedIds, toggleExpand 
}) => {
    const hasChildren = namespace.children && namespace.children.length > 0;
    const isExpanded = expandedIds.has(namespace.id);
    const [showMenu, setShowMenu] = useState(false);

    return (
        <>
            <div 
                className={`flex items-center gap-3 p-3 hover:bg-gray-50 border-b transition-colors ${
                    level > 0 ? 'bg-gray-50/50' : ''
                }`}
                style={{ paddingLeft: `${16 + level * 24}px` }}
            >
                {/* Expand/Collapse */}
                <button 
                    onClick={() => hasChildren && toggleExpand(namespace.id)}
                    className={`p-1 rounded ${hasChildren ? 'hover:bg-gray-200 cursor-pointer' : 'opacity-0'}`}
                >
                    {hasChildren && (
                        isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />
                    )}
                </button>

                {/* Icon */}
                <div className={`p-2 rounded-lg ${namespace.isPublic ? 'bg-green-100 text-green-600' : 'bg-indigo-100 text-indigo-600'}`}>
                    <FolderOpen size={18} />
                </div>

                {/* Name & Description */}
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                        <h3 className="font-medium text-gray-900 truncate">{namespace.name}</h3>
                        {namespace.isPublic ? (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-green-100 text-green-700 text-xs rounded-full">
                                <Unlock size={10} />
                                Public
                            </span>
                        ) : (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded-full">
                                <Lock size={10} />
                                Privé
                            </span>
                        )}
                    </div>
                    <p className="text-sm text-gray-500 truncate">{namespace.description}</p>
                </div>

                {/* Stats */}
                <div className="hidden md:flex items-center gap-6 text-sm text-gray-500">
                    <div className="flex items-center gap-1.5 min-w-[80px]">
                        <FileText size={14} className="text-gray-400" />
                        {namespace.documentsCount} docs
                    </div>
                    <div className="flex items-center gap-1.5 min-w-[80px]">
                        <Users size={14} className="text-gray-400" />
                        {namespace.usersCount} users
                    </div>
                </div>

                {/* Actions */}
                <div className="relative">
                    <button 
                        onClick={() => setShowMenu(!showMenu)}
                        className="p-2 hover:bg-gray-200 rounded-lg transition-colors"
                    >
                        <MoreVertical size={16} className="text-gray-500" />
                    </button>
                    {showMenu && (
                        <>
                            <div className="fixed inset-0 z-10" onClick={() => setShowMenu(false)} />
                            <div className="absolute right-0 top-full mt-1 w-48 bg-white rounded-lg shadow-lg border z-20">
                                <button 
                                    className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                                    onClick={() => { onEdit(namespace); setShowMenu(false); }}
                                >
                                    <Edit2 size={14} />
                                    Modifier
                                </button>
                                <button 
                                    className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                                    onClick={() => { onPermissions(namespace); setShowMenu(false); }}
                                >
                                    <Shield size={14} />
                                    Permissions
                                </button>
                                <button 
                                    className="w-full px-4 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2 text-red-600"
                                    onClick={() => { onDelete(namespace); setShowMenu(false); }}
                                >
                                    <Trash2 size={14} />
                                    Supprimer
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>

            {/* Children */}
            {hasChildren && isExpanded && namespace.children?.map(child => (
                <NamespaceItem
                    key={child.id}
                    namespace={child}
                    level={level + 1}
                    onEdit={onEdit}
                    onDelete={onDelete}
                    onPermissions={onPermissions}
                    expandedIds={expandedIds}
                    toggleExpand={toggleExpand}
                />
            ))}
        </>
    );
};

export const NamespacesAdminPage = () => {
    useAppSelector((state) => state.auth.currentOrganization); // For future use
    const [searchQuery, setSearchQuery] = useState('');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showPermissionsModal, setShowPermissionsModal] = useState(false);
    const [selectedNamespace, setSelectedNamespace] = useState<Namespace | null>(null);
    const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set(['1', '2']));
    useState<'tree' | 'list'>('tree'); // viewMode for future use

    // Form state
    const [formData, setFormData] = useState({
        name: '',
        slug: '',
        description: '',
        parentId: '',
        isPublic: false,
    });

    // Permissions state
    const [permissions, setPermissions] = useState<NamespacePermission[]>(mockPermissions);

    const toggleExpand = (id: string) => {
        setExpandedIds(prev => {
            const newSet = new Set(prev);
            if (newSet.has(id)) {
                newSet.delete(id);
            } else {
                newSet.add(id);
            }
            return newSet;
        });
    };

    const expandAll = () => {
        const getAllIds = (namespaces: Namespace[]): string[] => {
            return namespaces.flatMap(ns => [ns.id, ...(ns.children ? getAllIds(ns.children) : [])]);
        };
        setExpandedIds(new Set(getAllIds(mockNamespaces)));
    };

    const collapseAll = () => {
        setExpandedIds(new Set());
    };

    const handleEdit = (namespace: Namespace) => {
        setSelectedNamespace(namespace);
        setFormData({
            name: namespace.name,
            slug: namespace.slug,
            description: namespace.description,
            parentId: namespace.parentId || '',
            isPublic: namespace.isPublic,
        });
        setShowCreateModal(true);
    };

    const handleDelete = (namespace: Namespace) => {
        if (confirm(`Êtes-vous sûr de vouloir supprimer "${namespace.name}" ?`)) {
            toast.success(`Namespace "${namespace.name}" supprimé`);
        }
    };

    const handlePermissions = (namespace: Namespace) => {
        setSelectedNamespace(namespace);
        setShowPermissionsModal(true);
    };

    const handleSave = () => {
        if (selectedNamespace) {
            toast.success('Namespace mis à jour');
        } else {
            toast.success('Namespace créé');
        }
        setShowCreateModal(false);
        setSelectedNamespace(null);
        setFormData({ name: '', slug: '', description: '', parentId: '', isPublic: false });
    };

    const handlePermissionChange = (roleId: string, permission: string, value: boolean) => {
        setPermissions(prev => prev.map(p => 
            p.roleId === roleId ? { ...p, [permission]: value } : p
        ));
    };

    const savePermissions = () => {
        toast.success('Permissions mises à jour');
        setShowPermissionsModal(false);
    };

    // Generate slug from name
    const generateSlug = (name: string) => {
        return name
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-|-$/g, '');
    };

    // Stats
    const stats = useMemo(() => {
        const flatten = (namespaces: Namespace[]): Namespace[] => {
            return namespaces.flatMap(ns => [ns, ...(ns.children ? flatten(ns.children) : [])]);
        };
        const allNamespaces = flatten(mockNamespaces);
        return {
            total: allNamespaces.length,
            public: allNamespaces.filter(ns => ns.isPublic).length,
            private: allNamespaces.filter(ns => !ns.isPublic).length,
            totalDocs: allNamespaces.reduce((acc, ns) => acc + ns.documentsCount, 0),
        };
    }, []);

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <FolderTree className="text-primary" />
                        Gestion des namespaces
                    </h1>
                    <p className="text-gray-500">Organisez et gérez les espaces de stockage</p>
                </div>
                <Button variant="primary" onClick={() => setShowCreateModal(true)}>
                    <Plus size={16} className="mr-2" />
                    Nouveau namespace
                </Button>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <Card className="p-4">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-indigo-100 rounded-lg text-indigo-600">
                            <FolderTree size={20} />
                        </div>
                        <div>
                            <p className="text-2xl font-bold text-gray-900">{stats.total}</p>
                            <p className="text-sm text-gray-500">Total</p>
                        </div>
                    </div>
                </Card>
                <Card className="p-4">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-green-100 rounded-lg text-green-600">
                            <Unlock size={20} />
                        </div>
                        <div>
                            <p className="text-2xl font-bold text-gray-900">{stats.public}</p>
                            <p className="text-sm text-gray-500">Publics</p>
                        </div>
                    </div>
                </Card>
                <Card className="p-4">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-gray-100 rounded-lg text-gray-600">
                            <Lock size={20} />
                        </div>
                        <div>
                            <p className="text-2xl font-bold text-gray-900">{stats.private}</p>
                            <p className="text-sm text-gray-500">Privés</p>
                        </div>
                    </div>
                </Card>
                <Card className="p-4">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
                            <FileText size={20} />
                        </div>
                        <div>
                            <p className="text-2xl font-bold text-gray-900">{stats.totalDocs}</p>
                            <p className="text-sm text-gray-500">Documents</p>
                        </div>
                    </div>
                </Card>
            </div>

            {/* Toolbar */}
            <Card className="p-4">
                <div className="flex flex-col sm:flex-row gap-4 justify-between">
                    <div className="flex items-center gap-3">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                            <Input
                                placeholder="Rechercher un namespace..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                className="pl-10 w-64"
                            />
                        </div>
                    </div>
                    <div className="flex items-center gap-2">
                        <Button variant="ghost" size="sm" onClick={expandAll}>
                            Tout déplier
                        </Button>
                        <Button variant="ghost" size="sm" onClick={collapseAll}>
                            Tout replier
                        </Button>
                    </div>
                </div>
            </Card>

            {/* Namespace Tree */}
            <Card className="overflow-hidden">
                <div className="bg-gray-50 px-4 py-3 border-b flex items-center justify-between">
                    <div className="flex items-center gap-2 text-sm font-medium text-gray-600">
                        <FolderTree size={16} />
                        Hiérarchie des namespaces
                    </div>
                    <span className="text-xs text-gray-500">{stats.total} namespaces</span>
                </div>
                <div className="divide-y">
                    {mockNamespaces.map(namespace => (
                        <NamespaceItem
                            key={namespace.id}
                            namespace={namespace}
                            level={0}
                            onEdit={handleEdit}
                            onDelete={handleDelete}
                            onPermissions={handlePermissions}
                            expandedIds={expandedIds}
                            toggleExpand={toggleExpand}
                        />
                    ))}
                </div>
            </Card>

            {/* Create/Edit Modal */}
            <Modal
                isOpen={showCreateModal}
                onClose={() => {
                    setShowCreateModal(false);
                    setSelectedNamespace(null);
                    setFormData({ name: '', slug: '', description: '', parentId: '', isPublic: false });
                }}
                title={selectedNamespace ? 'Modifier le namespace' : 'Nouveau namespace'}
            >
                <div className="space-y-4">
                    <Input
                        label="Nom"
                        value={formData.name}
                        onChange={(e) => {
                            setFormData({ 
                                ...formData, 
                                name: e.target.value,
                                slug: generateSlug(e.target.value)
                            });
                        }}
                        placeholder="Nom du namespace"
                        required
                    />
                    <Input
                        label="Slug"
                        value={formData.slug}
                        onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
                        placeholder="nom-du-namespace"
                        className="font-mono"
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                        <textarea
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50 h-20 resize-none"
                            placeholder="Description du namespace..."
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Namespace parent</label>
                        <select
                            value={formData.parentId}
                            onChange={(e) => setFormData({ ...formData, parentId: e.target.value })}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                        >
                            <option value="">Aucun (racine)</option>
                            {mockNamespaces.map(ns => (
                                <option key={ns.id} value={ns.id}>{ns.name}</option>
                            ))}
                        </select>
                    </div>
                    <label className="flex items-center gap-3 cursor-pointer p-3 bg-gray-50 rounded-lg">
                        <input
                            type="checkbox"
                            checked={formData.isPublic}
                            onChange={(e) => setFormData({ ...formData, isPublic: e.target.checked })}
                            className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                        />
                        <div>
                            <div className="font-medium text-gray-900">Namespace public</div>
                            <div className="text-sm text-gray-500">Visible par tous les utilisateurs</div>
                        </div>
                    </label>
                    <div className="flex justify-end gap-3 pt-4">
                        <Button variant="outline" onClick={() => setShowCreateModal(false)}>
                            Annuler
                        </Button>
                        <Button variant="primary" onClick={handleSave}>
                            {selectedNamespace ? 'Mettre à jour' : 'Créer'}
                        </Button>
                    </div>
                </div>
            </Modal>

            {/* Permissions Modal */}
            <Modal
                isOpen={showPermissionsModal}
                onClose={() => setShowPermissionsModal(false)}
                title={`Permissions - ${selectedNamespace?.name}`}
            >
                <div className="space-y-4">
                    <p className="text-sm text-gray-500">
                        Configurez les permissions d'accès pour ce namespace.
                    </p>
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="border-b">
                                    <th className="text-left py-2 pr-4 font-medium text-gray-600">Rôle</th>
                                    <th className="px-2 py-2 text-center font-medium text-gray-600">Voir</th>
                                    <th className="px-2 py-2 text-center font-medium text-gray-600">Créer</th>
                                    <th className="px-2 py-2 text-center font-medium text-gray-600">Éditer</th>
                                    <th className="px-2 py-2 text-center font-medium text-gray-600">Suppr.</th>
                                    <th className="px-2 py-2 text-center font-medium text-gray-600">Gérer</th>
                                </tr>
                            </thead>
                            <tbody>
                                {permissions.map(perm => (
                                    <tr key={perm.roleId} className="border-b last:border-0">
                                        <td className="py-3 pr-4 font-medium">{perm.roleName}</td>
                                        {['canView', 'canCreate', 'canEdit', 'canDelete', 'canManage'].map(key => (
                                            <td key={key} className="px-2 py-3 text-center">
                                                <input
                                                    type="checkbox"
                                                    checked={perm[key as keyof NamespacePermission] as boolean}
                                                    onChange={(e) => handlePermissionChange(perm.roleId, key, e.target.checked)}
                                                    className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                                                />
                                            </td>
                                        ))}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <div className="flex justify-end gap-3 pt-4">
                        <Button variant="outline" onClick={() => setShowPermissionsModal(false)}>
                            Annuler
                        </Button>
                        <Button variant="primary" onClick={savePermissions}>
                            Sauvegarder
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default NamespacesAdminPage;
