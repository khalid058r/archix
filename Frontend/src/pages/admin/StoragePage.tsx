import { useState, useEffect } from 'react';
import {
    HardDrive, Building2, AlertTriangle, Trash2, RefreshCw,
    FileText, Image, Video, Music, Archive, Settings
} from 'lucide-react';
import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { StorageBar } from '../../components/admin';
import toast from 'react-hot-toast';
import { useGetStorageStatsQuery } from '../../api/endpoints/reportApi';

interface StorageStats {
    totalUsed: number;
    totalQuota: number;
    byType: {
        type: string;
        size: number;
        count: number;
        icon: React.ReactNode;
        color: string;
    }[];
    byOrganization: {
        id: number;
        name: string;
        used: number;
        quota: number;
    }[];
    orphanedFiles: number;
    orphanedSize: number;
}

const StoragePage = () => {
    const [stats, setStats] = useState<StorageStats | null>(null);
    const [loading, setLoading] = useState(true);
    const [cleaningUp, setCleaningUp] = useState(false);

    // Fetch real data from API
    const { data: apiStorage, isLoading: apiLoading } = useGetStorageStatsQuery();

    useEffect(() => {
        if (apiStorage?.data && apiStorage.data.totalBytes !== undefined) {
            // Map API data to local interface
            const mimeIconMap: Record<string, { icon: React.ReactNode; color: string }> = {
                'application/pdf': { icon: <FileText size={20} />, color: 'bg-red-500' },
                'image': { icon: <Image size={20} />, color: 'bg-blue-500' },
                'video': { icon: <Video size={20} />, color: 'bg-purple-500' },
                'audio': { icon: <Music size={20} />, color: 'bg-yellow-500' },
            };
            const byType = (apiStorage.data.byType || []).map(t => {
                const key = Object.keys(mimeIconMap).find(k => t.mimeType?.startsWith(k)) || '';
                return {
                    type: t.mimeType?.split('/').pop()?.toUpperCase() || 'OTHER',
                    size: 0,
                    count: t.count,
                    icon: mimeIconMap[key]?.icon || <FileText size={20} />,
                    color: mimeIconMap[key]?.color || 'bg-gray-500',
                };
            });
            setStats({
                totalUsed: apiStorage.data.totalBytes,
                totalQuota: 100 * 1024 * 1024 * 1024, // Default 100GB quota
                byType,
                byOrganization: [],
                orphanedFiles: 0,
                orphanedSize: 0,
            });
            setLoading(false);
        } else if (!apiLoading) {
            loadStorageStats(); // Fallback to mock
        }
    }, [apiStorage, apiLoading]);

    const loadStorageStats = async () => {
        try {
            setLoading(true);
            // Simulated data - replace with API call
            setStats({
                totalUsed: 45.2 * 1024 * 1024 * 1024, // 45.2 GB
                totalQuota: 100 * 1024 * 1024 * 1024, // 100 GB
                byType: [
                    { type: 'PDF', size: 15.8 * 1024 * 1024 * 1024, count: 1250, icon: <FileText size={20} />, color: 'bg-red-500' },
                    { type: 'Images', size: 11.3 * 1024 * 1024 * 1024, count: 3420, icon: <Image size={20} />, color: 'bg-blue-500' },
                    { type: 'Documents', size: 9.0 * 1024 * 1024 * 1024, count: 890, icon: <FileText size={20} />, color: 'bg-green-500' },
                    { type: 'Vidéos', size: 4.5 * 1024 * 1024 * 1024, count: 45, icon: <Video size={20} />, color: 'bg-purple-500' },
                    { type: 'Audio', size: 2.1 * 1024 * 1024 * 1024, count: 120, icon: <Music size={20} />, color: 'bg-yellow-500' },
                    { type: 'Archives', size: 1.5 * 1024 * 1024 * 1024, count: 67, icon: <Archive size={20} />, color: 'bg-gray-500' },
                    { type: 'Autres', size: 1.0 * 1024 * 1024 * 1024, count: 234, icon: <FileText size={20} />, color: 'bg-orange-500' },
                ],
                byOrganization: [
                    { id: 1, name: 'TechCorp Inc.', used: 23.5 * 1024 * 1024 * 1024, quota: 50 * 1024 * 1024 * 1024 },
                    { id: 2, name: 'StartupX', used: 8.2 * 1024 * 1024 * 1024, quota: 10 * 1024 * 1024 * 1024 },
                    { id: 3, name: 'AgencyY', used: 13.5 * 1024 * 1024 * 1024, quota: 40 * 1024 * 1024 * 1024 },
                ],
                orphanedFiles: 23,
                orphanedSize: 156 * 1024 * 1024, // 156 MB
            });
        } catch (error) {
            console.error('Failed to load storage stats', error);
            toast.error('Erreur lors du chargement des statistiques');
        } finally {
            setLoading(false);
        }
    };

    const formatSize = (bytes: number) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
    };

    const handleCleanup = async () => {
        if (!window.confirm('Supprimer les fichiers orphelins ? Cette action est irréversible.')) return;

        try {
            setCleaningUp(true);
            // await storageService.cleanupOrphanedFiles();
            await new Promise(resolve => setTimeout(resolve, 2000)); // Simulate
            toast.success('Nettoyage terminé');
            loadStorageStats();
        } catch (error) {
            toast.error('Erreur lors du nettoyage');
        } finally {
            setCleaningUp(false);
        }
    };

    const handleQuotaChange = async (_orgId: number, _newQuota: number) => {
        try {
            // await organizationService.updateQuota(orgId, newQuota);
            toast.success('Quota mis à jour');
            loadStorageStats();
        } catch (error) {
            toast.error('Erreur lors de la mise à jour du quota');
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-96">
                <div className="w-8 h-8 border-4 border-primary/30 border-t-primary rounded-full animate-spin" />
            </div>
        );
    }

    if (!stats) return null;

    const usedPercentage = (stats.totalUsed / stats.totalQuota) * 100;

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <HardDrive className="text-primary" />
                        Gestion du Stockage
                    </h1>
                    <p className="text-gray-500">
                        Surveillez et gérez l'utilisation du stockage
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="outline" size="sm" onClick={loadStorageStats}>
                        <RefreshCw size={16} className="mr-2" />
                        Actualiser
                    </Button>
                    <Button variant="outline" size="sm">
                        <Settings size={16} className="mr-2" />
                        Configuration
                    </Button>
                </div>
            </div>

            {/* Global Storage */}
            <Card className="p-6">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-semibold text-gray-900">Stockage Global</h2>
                    <span className={`text-sm font-medium ${usedPercentage >= 90 ? 'text-red-600' :
                            usedPercentage >= 75 ? 'text-orange-600' :
                                'text-green-600'
                        }`}>
                        {usedPercentage.toFixed(1)}% utilisé
                    </span>
                </div>
                <StorageBar
                    used={stats.totalUsed}
                    total={stats.totalQuota}
                    showDetails
                />

                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6 pt-6 border-t border-gray-100">
                    <div className="text-center">
                        <div className="text-2xl font-bold text-gray-900">{formatSize(stats.totalUsed)}</div>
                        <div className="text-sm text-gray-500">Utilisé</div>
                    </div>
                    <div className="text-center">
                        <div className="text-2xl font-bold text-gray-900">{formatSize(stats.totalQuota - stats.totalUsed)}</div>
                        <div className="text-sm text-gray-500">Disponible</div>
                    </div>
                    <div className="text-center">
                        <div className="text-2xl font-bold text-gray-900">{formatSize(stats.totalQuota)}</div>
                        <div className="text-sm text-gray-500">Total</div>
                    </div>
                    <div className="text-center">
                        <div className="text-2xl font-bold text-gray-900">
                            {stats.byType.reduce((acc, t) => acc + t.count, 0).toLocaleString()}
                        </div>
                        <div className="text-sm text-gray-500">Fichiers</div>
                    </div>
                </div>
            </Card>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* By Type */}
                <Card className="p-6">
                    <h2 className="text-lg font-semibold text-gray-900 mb-4">Répartition par type</h2>
                    <div className="space-y-4">
                        {stats.byType.map(type => {
                            const percentage = (type.size / stats.totalUsed) * 100;
                            return (
                                <div key={type.type} className="flex items-center gap-4">
                                    <div className={`p-2 rounded-lg ${type.color.replace('bg-', 'bg-').replace('-500', '-100')} ${type.color.replace('bg-', 'text-')}`}>
                                        {type.icon}
                                    </div>
                                    <div className="flex-1">
                                        <div className="flex items-center justify-between mb-1">
                                            <span className="text-sm font-medium text-gray-900">{type.type}</span>
                                            <span className="text-sm text-gray-500">
                                                {formatSize(type.size)} ({type.count} fichiers)
                                            </span>
                                        </div>
                                        <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                                            <div
                                                className={`h-full rounded-full ${type.color}`}
                                                style={{ width: `${percentage}%` }}
                                            />
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </Card>

                {/* Cleanup */}
                <Card className="p-6">
                    <h2 className="text-lg font-semibold text-gray-900 mb-4">Maintenance</h2>

                    {/* Orphaned Files */}
                    <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg mb-4">
                        <div className="flex items-start gap-3">
                            <AlertTriangle className="text-yellow-600 flex-shrink-0 mt-0.5" size={20} />
                            <div className="flex-1">
                                <h3 className="font-medium text-yellow-800">Fichiers orphelins détectés</h3>
                                <p className="text-sm text-yellow-700 mt-1">
                                    {stats.orphanedFiles} fichier(s) sans document associé ({formatSize(stats.orphanedSize)})
                                </p>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    className="mt-3"
                                    onClick={handleCleanup}
                                    disabled={cleaningUp}
                                >
                                    {cleaningUp ? (
                                        <>
                                            <RefreshCw size={14} className="mr-2 animate-spin" />
                                            Nettoyage...
                                        </>
                                    ) : (
                                        <>
                                            <Trash2 size={14} className="mr-2" />
                                            Nettoyer
                                        </>
                                    )}
                                </Button>
                            </div>
                        </div>
                    </div>

                    {/* Policies */}
                    <div className="space-y-3">
                        <h3 className="text-sm font-medium text-gray-700">Politiques actives</h3>
                        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                            <div>
                                <div className="font-medium text-sm text-gray-900">Rétention automatique</div>
                                <div className="text-xs text-gray-500">Archiver après 365 jours d'inactivité</div>
                            </div>
                            <span className="px-2 py-1 bg-green-100 text-green-700 text-xs rounded-full">Actif</span>
                        </div>
                        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                            <div>
                                <div className="font-medium text-sm text-gray-900">Suppression automatique</div>
                                <div className="text-xs text-gray-500">Supprimer les fichiers temporaires après 7 jours</div>
                            </div>
                            <span className="px-2 py-1 bg-green-100 text-green-700 text-xs rounded-full">Actif</span>
                        </div>
                    </div>
                </Card>
            </div>

            {/* By Organization */}
            <Card className="p-6">
                <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                    <Building2 size={20} className="text-gray-500" />
                    Stockage par Organisation
                </h2>
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead className="bg-gray-50 border-b border-gray-100">
                            <tr>
                                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Organisation</th>
                                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Utilisé</th>
                                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Quota</th>
                                <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase" style={{ width: '40%' }}>Progression</th>
                                <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {stats.byOrganization.map(org => {
                                const percentage = (org.used / org.quota) * 100;
                                const isWarning = percentage >= 75;
                                const isCritical = percentage >= 90;

                                return (
                                    <tr key={org.id} className="hover:bg-gray-50">
                                        <td className="px-4 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="p-2 bg-primary/10 rounded-lg text-primary">
                                                    <Building2 size={18} />
                                                </div>
                                                <span className="font-medium text-gray-900">{org.name}</span>
                                            </div>
                                        </td>
                                        <td className="px-4 py-4 text-sm text-gray-700">
                                            {formatSize(org.used)}
                                        </td>
                                        <td className="px-4 py-4 text-sm text-gray-700">
                                            {formatSize(org.quota)}
                                        </td>
                                        <td className="px-4 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="flex-1">
                                                    <StorageBar
                                                        used={org.used}
                                                        total={org.quota}
                                                        showDetails={false}
                                                    />
                                                </div>
                                                <span className={`text-sm font-medium ${isCritical ? 'text-red-600' :
                                                        isWarning ? 'text-orange-600' :
                                                            'text-gray-600'
                                                    }`}>
                                                    {percentage.toFixed(0)}%
                                                </span>
                                                {isCritical && (
                                                    <AlertTriangle size={16} className="text-red-500" />
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-4 py-4 text-right">
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                onClick={() => {
                                                    const newQuota = prompt('Nouveau quota (en GB):', String(org.quota / (1024 * 1024 * 1024)));
                                                    if (newQuota) {
                                                        handleQuotaChange(org.id, parseFloat(newQuota) * 1024 * 1024 * 1024);
                                                    }
                                                }}
                                            >
                                                Modifier quota
                                            </Button>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
};

export default StoragePage;
