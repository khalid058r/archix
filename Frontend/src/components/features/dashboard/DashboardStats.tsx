import { useEffect, useState } from 'react';
import { FileText, Clock, FileCheck, File, Users, Building } from 'lucide-react';
import { Card } from '../../ui/Card/Card';
import { cn } from '../../../utils/cn';
import { useGetDocumentsQuery } from '../../../api/endpoints/documentsApi';
import { useAppSelector } from '../../../store/hooks';
import { usePermissions } from '../../../hooks/usePermissions';
import { userApi } from '../../../api/endpoints/userApi';
import { departmentApi } from '../../../api/endpoints/departmentApi';
import { organizationService } from '../../../services/organization.service';

export const DashboardStats = () => {
    const currentOrganization = useAppSelector((state) => state.auth.currentOrganization);
    const { isAdmin, isSuperAdmin } = usePermissions();

    const [adminStats, setAdminStats] = useState({
        users: 0,
        departments: 0,
        organizations: 0
    });

    // Fetch counts for different statuses - Skip if no org selected
    const { data: totalData } = useGetDocumentsQuery({ size: 1 }, { skip: !currentOrganization });
    const { data: draftData } = useGetDocumentsQuery({ status: 'DRAFT', size: 1 }, { skip: !currentOrganization });
    const { data: pendingData } = useGetDocumentsQuery({ status: 'PENDING_REVIEW', size: 1 }, { skip: !currentOrganization });
    const { data: publishedData } = useGetDocumentsQuery({ status: 'PUBLISHED', size: 1 }, { skip: !currentOrganization });

    useEffect(() => {
        const loadAdminStats = async () => {
            if (!currentOrganization) return;
            try {
                if (isAdmin || isSuperAdmin) {
                    const users = await userApi.getAll();
                    const depts = await departmentApi.getAll();
                    setAdminStats(prev => ({ ...prev, users: users.length, departments: depts.length }));
                }
                if (isSuperAdmin) {
                    const orgs = await organizationService.getAll();
                    setAdminStats(prev => ({ ...prev, organizations: orgs.length }));
                }
            } catch (error) {
                console.error("Failed to load admin stats", error);
            }
        };
        loadAdminStats();
    }, [currentOrganization, isAdmin, isSuperAdmin]);

    const stats = [
        {
            label: 'Total Documents',
            value: totalData?.totalElements || 0,
            icon: FileText,
            color: 'text-primary',
            bg: 'bg-primary/10',
            show: true
        },
        {
            label: 'Brouillons',
            value: draftData?.totalElements || 0,
            icon: File,
            color: 'text-gray-600',
            bg: 'bg-gray-100',
            show: true
        },
        {
            label: 'En Revue',
            value: pendingData?.totalElements || 0,
            icon: Clock,
            color: 'text-warning',
            bg: 'bg-warning/10',
            show: true
        },
        {
            label: 'Publiés',
            value: publishedData?.totalElements || 0,
            icon: FileCheck,
            color: 'text-success',
            bg: 'bg-success/10',
            show: true
        },
        // Admin Stats
        {
            label: 'Utilisateurs',
            value: adminStats.users,
            icon: Users,
            color: 'text-blue-600',
            bg: 'bg-blue-100',
            show: isAdmin || isSuperAdmin
        },
        {
            label: 'Départements',
            value: adminStats.departments,
            icon: Building,
            color: 'text-purple-600',
            bg: 'bg-purple-100',
            show: isAdmin || isSuperAdmin
        },
        {
            label: 'Organisations (Global)',
            value: adminStats.organizations,
            icon: Building, // Using Building for Org too
            color: 'text-indigo-600',
            bg: 'bg-indigo-100',
            show: isSuperAdmin
        }
    ];

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {stats.filter(s => s.show).map((stat, index) => (
                <Card key={index} className="flex items-center p-6 border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
                    <div className={cn("p-4 rounded-full mr-4", stat.bg, stat.color)}>
                        <stat.icon size={24} />
                    </div>
                    <div>
                        <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                        <h3 className="text-2xl font-bold text-gray-900 mt-1">{stat.value}</h3>
                    </div>
                </Card>
            ))}
        </div>
    );
};
