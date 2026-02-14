import { useEffect, useState } from 'react';
import { Plus, Search, Building, Trash2, Edit, ChevronRight, User } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { Modal } from '../../components/ui/Modal/Modal';
import { departmentApi } from '../../api/endpoints/departmentApi';
import { userApi } from '../../api/endpoints/userApi';
import type { Department } from '../../types';
import type { User as UserType } from '../../types/user.types';
import toast from 'react-hot-toast';
import { useAppSelector } from '../../store/hooks';

export const DepartmentsPage = () => {
    const [departments, setDepartments] = useState<Department[]>([]);
    const [users, setUsers] = useState<UserType[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [selectedDept, setSelectedDept] = useState<Department | null>(null);

    // Get current org ID to auto-assign
    const currentOrg = useAppSelector((state) => state.auth.currentOrganization);

    // New Dept State
    const [newDept, setNewDept] = useState({
        name: '',
        description: '',
        parentId: undefined as number | undefined,
        managerId: undefined as number | undefined,
        storageQuotaBytes: undefined as number | undefined
    });

    useEffect(() => {
        loadDepartments();
        loadUsers();
    }, []);

    const loadDepartments = async () => {
        try {
            setLoading(true);
            const data = await departmentApi.getAll();
            setDepartments(data);
        } catch (error) {
            console.error('Failed to load departments', error);
            toast.error('Erreur lors du chargement des départements');
        } finally {
            setLoading(false);
        }
    };

    const loadUsers = async () => {
        try {
            const data = await userApi.getAll();
            setUsers(data);
        } catch (error) {
            console.error('Failed to load users', error);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer ce département ?')) return;
        try {
            await departmentApi.delete(id);
            toast.success('Département supprimé');
            loadDepartments();
        } catch (error) {
            toast.error('Erreur lors de la suppression');
        }
    };

    const handleCreate = async () => {
        if (!currentOrg) {
            toast.error("Aucune organisation sélectionnée");
            return;
        }
        try {
            await departmentApi.create({
                ...newDept,
                organizationId: currentOrg.id
            });
            toast.success('Département créé avec succès');
            setIsCreateModalOpen(false);
            setNewDept({ name: '', description: '', parentId: undefined, managerId: undefined, storageQuotaBytes: undefined });
            loadDepartments();
        } catch (error) {
            console.error(error);
            toast.error('Erreur lors de la création');
        }
    };

    const handleEdit = async () => {
        if (!selectedDept) return;
        try {
            await departmentApi.update(selectedDept.id, {
                name: selectedDept.name,
                description: selectedDept.description,
                parentId: selectedDept.parentId,
                managerId: selectedDept.managerId,
                storageQuotaBytes: selectedDept.storageQuotaBytes
            });
            toast.success('Département mis à jour');
            setIsEditModalOpen(false);
            setSelectedDept(null);
            loadDepartments();
        } catch (error) {
            console.error(error);
            toast.error('Erreur lors de la mise à jour');
        }
    };

    const filteredDepts = departments.filter(dept =>
        dept.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const formatBytes = (bytes?: number) => {
        if (!bytes) return 'Illimité';
        const gb = bytes / (1024 * 1024 * 1024);
        if (gb >= 1) return `${gb.toFixed(1)} Go`;
        const mb = bytes / (1024 * 1024);
        return `${mb.toFixed(0)} Mo`;
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Départements</h1>
                    <p className="text-gray-500 dark:text-gray-400">Gérez la structure hiérarchique de votre organisation</p>
                </div>
                <Button variant="primary" onClick={() => setIsCreateModalOpen(true)}>
                    <Plus className="w-4 h-4 mr-2" />
                    Nouveau Département
                </Button>
            </div>

            {/* Search */}
            <div className="max-w-md">
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <Input
                        placeholder="Rechercher un département..."
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
                    {filteredDepts.map((dept) => (
                        <Card key={dept.id} className={`p-4 hover:shadow-md transition-shadow ${dept.parentId ? 'ml-8 border-l-4 border-purple-300' : ''}`}>
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-4">
                                    <div className="p-3 bg-purple-100 dark:bg-purple-900/30 rounded-lg text-purple-600 dark:text-purple-400">
                                        <Building size={24} />
                                    </div>
                                    <div>
                                        <div className="flex items-center gap-2">
                                            <h3 className="font-semibold text-lg">{dept.name}</h3>
                                            {dept.parentName && (
                                                <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded">
                                                    Sous-dept de {dept.parentName}
                                                </span>
                                            )}
                                        </div>
                                        <p className="text-sm text-gray-500">{dept.description || 'Aucune description'}</p>
                                        <div className="flex items-center gap-4 mt-2 text-xs text-gray-400">
                                            {dept.managerName && (
                                                <span className="flex items-center gap-1">
                                                    <User size={12} />
                                                    Manager: {dept.managerName}
                                                </span>
                                            )}
                                            {dept.childrenCount !== undefined && dept.childrenCount > 0 && (
                                                <span className="flex items-center gap-1">
                                                    <ChevronRight size={12} />
                                                    {dept.childrenCount} sous-département(s)
                                                </span>
                                            )}
                                            <span className="flex items-center gap-1">
                                                Quota: {formatBytes(dept.storageQuotaBytes)}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <Button variant="ghost" size="sm" className="text-blue-500 hover:bg-blue-50" onClick={() => {
                                        setSelectedDept(dept);
                                        setIsEditModalOpen(true);
                                    }}>
                                        <Edit className="w-4 h-4" />
                                    </Button>
                                    <Button variant="ghost" size="sm" className="text-red-500 hover:bg-red-50" onClick={() => handleDelete(dept.id)}>
                                        <Trash2 className="w-4 h-4" />
                                    </Button>
                                </div>
                            </div>
                        </Card>
                    ))}
                    {filteredDepts.length === 0 && (
                        <div className="text-center py-10 text-gray-500">
                            Aucun département trouvé.
                        </div>
                    )}
                </div>
            )}

            {/* Create Modal */}
            <Modal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                title="Créer un département"
                footer={
                    <>
                        <Button variant="ghost" onClick={() => setIsCreateModalOpen(false)}>Annuler</Button>
                        <Button variant="primary" onClick={handleCreate}>Créer</Button>
                    </>
                }
            >
                <div className="space-y-4">
                    <Input
                        label="Nom du département"
                        placeholder="Marketing, IT..."
                        value={newDept.name}
                        onChange={(e) => setNewDept({ ...newDept, name: e.target.value })}
                    />
                    <Input
                        label="Description"
                        placeholder="Description optionnelle"
                        value={newDept.description}
                        onChange={(e) => setNewDept({ ...newDept, description: e.target.value })}
                    />
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Département Parent</label>
                        <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
                            value={newDept.parentId || ''}
                            onChange={(e) => setNewDept({ ...newDept, parentId: e.target.value ? Number(e.target.value) : undefined })}
                        >
                            <option value="">Aucun (Département racine)</option>
                            {departments.map(d => (
                                <option key={d.id} value={d.id}>{d.name}</option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Manager</label>
                        <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
                            value={newDept.managerId || ''}
                            onChange={(e) => setNewDept({ ...newDept, managerId: e.target.value ? Number(e.target.value) : undefined })}
                        >
                            <option value="">Aucun manager</option>
                            {users.map(u => (
                                <option key={u.id} value={u.id}>{u.firstName} {u.lastName}</option>
                            ))}
                        </select>
                    </div>
                    <Input
                        label="Quota de stockage (Mo)"
                        type="number"
                        placeholder="Ex: 1024 pour 1 Go"
                        value={newDept.storageQuotaBytes ? newDept.storageQuotaBytes / (1024 * 1024) : ''}
                        onChange={(e) => setNewDept({ ...newDept, storageQuotaBytes: e.target.value ? Number(e.target.value) * 1024 * 1024 : undefined })}
                    />
                </div>
            </Modal>

            {/* Edit Modal */}
            <Modal
                isOpen={isEditModalOpen}
                onClose={() => { setIsEditModalOpen(false); setSelectedDept(null); }}
                title="Modifier le département"
                footer={
                    <>
                        <Button variant="ghost" onClick={() => { setIsEditModalOpen(false); setSelectedDept(null); }}>Annuler</Button>
                        <Button variant="primary" onClick={handleEdit}>Sauvegarder</Button>
                    </>
                }
            >
                {selectedDept && (
                    <div className="space-y-4">
                        <Input
                            label="Nom du département"
                            value={selectedDept.name}
                            onChange={(e) => setSelectedDept({ ...selectedDept, name: e.target.value })}
                        />
                        <Input
                            label="Description"
                            value={selectedDept.description || ''}
                            onChange={(e) => setSelectedDept({ ...selectedDept, description: e.target.value })}
                        />
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Département Parent</label>
                            <select
                                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
                                value={selectedDept.parentId || ''}
                                onChange={(e) => setSelectedDept({ ...selectedDept, parentId: e.target.value ? Number(e.target.value) : undefined })}
                            >
                                <option value="">Aucun (Département racine)</option>
                                {departments.filter(d => d.id !== selectedDept.id).map(d => (
                                    <option key={d.id} value={d.id}>{d.name}</option>
                                ))}
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Manager</label>
                            <select
                                className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
                                value={selectedDept.managerId || ''}
                                onChange={(e) => setSelectedDept({ ...selectedDept, managerId: e.target.value ? Number(e.target.value) : undefined })}
                            >
                                <option value="">Aucun manager</option>
                                {users.map(u => (
                                    <option key={u.id} value={u.id}>{u.firstName} {u.lastName}</option>
                                ))}
                            </select>
                        </div>
                        <Input
                            label="Quota de stockage (Mo)"
                            type="number"
                            value={selectedDept.storageQuotaBytes ? selectedDept.storageQuotaBytes / (1024 * 1024) : ''}
                            onChange={(e) => setSelectedDept({ ...selectedDept, storageQuotaBytes: e.target.value ? Number(e.target.value) * 1024 * 1024 : undefined })}
                        />
                    </div>
                )}
            </Modal>
        </div>
    );
};
export default DepartmentsPage;
