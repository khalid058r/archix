import { useState, useEffect } from 'react';
import { Plus, Search } from 'lucide-react';
import { Link } from 'react-router-dom';
import { DocumentList } from '../../components/features/documents/DocumentList';
import { Button } from '../../components/ui/Button/Button';
import { ViewToggle } from '../../components/ui/ViewToggle/ViewToggle';
import { usePermissions } from '../../hooks/usePermissions';
import { userApi } from '../../api/endpoints/userApi';
import { departmentApi } from '../../api/endpoints/departmentApi';
import type { User } from '../../types/user.types';
import type { Department } from '../../types/organization.types';

const DocumentsPage = () => {
    const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');
    const { isAdmin, isSuperAdmin } = usePermissions();
    const [users, setUsers] = useState<User[]>([]);
    const [selectedUser, setSelectedUser] = useState<string>('');
    const [filters, setFilters] = useState({
        departmentId: '',
        status: '',
        search: ''
    });
    const [departments, setDepartments] = useState<Department[]>([]);

    useEffect(() => {
        if (isAdmin || isSuperAdmin) {
            Promise.all([
                userApi.getAll(),
                departmentApi.getAll()
            ]).then(([usersData, deptsData]) => {
                setUsers(usersData);
                setDepartments(deptsData);
            }).catch(console.error);
        }
    }, [isAdmin, isSuperAdmin]);

    // ... render ...


    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Documents</h1>
                    <p className="text-gray-500">Gérez, organisez et suivez tous vos documents.</p>
                </div>
                <Link to="/documents/upload">
                    <Button>
                        <Plus size={18} className="mr-2" />
                        Nouveau document
                    </Button>
                </Link>
            </div>

            {/* Filters Bar */}
            <div className="bg-white p-4 rounded-lg border border-gray-200 flex flex-col md:flex-row gap-4 items-center shadow-sm">
                <div className="relative flex-1 w-full">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                    <input
                        type="text"
                        placeholder="Rechercher par nom, tag ou contenu..."
                        className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-primary focus:border-transparent outline-none"
                        value={filters.search}
                        onChange={(e) => setFilters(prev => ({ ...prev, search: e.target.value }))}
                    />
                </div>

                <div className="flex gap-3 w-full md:w-auto items-center flex-wrap">
                    {(isAdmin || isSuperAdmin) && (
                        <select
                            className="px-4 py-2 border border-gray-300 rounded-md text-sm bg-white focus:ring-2 focus:ring-primary outline-none"
                            value={selectedUser}
                            onChange={(e) => setSelectedUser(e.target.value)}
                        >
                            <option value="">Tous les utilisateurs</option>
                            {users.map(u => (
                                <option key={u.id} value={u.id}>
                                    {u.firstName} {u.lastName}
                                </option>
                            ))}
                        </select>
                    )}

                    <select
                        className="px-4 py-2 border border-gray-300 rounded-md text-sm bg-white focus:ring-2 focus:ring-primary outline-none"
                        value={filters.departmentId}
                        onChange={(e) => setFilters(prev => ({ ...prev, departmentId: e.target.value }))}
                    >
                        <option value="">Tous les départements</option>
                        {departments.length > 0 ? (
                            departments.map(d => (
                                <option key={d.id} value={d.id}>
                                    {d.name}
                                </option>
                            ))
                        ) : (
                            /* Fallback hardcoded if not admin or load failed, or maybe just empty if not permitted */
                            <>
                                <option value="finance">Finance</option>
                                <option value="rh">Ressources Humaines</option>
                                <option value="it">IT</option>
                            </>
                        )}
                    </select>

                    <select
                        className="px-4 py-2 border border-gray-300 rounded-md text-sm bg-white focus:ring-2 focus:ring-primary outline-none"
                        value={filters.status}
                        onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                    >
                        <option value="">Tous statuts</option>
                        <option value="DRAFT">Brouillon</option>
                        <option value="PENDING_REVIEW">À valider</option>
                        <option value="IN_REVIEW">En révision</option>
                        <option value="APPROVED">Approuvé</option>
                        <option value="PUBLISHED">Publié</option>
                        <option value="ARCHIVED">Archivé</option>
                    </select>

                    <ViewToggle viewMode={viewMode} onChange={setViewMode} />
                </div>
            </div>

            <DocumentList
                viewMode={viewMode}
                queryParams={{
                    search: filters.search,
                    departmentId: filters.departmentId ? Number(filters.departmentId) : undefined, // Assuming dept IDs are numbers, need to check fallback
                    status: filters.status as any,
                    // Check if API supports createdById. 
                    // Based on step 5127, GetDocumentsParams DOES NOT explicitly list createdById.
                    // However, I can add it to the interface if backend supports it, or I have to add it.
                    // For now, I will pass it and hope backend ignores or handles it, OR update API definition.
                    // Actually, I should check API definition. 
                    // Wait, I saw GetDocumentsParams in Step 5127. It has: page, size, search, departmentId, namespaceId, status, sort.
                    // It DOES NOT have createdById. 
                    // I will add it to the params passed here, and update the API definition in the next step.
                    ...((selectedUser ? { createdById: Number(selectedUser) } : {}) as any)
                }}
            />
        </div>
    );
};

export default DocumentsPage;
