import React, { useEffect, useState } from 'react';
import { Plus, Search, Building2, MoreVertical, Trash2, ExternalLink } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { organizationService } from '../../services/organization.service';
import type { OrganizationDto } from '../../types/organization.types';
import toast from 'react-hot-toast';

export const Organizations = () => {
    const [organizations, setOrganizations] = useState<OrganizationDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        loadOrganizations();
    }, []);

    const loadOrganizations = async () => {
        try {
            setLoading(true);
            const data = await organizationService.getAll();
            setOrganizations(data);
        } catch (error) {
            console.error('Failed to load organizations', error);
            toast.error('Erreur lors du chargement des organisations');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Êtes-vous sûr de vouloir supprimer cette organisation ?')) return;
        try {
            await organizationService.delete(id);
            toast.success('Organisation supprimée');
            loadOrganizations();
        } catch (error) {
            toast.error('Erreur lors de la suppression');
        }
    };

    const filteredOrgs = organizations.filter(org =>
        org.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Organisations</h1>
                    <p className="text-gray-500 dark:text-gray-400">Gérez les organisations et leurs accès</p>
                </div>
                <Button variant="primary" onClick={() => toast('Fonctionnalité de création à venir')}>
                    <Plus className="w-4 h-4 mr-2" />
                    Nouvelle Organisation
                </Button>
            </div>

            {/* Search */}
            <div className="max-w-md">
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
                    <Input
                        placeholder="Rechercher une organisation..."
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
                    {filteredOrgs.map((org) => (
                        <Card key={org.id} className="p-4 flex items-center justify-between hover:shadow-md transition-shadow">
                            <div className="flex items-center gap-4">
                                <div className="p-3 bg-primary/10 rounded-lg text-primary">
                                    <Building2 size={24} />
                                </div>
                                <div>
                                    <h3 className="font-semibold text-lg">{org.name}</h3>
                                    <p className="text-sm text-gray-500">{org.description || 'Aucune description'}</p>
                                    <div className="flex gap-4 mt-1 text-xs text-gray-400">
                                        <span>Créé le {new Date(org.createdAt).toLocaleDateString()}</span>
                                        <span>•</span>
                                        <span>Quota: {org.storageQuota ? `${org.storageQuota}GB` : 'Illimité'}</span>
                                    </div>
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                <Link to={`/admin/organizations/${org.id}`}>
                                    <Button variant="outline" size="sm">
                                        <ExternalLink className="w-4 h-4 mr-2" />
                                        Gérer
                                    </Button>
                                </Link>
                                <Button variant="ghost" size="sm" className="text-red-500 hover:bg-red-50" onClick={() => handleDelete(org.id)}>
                                    <Trash2 className="w-4 h-4" />
                                </Button>
                            </div>
                        </Card>
                    ))}
                    {filteredOrgs.length === 0 && (
                        <div className="text-center py-10 text-gray-500">
                            Aucune organisation trouvée.
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default Organizations;
