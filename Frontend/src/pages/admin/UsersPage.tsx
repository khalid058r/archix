import React, { useEffect, useState } from 'react';
import { Plus, Search, User as UserIcon, Trash2, Mail, Phone, Building } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { Modal } from '../../components/ui/Modal/Modal';
import { FileUploader } from '../../components/ui/FileUploader'; // Reusing for consistency, though maybe not for user avatar yet
import { userApi } from '../../api/endpoints/userApi';
import type { User } from '../../types/user.types';
import toast from 'react-hot-toast';

export const UsersPage = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    // New User State
    const [newUser, setNewUser] = useState({
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        departmentId: ''
    });

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            setLoading(true);
            const data = await userApi.getAll();
            setUsers(data);
        } catch (error) {
            console.error('Failed to load users', error);
            toast.error('Erreur lors du chargement des utilisateurs');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) return;
        try {
            await userApi.delete(id);
            toast.success('Utilisateur supprimé');
            loadUsers();
        } catch (error) {
            toast.error('Erreur lors de la suppression');
        }
    };

    const handleCreate = async () => {
        try {
            await userApi.create({
                ...newUser,
                departmentId: newUser.departmentId ? parseInt(newUser.departmentId) : undefined
            });
            toast.success('Utilisateur créé avec succès');
            setIsCreateModalOpen(false);
            setNewUser({ email: '', password: '', firstName: '', lastName: '', departmentId: '' });
            loadUsers();
        } catch (error) {
            console.error(error);
            toast.error('Erreur lors de la création');
        }
    };

    const filteredUsers = users.filter(user =>
        user.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Utilisateurs</h1>
                    <p className="text-gray-500 dark:text-gray-400">Gérez les comptes utilisateurs de votre organisation</p>
                </div>
                <Button variant="primary" onClick={() => setIsCreateModalOpen(true)}>
                    <Plus className="w-4 h-4 mr-2" />
                    Nouvel Utilisateur
                </Button>
            </div>

            {/* Search */}
            <div className="max-w-md">
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <Input
                        placeholder="Rechercher un utilisateur (nom, email)..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="pl-10"
                    />
                </div>
            </div>

            {/* List */}
            {loading ? (
                <div>Chargement...</div>
            ) : (
                <div className="grid gap-4">
                    {filteredUsers.map((user) => (
                        <Card key={user.id} className="p-4 flex items-center justify-between hover:shadow-md transition-shadow">
                            <div className="flex items-center gap-4">
                                <div className="p-3 bg-blue-100 dark:bg-blue-900/30 rounded-full text-blue-600 dark:text-blue-400">
                                    <UserIcon size={24} />
                                </div>
                                <div>
                                    <h3 className="font-semibold text-lg flex items-center gap-2">
                                        {user.fullName}
                                        {user.isActive ? (
                                            <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded-full">Actif</span>
                                        ) : (
                                            <span className="text-xs px-2 py-0.5 bg-red-100 text-red-700 rounded-full">Inactif</span>
                                        )}
                                    </h3>
                                    <div className="flex gap-4 mt-1 text-sm text-gray-500">
                                        <div className="flex items-center gap-1">
                                            <Mail className="w-3 h-3" />
                                            {user.email}
                                        </div>
                                        {user.phone && (
                                            <div className="flex items-center gap-1">
                                                <Phone className="w-3 h-3" />
                                                {user.phone}
                                            </div>
                                        )}
                                        {user.department && (
                                            <div className="flex items-center gap-1">
                                                <Building className="w-3 h-3" />
                                                {user.department.name}
                                            </div>
                                        )}
                                    </div>
                                    {/* Roles badges */}
                                    <div className="flex gap-2 mt-2">
                                        {user.roles && user.roles.map(role => (
                                            <span key={role.id} className="text-xs border px-1.5 py-0.5 rounded text-gray-500">
                                                {role.name}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                <Button variant="ghost" size="sm" className="text-red-500 hover:bg-red-50" onClick={() => handleDelete(user.id)}>
                                    <Trash2 className="w-4 h-4" />
                                </Button>
                            </div>
                        </Card>
                    ))}
                    {filteredUsers.length === 0 && (
                        <div className="text-center py-10 text-gray-500">
                            Aucun utilisateur trouvé.
                        </div>
                    )}
                </div>
            )}

            {/* Create Modal */}
            <Modal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                title="Ajouter un utilisateur"
                footer={
                    <>
                        <Button variant="ghost" onClick={() => setIsCreateModalOpen(false)}>Annuler</Button>
                        <Button variant="primary" onClick={handleCreate}>Créer</Button>
                    </>
                }
            >
                <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <Input
                            label="Prénom"
                            placeholder="John"
                            value={newUser.firstName}
                            onChange={(e) => setNewUser({ ...newUser, firstName: e.target.value })}
                        />
                        <Input
                            label="Nom"
                            placeholder="Doe"
                            value={newUser.lastName}
                            onChange={(e) => setNewUser({ ...newUser, lastName: e.target.value })}
                        />
                    </div>
                    <Input
                        label="Email"
                        type="email"
                        placeholder="john.doe@archix.com"
                        value={newUser.email}
                        onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                    />
                    <Input
                        label="Mot de passe"
                        type="password"
                        placeholder="••••••••"
                        value={newUser.password}
                        onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                    />
                    <Input
                        label="ID Département (Optionnel)"
                        type="number"
                        placeholder="ex: 1"
                        value={newUser.departmentId}
                        onChange={(e) => setNewUser({ ...newUser, departmentId: e.target.value })}
                    />
                </div>
            </Modal>
        </div>
    );
};
export default UsersPage;
