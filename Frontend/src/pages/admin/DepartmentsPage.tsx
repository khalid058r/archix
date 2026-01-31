import React, { useEffect, useState } from 'react';
import { Plus, Search, Building, Trash2 } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { Modal } from '../../components/ui/Modal/Modal';
import { departmentApi } from '../../api/endpoints/departmentApi';
import type { Department } from '../../types';
import toast from 'react-hot-toast';
import { useAppSelector } from '../../store/hooks';

export const DepartmentsPage = () => {
    const [departments, setDepartments] = useState<Department[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    // Get current org ID to auto-assign
    const currentOrg = useAppSelector((state) => state.auth.currentOrganization);

    // New Dept State
    const [newDept, setNewDept] = useState({
        name: '',
        description: ''
    });

    useEffect(() => {
        loadDepartments();
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
            setNewDept({ name: '', description: '' });
            loadDepartments();
        } catch (error) {
            console.error(error);
            toast.error('Erreur lors de la création');
        }
    };

    const filteredDepts = departments.filter(dept =>
        dept.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Départements</h1>
                    <p className="text-gray-500 dark:text-gray-400">Gérez la structure de votre organisation</p>
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
                        <Card key={dept.id} className="p-4 flex items-center justify-between hover:shadow-md transition-shadow">
                            <div className="flex items-center gap-4">
                                <div className="p-3 bg-purple-100 dark:bg-purple-900/30 rounded-lg text-purple-600 dark:text-purple-400">
                                    <Building size={24} />
                                </div>
                                <div>
                                    <h3 className="font-semibold text-lg">{dept.name}</h3>
                                    <p className="text-sm text-gray-500">{dept.description || 'Aucune description'}</p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                <Button variant="ghost" size="sm" className="text-red-500 hover:bg-red-50" onClick={() => handleDelete(dept.id)}>
                                    <Trash2 className="w-4 h-4" />
                                </Button>
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
                </div>
            </Modal>
        </div>
    );
};
export default DepartmentsPage;
