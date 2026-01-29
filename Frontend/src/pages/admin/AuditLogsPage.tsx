import { useState, useEffect } from 'react';
import { Card } from '../../components/ui/Card/Card';
import { auditService } from '../../services';
import type { AuditLog } from '../../types';
import { FileSearch, User, Clock, Filter } from 'lucide-react';

const AuditLogsPage = () => {
    const [logs, setLogs] = useState<AuditLog[]>([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('');

    useEffect(() => {
        fetchLogs();
    }, []);

    const fetchLogs = async () => {
        try {
            setLoading(true);
            const data = await auditService.getLogs();
            setLogs(data);
        } catch (err) {
            console.error("Failed to fetch audit logs", err);
        } finally {
            setLoading(false);
        }
    };

    const filteredLogs = logs.filter(log =>
        log.action.toLowerCase().includes(filter.toLowerCase()) ||
        log.username.toLowerCase().includes(filter.toLowerCase()) ||
        log.entityName.toLowerCase().includes(filter.toLowerCase()) ||
        log.details.toLowerCase().includes(filter.toLowerCase())
    );

    const getActionColor = (action: string) => {
        if (action.includes('CREATE')) return 'bg-green-100 text-green-800 border-green-200';
        if (action.includes('UPDATE')) return 'bg-blue-100 text-blue-800 border-blue-200';
        if (action.includes('DELETE')) return 'bg-red-100 text-red-800 border-red-200';
        if (action.includes('LOGIN')) return 'bg-purple-100 text-purple-800 border-purple-200';
        return 'bg-gray-100 text-gray-800 border-gray-200';
    };

    if (loading) return (
        <div className="flex h-96 items-center justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
    );

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6 animate-in fade-in duration-500">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <FileSearch className="text-primary" />
                        Audit Logs
                    </h1>
                    <p className="text-sm text-gray-500 mt-1">
                        Historique des actions système et utilisateurs
                    </p>
                </div>
                <div className="relative">
                    <Filter className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                    <input
                        type="text"
                        placeholder="Filtrer les logs..."
                        className="pl-9 pr-4 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 w-64"
                        value={filter}
                        onChange={(e) => setFilter(e.target.value)}
                    />
                </div>
            </div>

            <Card className="overflow-hidden border-0 shadow-md ring-1 ring-gray-100">
                <div className="overflow-x-auto">
                    <table className="w-full text-sm text-left">
                        <thead className="text-xs text-gray-500 uppercase bg-gray-50 border-b border-gray-100">
                            <tr>
                                <th className="px-6 py-3 font-medium">Action</th>
                                <th className="px-6 py-3 font-medium">Utilisateur</th>
                                <th className="px-6 py-3 font-medium">Cible</th>
                                <th className="px-6 py-3 font-medium">Détails</th>
                                <th className="px-6 py-3 font-medium">Date</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100 bg-white">
                            {filteredLogs.map((log) => (
                                <tr key={log.id} className="hover:bg-gray-50/50 transition-colors">
                                    <td className="px-6 py-4">
                                        <span className={`px-2 py-1 rounded-md text-xs font-semibold border ${getActionColor(log.action)}`}>
                                            {log.action}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center gap-2">
                                            <div className="w-6 h-6 rounded-full bg-gray-100 flex items-center justify-center text-gray-400">
                                                <User size={14} />
                                            </div>
                                            <span className="font-medium text-gray-900">{log.username}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <span className="inline-flex items-center text-gray-600">
                                            {log.entityName} <span className="text-gray-400 mx-1">#</span>{log.entityId}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-gray-600 max-w-md truncate" title={log.details}>
                                        {log.details}
                                    </td>
                                    <td className="px-6 py-4 text-gray-500 whitespace-nowrap">
                                        <div className="flex items-center gap-1.5">
                                            <Clock size={14} className="text-gray-400" />
                                            {new Date(log.timestamp).toLocaleString()}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            {filteredLogs.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-6 py-12 text-center text-gray-500 italic">
                                        Aucun log trouvé
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
};

export default AuditLogsPage;
