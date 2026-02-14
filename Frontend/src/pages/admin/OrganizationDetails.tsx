import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, UserPlus } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { organizationService } from '../../services/organization.service';
import type { OrganizationDto } from '../../types/organization.types';
import toast from 'react-hot-toast';

export const OrganizationDetails = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [organization, setOrganization] = useState<OrganizationDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [inviteEmail, setInviteEmail] = useState('');

    useEffect(() => {
        if (id) loadOrganization(parseInt(id));
    }, [id]);

    const loadOrganization = async (orgId: number) => {
        try {
            setLoading(true);
            const data = await organizationService.getById(orgId);
            setOrganization(data);
        } catch (error) {
            toast.error('Erreur lors du chargement');
            navigate('/admin/organizations');
        } finally {
            setLoading(false);
        }
    };

    const handleInvite = async () => {
        if (!organization || !inviteEmail) return;
        try {
            await organizationService.addMember(organization.id, { email: inviteEmail });
            toast.success(`Invitation envoyée à ${inviteEmail}`);
            setInviteEmail('');
        } catch (error) {
            toast.error("Erreur lors de l'invitation");
        }
    };

    if (loading) return <div>Chargement...</div>;
    if (!organization) return <div>Organisation introuvable</div>;

    return (
        <div className="space-y-6">
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="sm" onClick={() => navigate('/admin/organizations')}>
                    <ArrowLeft className="w-4 h-4 mr-2" />
                    Retour
                </Button>
                <h1 className="text-2xl font-bold">{organization.name}</h1>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Info Card */}
                <Card className="p-6 md:col-span-2 space-y-4">
                    <h2 className="text-lg font-semibold">Informations</h2>
                    <div className="grid gap-4">
                        <div>
                            <label className="text-sm font-medium text-gray-500">Nom</label>
                            <Input value={organization.name} disabled />
                        </div>
                        <div>
                            <label className="text-sm font-medium text-gray-500">Description</label>
                            <Input value={organization.description || ''} disabled />
                        </div>
                    </div>
                </Card>

                {/* Invite Card */}
                <Card className="p-6 space-y-4">
                    <h2 className="text-lg font-semibold">Inviter un membre</h2>
                    <p className="text-sm text-gray-500">Ajoutez un utilisateur à cette organisation par email.</p>
                    <div className="flex gap-2">
                        <Input
                            placeholder="email@example.com"
                            value={inviteEmail}
                            onChange={(e) => setInviteEmail(e.target.value)}
                        />
                        <Button onClick={handleInvite} disabled={!inviteEmail}>
                            <UserPlus className="w-4 h-4" />
                        </Button>
                    </div>
                </Card>
            </div>
        </div>
    );
};

export default OrganizationDetails;
