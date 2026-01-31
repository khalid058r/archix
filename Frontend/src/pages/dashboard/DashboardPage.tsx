import { useAppSelector } from '../../store/hooks';
import { selectCurrentUser, selectCurrentOrganization } from '../../store/slices/authSlice';
import { DashboardStats } from '../../components/features/dashboard/DashboardStats';
import { DocumentList } from '../../components/features/documents/DocumentList';
import { AdminQuickActions } from '../../components/features/dashboard/AdminQuickActions';

const DashboardPage = () => {
    const user = useAppSelector(selectCurrentUser);
    const currentOrganization = useAppSelector(selectCurrentOrganization);

    if (!currentOrganization) {
        return (
            <div className="flex flex-col items-center justify-center min-h-[50vh] text-center space-y-4">
                <div className="bg-gray-100 p-4 rounded-full">
                    <svg className="w-8 h-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                    </svg>
                </div>
                <h2 className="text-xl font-semibold text-gray-900">Aucune organisation sélectionnée</h2>
                <p className="text-gray-500 max-w-sm">
                    Vous ne faites partie d'aucune organisation pour le moment. Veuillez contacter un administrateur ou créer une nouvelle organisation.
                </p>
            </div>
        );
    }

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

            <AdminQuickActions />

            <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Documents Récents</h2>
                <DocumentList compact limit={5} />
            </div>
        </div>
    );
};

export default DashboardPage;
