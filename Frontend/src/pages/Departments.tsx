import { useState, useEffect } from 'react';
import { Plus, Search, Edit2, Trash2, Users, Building2 } from 'lucide-react';
import {
    Button,
    Input,
    Card,
    CardContent,
    Badge,
    Spinner,
    useToast
} from '../components/ui';
import { departmentService } from '../services';
import type { Department } from '../types';
import './Departments.css';

export function Departments() {
    const { toast } = useToast();

    const [departments, setDepartments] = useState<Department[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');

    useEffect(() => {
        loadDepartments();
    }, []);

    const loadDepartments = async () => {
        try {
            setIsLoading(true);
            const response = await departmentService.getAll(0, 100);
            setDepartments(response.content);
        } catch (error) {
            console.error('Error loading departments:', error);
            toast.error('Erreur lors du chargement des départements');
        } finally {
            setIsLoading(false);
        }
    };

    const handleDelete = async (dept: Department) => {
        if (window.confirm(`Supprimer le département "${dept.name}" ?`)) {
            try {
                await departmentService.delete(dept.id);
                toast.success('Département supprimé');
                loadDepartments();
            } catch (error) {
                toast.error('Erreur lors de la suppression');
            }
        }
    };

    const filteredDepartments = (departments || []).filter(dept =>
        dept.name.toLowerCase().includes(searchQuery.toLowerCase())
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
        <div className="departments-page">
            {/* Header */}
            <div className="page-header">
                <div className="page-header-content">
                    <h1 className="page-title">Départements</h1>
                    <p className="page-description">Gérez la structure organisationnelle</p>
                </div>
                <div className="page-actions">
                    <Button variant="primary" leftIcon={<Plus size={18} />}>
                        Nouveau département
                    </Button>
                </div>
            </div>

            {/* Search */}
            <div className="section-header mb-6">
                <Input
                    placeholder="Rechercher un département..."
                    leftIcon={<Search size={18} />}
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="max-w-md"
                />
            </div>

            {/* Departments Grid */}
            {filteredDepartments.length === 0 ? (
                <div className="empty-state">
                    <Building2 size={48} className="text-secondary mb-4" />
                    <p>Aucun département trouvé</p>
                </div>
            ) : (
                <div className="departments-grid">
                    {filteredDepartments.map((dept) => (
                        <Card key={dept.id} className="department-card" hover>
                            <CardContent>
                                <div className="department-header">
                                    <div className="department-icon">
                                        <Building2 size={24} />
                                    </div>
                                    <div className="department-actions">
                                        <Button variant="ghost" size="sm">
                                            <Edit2 size={16} />
                                        </Button>
                                        <Button
                                            variant="ghost"
                                            size="sm"
                                            onClick={() => handleDelete(dept)}
                                        >
                                            <Trash2 size={16} />
                                        </Button>
                                    </div>
                                </div>

                                <h3 className="department-name">{dept.name}</h3>

                                {dept.description && (
                                    <p className="department-description">{dept.description}</p>
                                )}

                                <div className="department-meta">
                                    <div className="department-stat">
                                        <Users size={14} />
                                        <span>{dept.userCount || 0} utilisateurs</span>
                                    </div>
                                    <Badge variant="default" size="sm">
                                        {new Date(dept.createdAt).toLocaleDateString()}
                                    </Badge>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}

export default Departments;
