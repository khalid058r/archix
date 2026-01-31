import { useState } from 'react';
import { Search, Filter, X } from 'lucide-react';
import { DocumentList } from '../../components/features/documents/DocumentList';
import { Button } from '../../components/ui/Button/Button';
import { ViewToggle } from '../../components/ui/ViewToggle/ViewToggle';
import type { DocumentStatus } from '../../types/document.types';

export const SearchPage = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [status, setStatus] = useState<DocumentStatus | ''>('');
    const [showFilters, setShowFilters] = useState(false);
    const [viewMode, setViewMode] = useState<'list' | 'grid'>('grid');

    // Debounce logic could be added here, currently simple state
    // Ideally use useDebounce hook for search term

    const handleClearFilters = () => {
        setSearchTerm('');
        setStatus('');
    };

    const hasActiveFilters = searchTerm || status;

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4">
                <div className="flex items-center justify-between">
                    <h1 className="text-2xl font-bold text-gray-900">Recherche Avancée</h1>
                    <div className="flex items-center gap-3">
                        <ViewToggle viewMode={viewMode} onChange={setViewMode} />
                        {hasActiveFilters && (
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={handleClearFilters}
                                className="text-gray-500 hover:text-error"
                            >
                                <X size={16} className="mr-2" />
                                Réinitialiser
                            </Button>
                        )}
                    </div>
                </div>

                <div className="flex flex-col md:flex-row gap-4">
                    {/* Main Search Input */}
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                        <input
                            type="text"
                            placeholder="Rechercher par nom..."
                            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>

                    {/* Filter Toggle (Mobile) or Statut Select */}
                    <div className="flex gap-2">
                        <select
                            className="px-4 py-2 border border-gray-200 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary"
                            value={status}
                            onChange={(e) => setStatus(e.target.value as DocumentStatus | '')}
                        >
                            <option value="">Tous statuts</option>
                            <option value="DRAFT">Brouillon (Draft)</option>
                            <option value="PENDING_REVIEW">En attente (Pending)</option>
                            <option value="IN_REVIEW">En revue</option>
                            <option value="APPROVED">Approuvé</option>
                            <option value="REJECTED">Rejeté</option>
                            <option value="PUBLISHED">Publié</option>
                            <option value="ARCHIVED">Archivé</option>
                        </select>
                    </div>
                </div>
            </div>

            {/* Results */}
            <DocumentList
                viewMode={viewMode}
                queryParams={{
                    search: searchTerm || undefined,
                    status: status || undefined,
                    size: 20
                }}
                limit={20}
            />
        </div>
    );
};

export default SearchPage;
