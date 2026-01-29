import { useAppSelector } from '../../store/hooks';
import { selectCurrentUser } from '../../store/slices/authSlice';
import { DashboardStats } from '../../components/features/dashboard/DashboardStats';
import { DocumentList } from '../../components/features/documents/DocumentList';

const DashboardPage = () => {
    const user = useAppSelector(selectCurrentUser);

    return (
        <div className="space-y-8">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        Bonjour, {user?.firstName || 'Utilisateur'} 👋
                    </h1>
                    <p className="text-gray-500">Voici ce qu'il se passe sur Archix-Base aujourd'hui.</p>
                </div>
                <div className="text-sm text-gray-500 bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm">
                    {new Date().toLocaleDateString('fr-FR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                </div>
            </div>

            <DashboardStats />

            <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Documents Récents</h2>
                <DocumentList compact limit={5} />
            </div>
        </div>
    );
};

export default DashboardPage;
