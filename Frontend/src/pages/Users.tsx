import { useState, useEffect } from 'react';
import { UserPlus, Search, Edit2, Trash2, Shield, Building2 } from 'lucide-react';
import {
    Button,
    Input,
    Card,
    Badge,
    Spinner,
    useToast
} from '../components/ui';
import { userService } from '../services';
import type { UserDto } from '../types';
import './Users.css';

export function Users() {
    const { toast } = useToast();

    const [users, setUsers] = useState<UserDto[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        loadData();
    }, [currentPage]);

    const loadData = async () => {
        try {
            setIsLoading(true);
            const usersRes = await userService.getAll(currentPage, 10);
            setUsers(usersRes.content);
            setTotalPages(usersRes.totalPages);
        } catch (error) {
            console.error('Error loading users:', error);
            toast.error('Erreur lors du chargement des utilisateurs');
        } finally {
            setIsLoading(false);
        }
    };

    const handleToggleActive = async (user: UserDto) => {
        try {
            if (user.isActive) {
                await userService.deactivate(user.id);
                toast.success(`${user.firstName} ${user.lastName} désactivé`);
            } else {
                await userService.activate(user.id);
                toast.success(`${user.firstName} ${user.lastName} activé`);
            }
            loadData();
        } catch (error) {
            toast.error('Erreur lors de la mise à jour');
        }
    };

    const handleDelete = async (user: UserDto) => {
        if (window.confirm(`Supprimer ${user.firstName} ${user.lastName} ?`)) {
            try {
                await userService.delete(user.id);
                toast.success('Utilisateur supprimé');
                loadData();
            } catch (error) {
                toast.error('Erreur lors de la suppression');
            }
        }
    };

    const filteredUsers = users.filter(user =>
        `${user.firstName} ${user.lastName} ${user.email}`.toLowerCase().includes(searchQuery.toLowerCase())
    );

    if (isLoading) {
        return (
            <div className="page-loading">
                <Spinner size="lg" />
                <span>Chargement...</span>
            </div>
        );
    }

    return (
        <div className="users-page">
            {/* Header */}
            <div className="page-header">
                <div className="page-header-content">
                    <h1 className="page-title">Utilisateurs</h1>
                    <p className="page-description">Gérez les utilisateurs et leurs permissions</p>
                </div>
                <div className="page-actions">
                    <Button variant="primary" leftIcon={<UserPlus size={18} />}>
                        Nouvel utilisateur
                    </Button>
                </div>
            </div>

            {/* Search */}
            <div className="section-header mb-6">
                <Input
                    placeholder="Rechercher un utilisateur..."
                    leftIcon={<Search size={18} />}
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="max-w-md"
                />
            </div>

            {/* Users Table */}
            <Card padding="none">
                <div className="users-table">
                    <table>
                        <thead>
                            <tr>
                                <th>Utilisateur</th>
                                <th>Département</th>
                                <th>Statut</th>
                                <th>Créé le</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredUsers.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="text-center py-8 text-secondary">
                                        Aucun utilisateur trouvé
                                    </td>
                                </tr>
                            ) : (
                                filteredUsers.map((user) => (
                                    <tr key={user.id}>
                                        <td>
                                            <div className="user-cell">
                                                <div className="user-avatar">
                                                    {user.firstName?.[0]}{user.lastName?.[0]}
                                                </div>
                                                <div className="user-info">
                                                    <div className="user-name">{user.firstName} {user.lastName}</div>
                                                    <div className="user-email">{user.email}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <div className="flex items-center gap-2 text-secondary">
                                                <Building2 size={14} />
                                                {user.departmentName || user.department?.name || 'Non assigné'}
                                            </div>
                                        </td>
                                        <td>
                                            <Badge variant={user.isActive ? 'success' : 'default'}>
                                                {user.isActive ? 'Actif' : 'Inactif'}
                                            </Badge>
                                        </td>
                                        <td className="text-secondary text-sm">
                                            {new Date(user.createdAt).toLocaleDateString()}
                                        </td>
                                        <td>
                                            <div className="flex gap-1 justify-end">
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleToggleActive(user)}
                                                    title={user.isActive ? 'Désactiver' : 'Activer'}
                                                >
                                                    <Shield size={16} />
                                                </Button>
                                                <Button variant="ghost" size="sm">
                                                    <Edit2 size={16} />
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    onClick={() => handleDelete(user)}
                                                >
                                                    <Trash2 size={16} />
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </Card>

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="flex justify-center mt-6 gap-2">
                    <Button
                        variant="outline"
                        disabled={currentPage === 0}
                        onClick={() => setCurrentPage(p => p - 1)}
                    >
                        Précédent
                    </Button>
                    <span className="flex items-center px-4 text-secondary">
                        Page {currentPage + 1} sur {totalPages}
                    </span>
                    <Button
                        variant="outline"
                        disabled={currentPage >= totalPages - 1}
                        onClick={() => setCurrentPage(p => p + 1)}
                    >
                        Suivant
                    </Button>
                </div>
            )}
        </div>
    );
}

export default Users;
