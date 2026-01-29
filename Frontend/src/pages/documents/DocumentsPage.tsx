import { Plus, Filter, Search } from 'lucide-react';
import { Link } from 'react-router-dom';
import { DocumentList } from '../../components/features/documents/DocumentList';
import { Button } from '../../components/ui/Button/Button';

const DocumentsPage = () => {
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
                    />
                </div>

                <div className="flex gap-3 w-full md:w-auto">
                    <select className="px-4 py-2 border border-gray-300 rounded-md text-sm bg-white focus:ring-2 focus:ring-primary outline-none">
                        <option value="">Tous les départements</option>
                        <option value="finance">Finance</option>
                        <option value="rh">Ressources Humaines</option>
                        <option value="it">IT</option>
                    </select>

                    <select className="px-4 py-2 border border-gray-300 rounded-md text-sm bg-white focus:ring-2 focus:ring-primary outline-none">
                        <option value="">Tous statuts</option>
                        <option value="DRAFT">Brouillon</option>
                        <option value="APPROVED">Approuvé</option>
                    </select>

                    <Button variant="outline" className="px-3">
                        <Filter size={18} />
                    </Button>
                </div>
            </div>

            <DocumentList />
        </div>
    );
};

export default DocumentsPage;
