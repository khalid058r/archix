import { UserPlus, Building, FileBarChart } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../ui/Button/Button';
import { Card } from '../../ui/Card/Card';
import { usePermissions } from '../../../hooks/usePermissions';

export const AdminQuickActions = () => {
    const navigate = useNavigate();
    const { isAdmin, isSuperAdmin } = usePermissions();

    if (!isAdmin && !isSuperAdmin) return null;

    return (
        <Card className="p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Actions Rapides (Admin)</h2>
            <div className="flex flex-wrap gap-4">
                <Button variant="outline" onClick={() => navigate('/admin/users')}>
                    <UserPlus className="w-4 h-4 mr-2" />
                    Gérer Utilisateurs
                </Button>
                <Button variant="outline" onClick={() => navigate('/admin/departments')}>
                    <Building className="w-4 h-4 mr-2" />
                    Gérer Départements
                </Button>
                <Button variant="outline" onClick={() => navigate('/admin/audit')}>
                    <FileBarChart className="w-4 h-4 mr-2" />
                    Voir Logs Audit
                </Button>
            </div>
        </Card>
    );
};
