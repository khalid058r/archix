import { useEffect, useState } from 'react';
import {
    Plus, Trash2, Edit,
    Download, Filter, Shield, Key,
    UserX, UserCheck
} from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { Modal } from '../../components/ui/Modal/Modal';
import { DataTable, RoleBadge, StatusBadge } from '../../components/admin';
import type { Column } from '../../components/admin/DataTable';
import { userApi } from '../../api/endpoints/userApi';
import { departmentApi } from '../../api/endpoints/departmentApi';
import type { User } from '../../types/user.types';
import type { Department } from '../../types';
import toast from 'react-hot-toast';

interface UserFilters {
    status: 'all' | 'active' | 'inactive' | 'locked';
    role: string;
    department: string;
}

export const UsersPage = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [departments, setDepartments] = useState<Department[]>([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState<UserFilters>({
        status: 'all',
        role: 'all',
        department: 'all'
    });
    useState(false); // showFilters - for future use
    const [selectedUsers, setSelectedUsers] = useState<User[]>([]);

    // Modals
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [, setIsRolesModalOpen] = useState(false); // isRolesModalOpen - for future modal
    const [selectedUser, setSelectedUser] = useState<User | null>(null);

    // New User State
    const [newUser, setNewUser] = useState({
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        phone: '',
        departmentId: '',
        roles: [] as string[]
    });

    // Form validation errors
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});

    const availableRoles = [
        { value: 'SUPER_ADMIN', label: 'Super Admin' },
        { value: 'ADMIN', label: 'Admin' },
        { value: 'MANAGER', label: 'Manager' },
        { value: 'USER', label: 'Utilisateur' },
        { value: 'READER', label: 'Lecteur' },
        { value: 'GUEST', label: 'Invité' },
    ];

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            const [usersData, deptsData] = await Promise.all([
                userApi.getAll(),
                departmentApi.getAll()
            ]);
            setUsers(usersData);
            setDepartments(deptsData);
        } catch (error) {
            console.error('Failed to load data', error);
            toast.error('Erreur lors du chargement des données');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) return;
        try {
            await userApi.delete(id);
            toast.success('Utilisateur supprimé');
            loadData();
        } catch (error) {
            toast.error('Erreur lors de la suppression');
        }
    };

    const handleBulkDelete = async () => {
        if (selectedUsers.length === 0) return;
        if (!window.confirm(`Supprimer ${selectedUsers.length} utilisateur(s) ?`)) return;

        try {
            await Promise.all(selectedUsers.map(u => userApi.delete(u.id)));
            toast.success(`${selectedUsers.length} utilisateur(s) supprimé(s)`);
            setSelectedUsers([]);
            loadData();
        } catch (error) {
            toast.error('Erreur lors de la suppression');
        }
    };

    const handleToggleStatus = async (user: User) => {
        try {
            await userApi.update(user.id, { isActive: !user.isActive });
            toast.success(user.isActive ? 'Utilisateur désactivé' : 'Utilisateur activé');
            loadData();
        } catch (error) {
            toast.error('Erreur lors de la mise à jour');
        }
    };

    // Validate form before submission
    const validateForm = (): boolean => {
        const errors: Record<string, string> = {};

        // First name validation
        if (!newUser.firstName.trim()) {
            errors.firstName = 'Le prénom est requis';
        } else if (newUser.firstName.length < 2 || newUser.firstName.length > 50) {
            errors.firstName = 'Le prénom doit contenir entre 2 et 50 caractères';
        }

        // Last name validation
        if (!newUser.lastName.trim()) {
            errors.lastName = 'Le nom est requis';
        } else if (newUser.lastName.length < 2 || newUser.lastName.length > 50) {
            errors.lastName = 'Le nom doit contenir entre 2 et 50 caractères';
        }

        // Email validation
        if (!newUser.email.trim()) {
            errors.email = 'L\'email est requis';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newUser.email)) {
            errors.email = 'Format d\'email invalide';
        }

        // Password validation
        if (!newUser.password) {
            errors.password = 'Le mot de passe est requis';
        } else if (newUser.password.length < 8) {
            errors.password = 'Le mot de passe doit contenir au moins 8 caractères';
        } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(newUser.password)) {
            errors.password = 'Le mot de passe doit contenir au moins 1 majuscule, 1 minuscule et 1 chiffre';
        }

        // Phone validation (optional but must be valid if provided)
        if (newUser.phone && !/^\+?[0-9]{10,15}$/.test(newUser.phone.replace(/\s/g, ''))) {
            errors.phone = 'Numéro de téléphone invalide (10-15 chiffres)';
        }

        // Department validation
        if (!newUser.departmentId) {
            errors.departmentId = 'Veuillez sélectionner un département';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleCreate = async () => {
        // Validate form first
        if (!validateForm()) {
            toast.error('Veuillez corriger les erreurs du formulaire');
            return;
        }

        try {
            await userApi.create({
                email: newUser.email,
                password: newUser.password,
                firstName: newUser.firstName,
                lastName: newUser.lastName,
                phone: newUser.phone ? newUser.phone.replace(/\s/g, '') : undefined, // Remove spaces
                departmentId: parseInt(newUser.departmentId),
                roleTypes: newUser.roles.length > 0 ? newUser.roles : undefined // Send roles to backend
            });
            toast.success('Utilisateur créé avec succès');
            setIsCreateModalOpen(false);
            setNewUser({ email: '', password: '', firstName: '', lastName: '', phone: '', departmentId: '', roles: [] });
            setFormErrors({});
            loadData();
        } catch (error: any) {
            console.error(error);
            // Extract backend validation message if available
            const errorData = error.response?.data;
            if (errorData?.errors) {
                // Handle field-specific errors from backend
                const backendErrors: Record<string, string> = {};
                for (const [field, message] of Object.entries(errorData.errors)) {
                    backendErrors[field] = message as string;
                }
                setFormErrors(backendErrors);
            }
            const message = errorData?.message || errorData?.error || 'Erreur lors de la création';
            toast.error(message);
        }
    };

    const handleEdit = async () => {
        if (!selectedUser) return;
        try {
            await userApi.update(selectedUser.id, {
                firstName: selectedUser.firstName,
                lastName: selectedUser.lastName,
                email: selectedUser.email,
                phone: selectedUser.phone,
                isActive: selectedUser.isActive
            });
            toast.success('Utilisateur mis à jour');
            setIsEditModalOpen(false);
            setSelectedUser(null);
            loadData();
        } catch (error) {
            console.error(error);
            toast.error('Erreur lors de la mise à jour');
        }
    };

    const handleExport = () => {
        const csv = [
            ['ID', 'Prénom', 'Nom', 'Email', 'Téléphone', 'Département', 'Rôles', 'Statut', 'Créé le'].join(','),
            ...users.map(u => [
                u.id,
                u.firstName,
                u.lastName,
                u.email,
                u.phone || '',
                u.department?.name || '',
                u.roles?.map(r => r.name).join(';') || '',
                u.isActive ? 'Actif' : 'Inactif',
                new Date(u.createdAt).toLocaleDateString()
            ].join(','))
        ].join('\n');

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `users_export_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
        toast.success('Export téléchargé');
    };

    // Filter users
    const filteredUsers = users.filter(user => {
        if (filters.status !== 'all') {
            if (filters.status === 'active' && !user.isActive) return false;
            if (filters.status === 'inactive' && user.isActive) return false;
            // Add locked logic if needed
        }
        if (filters.role !== 'all' && !user.roles?.some(r => r.name === filters.role)) {
            return false;
        }
        if (filters.department !== 'all' && user.department?.id !== parseInt(filters.department)) {
            return false;
        }
        return true;
    });

    const columns: Column<User>[] = [
        {
            key: 'fullName',
            header: 'Utilisateur',
            sortable: true,
            render: (user) => (
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center text-white font-semibold text-sm">
                        {user.firstName?.[0]}{user.lastName?.[0]}
                    </div>
                    <div>
                        <div className="font-medium text-gray-900">
                            {user.firstName} {user.lastName}
                        </div>
                        <div className="text-sm text-gray-500">{user.email}</div>
                    </div>
                </div>
            )
        },
        {
            key: 'roles',
            header: 'Rôles',
            render: (user) => (
                <div className="flex flex-wrap gap-1">
                    {user.roles?.map(role => (
                        <RoleBadge key={role.id} role={role.name} />
                    ))}
                    {(!user.roles || user.roles.length === 0) && (
                        <span className="text-gray-400 text-sm">Aucun rôle</span>
                    )}
                </div>
            )
        },
        {
            key: 'department',
            header: 'Département',
            sortable: true,
            render: (user) => (
                <span className="text-sm text-gray-600">
                    {user.department?.name || <span className="text-gray-400">Non assigné</span>}
                </span>
            )
        },
        {
            key: 'isActive',
            header: 'Statut',
            sortable: true,
            render: (user) => (
                <StatusBadge
                    status={user.isActive ? 'Actif' : 'Inactif'}
                    variant={user.isActive ? 'success' : 'error'}
                    dot
                />
            )
        },
        {
            key: 'createdAt',
            header: 'Créé le',
            sortable: true,
            render: (user) => (
                <span className="text-sm text-gray-500">
                    {new Date(user.createdAt).toLocaleDateString('fr-FR')}
                </span>
            )
        },
        {
            key: 'actions',
            header: '',
            width: '120px',
            render: (user) => (
                <div className="flex items-center justify-end gap-1">
                    <Button
                        variant="ghost"
                        size="sm"
                        className="p-2"
                        onClick={(e) => {
                            e.stopPropagation();
                            handleToggleStatus(user);
                        }}
                        title={user.isActive ? 'Désactiver' : 'Activer'}
                    >
                        {user.isActive ? <UserX size={16} className="text-orange-500" /> : <UserCheck size={16} className="text-green-500" />}
                    </Button>
                    <Button
                        variant="ghost"
                        size="sm"
                        className="p-2"
                        onClick={(e) => {
                            e.stopPropagation();
                            setSelectedUser(user);
                            setIsEditModalOpen(true);
                        }}
                    >
                        <Edit size={16} className="text-blue-500" />
                    </Button>
                    <Button
                        variant="ghost"
                        size="sm"
                        className="p-2"
                        onClick={(e) => {
                            e.stopPropagation();
                            handleDelete(user.id);
                        }}
                    >
                        <Trash2 size={16} className="text-red-500" />
                    </Button>
                </div>
            )
        }
    ];

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Gestion des Utilisateurs</h1>
                    <p className="text-gray-500">
                        {filteredUsers.length} utilisateur{filteredUsers.length > 1 ? 's' : ''}
                        {filters.status !== 'all' || filters.role !== 'all' || filters.department !== 'all'
                            ? ' (filtré)'
                            : ''}
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="outline" size="sm" onClick={handleExport}>
                        <Download size={16} className="mr-2" />
                        Exporter
                    </Button>
                    <Button variant="primary" onClick={() => setIsCreateModalOpen(true)}>
                        <Plus size={16} className="mr-2" />
                        Nouvel Utilisateur
                    </Button>
                </div>
            </div>

            {/* Filters */}
            <Card className="p-4">
                <div className="flex flex-wrap items-center gap-4">
                    <div className="flex items-center gap-2">
                        <Filter size={18} className="text-gray-400" />
                        <span className="text-sm font-medium text-gray-700">Filtres:</span>
                    </div>

                    <select
                        value={filters.status}
                        onChange={(e) => setFilters({ ...filters, status: e.target.value as UserFilters['status'] })}
                        className="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                    >
                        <option value="all">Tous les statuts</option>
                        <option value="active">Actifs</option>
                        <option value="inactive">Inactifs</option>
                        <option value="locked">Verrouillés</option>
                    </select>

                    <select
                        value={filters.role}
                        onChange={(e) => setFilters({ ...filters, role: e.target.value })}
                        className="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                    >
                        <option value="all">Tous les rôles</option>
                        {availableRoles.map(role => (
                            <option key={role.value} value={role.value}>{role.label}</option>
                        ))}
                    </select>

                    <select
                        value={filters.department}
                        onChange={(e) => setFilters({ ...filters, department: e.target.value })}
                        className="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                    >
                        <option value="all">Tous les départements</option>
                        {departments.map(dept => (
                            <option key={dept.id} value={dept.id}>{dept.name}</option>
                        ))}
                    </select>

                    {(filters.status !== 'all' || filters.role !== 'all' || filters.department !== 'all') && (
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setFilters({ status: 'all', role: 'all', department: 'all' })}
                        >
                            Réinitialiser
                        </Button>
                    )}
                </div>

                {/* Bulk Actions */}
                {selectedUsers.length > 0 && (
                    <div className="mt-4 pt-4 border-t border-gray-200 flex items-center gap-4">
                        <span className="text-sm text-gray-600">
                            {selectedUsers.length} sélectionné{selectedUsers.length > 1 ? 's' : ''}
                        </span>
                        <Button variant="outline" size="sm" onClick={() => toast('Action: Activer')}>
                            <UserCheck size={14} className="mr-1" />
                            Activer
                        </Button>
                        <Button variant="outline" size="sm" onClick={() => toast('Action: Désactiver')}>
                            <UserX size={14} className="mr-1" />
                            Désactiver
                        </Button>
                        <Button variant="outline" size="sm" className="text-red-600" onClick={handleBulkDelete}>
                            <Trash2 size={14} className="mr-1" />
                            Supprimer
                        </Button>
                    </div>
                )}
            </Card>

            {/* Data Table */}
            <DataTable
                data={filteredUsers}
                columns={columns}
                loading={loading}
                searchable
                searchPlaceholder="Rechercher un utilisateur..."
                pageSize={15}
                emptyMessage="Aucun utilisateur trouvé"
                selectedRows={selectedUsers}
                onSelectionChange={setSelectedUsers}
                getRowId={(user) => user.id}
                onRowClick={(user) => {
                    setSelectedUser(user);
                    setIsEditModalOpen(true);
                }}
            />

            {/* Create Modal */}
            <Modal
                isOpen={isCreateModalOpen}
                onClose={() => { setIsCreateModalOpen(false); setFormErrors({}); }}
                title="Créer un nouvel utilisateur"
                footer={
                    <>
                        <Button variant="ghost" onClick={() => { setIsCreateModalOpen(false); setFormErrors({}); }}>Annuler</Button>
                        <Button variant="primary" onClick={handleCreate}>Créer</Button>
                    </>
                }
            >
                <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <Input
                                label="Prénom *"
                                placeholder="John"
                                value={newUser.firstName}
                                onChange={(e) => { setNewUser({ ...newUser, firstName: e.target.value }); setFormErrors(prev => ({ ...prev, firstName: '' })); }}
                            />
                            {formErrors.firstName && <p className="text-red-500 text-xs mt-1">{formErrors.firstName}</p>}
                        </div>
                        <div>
                            <Input
                                label="Nom *"
                                placeholder="Doe"
                                value={newUser.lastName}
                                onChange={(e) => { setNewUser({ ...newUser, lastName: e.target.value }); setFormErrors(prev => ({ ...prev, lastName: '' })); }}
                            />
                            {formErrors.lastName && <p className="text-red-500 text-xs mt-1">{formErrors.lastName}</p>}
                        </div>
                    </div>
                    <div>
                        <Input
                            label="Email *"
                            type="email"
                            placeholder="john.doe@archix.com"
                            value={newUser.email}
                            onChange={(e) => { setNewUser({ ...newUser, email: e.target.value }); setFormErrors(prev => ({ ...prev, email: '' })); }}
                        />
                        {formErrors.email && <p className="text-red-500 text-xs mt-1">{formErrors.email}</p>}
                    </div>
                    <div>
                        <Input
                            label="Téléphone"
                            type="tel"
                            placeholder="+33 6 00 00 00 00"
                            value={newUser.phone}
                            onChange={(e) => { setNewUser({ ...newUser, phone: e.target.value }); setFormErrors(prev => ({ ...prev, phone: '' })); }}
                        />
                        {formErrors.phone && <p className="text-red-500 text-xs mt-1">{formErrors.phone}</p>}
                    </div>
                    <div>
                        <Input
                            label="Mot de passe *"
                            type="password"
                            placeholder="••••••••"
                            value={newUser.password}
                            onChange={(e) => { setNewUser({ ...newUser, password: e.target.value }); setFormErrors(prev => ({ ...prev, password: '' })); }}
                        />
                        {formErrors.password && <p className="text-red-500 text-xs mt-1">{formErrors.password}</p>}
                        <p className="text-xs text-gray-500 mt-1">
                            Min. 8 caractères, 1 majuscule, 1 minuscule, 1 chiffre
                        </p>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Département *</label>
                        <select
                            value={newUser.departmentId}
                            onChange={(e) => { setNewUser({ ...newUser, departmentId: e.target.value }); setFormErrors(prev => ({ ...prev, departmentId: '' })); }}
                            className={`w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-primary/50 ${formErrors.departmentId ? 'border-red-500' : 'border-gray-300'}`}
                        >
                            <option value="">Sélectionner un département</option>
                            {departments.map(dept => (
                                <option key={dept.id} value={dept.id}>{dept.name}</option>
                            ))}
                        </select>
                        {formErrors.departmentId && <p className="text-red-500 text-xs mt-1">{formErrors.departmentId}</p>}
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Rôles</label>
                        <div className="flex flex-wrap gap-2">
                            {availableRoles.map(role => (
                                <label key={role.value} className="inline-flex items-center gap-2 cursor-pointer">
                                    <input
                                        type="checkbox"
                                        checked={newUser.roles.includes(role.value)}
                                        onChange={(e) => {
                                            if (e.target.checked) {
                                                setNewUser({ ...newUser, roles: [...newUser.roles, role.value] });
                                            } else {
                                                setNewUser({ ...newUser, roles: newUser.roles.filter(r => r !== role.value) });
                                            }
                                        }}
                                        className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary/50"
                                    />
                                    <span className="text-sm text-gray-700">{role.label}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                </div>
            </Modal>

            {/* Edit Modal */}
            <Modal
                isOpen={isEditModalOpen}
                onClose={() => { setIsEditModalOpen(false); setSelectedUser(null); }}
                title="Modifier l'utilisateur"
                footer={
                    <>
                        <Button variant="ghost" onClick={() => { setIsEditModalOpen(false); setSelectedUser(null); }}>Annuler</Button>
                        <Button variant="primary" onClick={handleEdit}>Sauvegarder</Button>
                    </>
                }
            >
                {selectedUser && (
                    <div className="space-y-4">
                        <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg">
                            <div className="w-16 h-16 rounded-full bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center text-white font-bold text-xl">
                                {selectedUser.firstName?.[0]}{selectedUser.lastName?.[0]}
                            </div>
                            <div>
                                <div className="font-semibold text-lg">{selectedUser.firstName} {selectedUser.lastName}</div>
                                <div className="text-sm text-gray-500">{selectedUser.email}</div>
                                <div className="flex gap-1 mt-1">
                                    {selectedUser.roles?.map(role => (
                                        <RoleBadge key={role.id} role={role.name} size="sm" />
                                    ))}
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <Input
                                label="Prénom"
                                value={selectedUser.firstName || ''}
                                onChange={(e) => setSelectedUser({ ...selectedUser, firstName: e.target.value })}
                            />
                            <Input
                                label="Nom"
                                value={selectedUser.lastName || ''}
                                onChange={(e) => setSelectedUser({ ...selectedUser, lastName: e.target.value })}
                            />
                        </div>
                        <Input
                            label="Email"
                            type="email"
                            value={selectedUser.email}
                            onChange={(e) => setSelectedUser({ ...selectedUser, email: e.target.value })}
                        />
                        <Input
                            label="Téléphone"
                            type="tel"
                            value={selectedUser.phone || ''}
                            onChange={(e) => setSelectedUser({ ...selectedUser, phone: e.target.value })}
                        />

                        <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                            <div>
                                <div className="font-medium text-gray-900">Compte actif</div>
                                <div className="text-sm text-gray-500">L'utilisateur peut se connecter</div>
                            </div>
                            <label className="relative inline-flex items-center cursor-pointer">
                                <input
                                    type="checkbox"
                                    checked={selectedUser.isActive}
                                    onChange={(e) => setSelectedUser({ ...selectedUser, isActive: e.target.checked })}
                                    className="sr-only peer"
                                />
                                <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary/20 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
                            </label>
                        </div>

                        <div className="border-t pt-4 mt-4">
                            <Button
                                variant="outline"
                                size="sm"
                                className="mr-2"
                                onClick={() => {
                                    setIsEditModalOpen(false);
                                    setIsRolesModalOpen(true);
                                }}
                            >
                                <Shield size={14} className="mr-1" />
                                Gérer les rôles
                            </Button>
                            <Button variant="outline" size="sm" onClick={() => toast('Reset password email sent')}>
                                <Key size={14} className="mr-1" />
                                Réinitialiser mot de passe
                            </Button>
                        </div>
                    </div>
                )}
            </Modal>
        </div>
    );
};

export default UsersPage;
