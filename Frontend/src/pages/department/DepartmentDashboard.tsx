import { useAppSelector } from '../../store/hooks';
import { selectCurrentUser } from '../../store/slices/authSlice';
import { useGetDepartmentStatsQuery } from '../../api/endpoints/departmentApi';
import { Users, FileText, Clock, HardDrive } from 'lucide-react';
import { Card } from '../../components/ui/Card/Card';

const DepartmentDashboard = () => {
    const user = useAppSelector(selectCurrentUser);

    // Assuming user has department object or ID
    const departmentId = user?.department?.id;

    const { data: stats, isLoading, error } = useGetDepartmentStatsQuery(departmentId!, {
        skip: !departmentId,
    });

    if (!departmentId) {
        return (
            <div className="p-8 text-center bg-yellow-50 rounded-lg border border-yellow-200">
                <h2 className="text-xl font-semibold text-yellow-800">Aucun département assigné</h2>
                <p className="text-yellow-600 mt-2">Vous ne faites partie d'aucun département pour le moment.</p>
            </div>
        );
    }

    if (isLoading) return <div className="p-8 text-center text-gray-500">Chargement des données du département...</div>;

    if (error) {
        return (
            <div className="p-8 text-center bg-red-50 rounded-lg border border-red-200">
                <h2 className="text-xl font-semibold text-red-800">Erreur de chargement</h2>
                <p className="text-red-600 mt-2">Impossible de récupérer les informations du département.</p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">{stats?.name}</h1>
                    <p className="text-gray-500">Vue d'ensemble et gestion</p>
                </div>
                <div className="bg-blue-50 text-blue-700 px-4 py-2 rounded-lg text-sm font-medium">
                    ID: {stats?.id}
                </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <Card className="p-6">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-blue-100 text-blue-600 rounded-lg">
                            <Users size={24} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500">Membres</p>
                            <p className="text-2xl font-bold text-gray-900">{stats?.memberCount}</p>
                        </div>
                    </div>
                </Card>

                <Card className="p-6">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-green-100 text-green-600 rounded-lg">
                            <FileText size={24} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500">Documents</p>
                            <p className="text-2xl font-bold text-gray-900">{stats?.documentCount}</p>
                        </div>
                    </div>
                </Card>

                <Card className="p-6">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-amber-100 text-amber-600 rounded-lg">
                            <Clock size={24} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500">En attente</p>
                            <p className="text-2xl font-bold text-gray-900">{stats?.pendingDocumentsCount}</p>
                        </div>
                    </div>
                </Card>

                <Card className="p-6">
                    <div className="flex items-center gap-4">
                        <div className="p-3 bg-purple-100 text-purple-600 rounded-lg">
                            <HardDrive size={24} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500">Stockage</p>
                            <p className="text-2xl font-bold text-gray-900">
                                {stats?.storageUsedBytes ? (stats.storageUsedBytes / 1024 / 1024).toFixed(2) : '0'} MB
                            </p>
                        </div>
                    </div>
                </Card>
            </div>

            {/* Quick Actions / Members Preview (Placeholder for now) */}
            <div className="bg-white p-6 rounded-lg shadow-sm border mt-8">
                <h3 className="text-lg font-semibold mb-4">Membres du département</h3>
                <p className="text-gray-500 text-sm">Liste complète disponible dans la section "Membres".</p>
                {/* Later: Add simplified members list here */}
            </div>
        </div>
    );
};

export default DepartmentDashboard;
