import { useEffect, useState } from 'react';
import { Plus, User as UserIcon, Mail } from 'lucide-react';
import { toast } from 'react-hot-toast';

import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { useAppSelector } from '../../store/hooks';
import { selectCurrentOrganization } from '../../store/slices/authSlice';
import { organizationApi } from '../../api/endpoints/organizationApi';
import { type User } from '../../types';
import { InviteMemberModal } from '../../components/organization/InviteMemberModal';

const MembersPage = () => {
    const currentOrg = useAppSelector(selectCurrentOrganization);
    const [members, setMembers] = useState<User[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);

    useEffect(() => {
        if (currentOrg?.id) {
            fetchMembers();
        }
    }, [currentOrg?.id]);

    const fetchMembers = async () => {
        if (!currentOrg) return;
        setIsLoading(true);
        try {
            const data = await organizationApi.getMembers(currentOrg.id);
            setMembers(data);
        } catch (error) {
            console.error('Error fetching members:', error);
            toast.error("Impossible de charger les membres");
        } finally {
            setIsLoading(false);
        }
    };

    if (!currentOrg) {
        return <div className="p-6">Veuillez sélectionner une organisation.</div>;
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Membres</h1>
                    <p className="text-gray-600">Gérez l'équipe de {currentOrg.name}</p>
                </div>
                <Button onClick={() => setIsInviteModalOpen(true)}>
                    <Plus size={20} className="mr-2" />
                    Inviter un membre
                </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {isLoading ? (
                    <div className="col-span-full py-12 text-center text-gray-500">
                        Chargement des membres...
                    </div>
                ) : members.length === 0 ? (
                    <div className="col-span-full py-12 text-center text-gray-500 bg-white rounded-lg border border-dashed border-gray-300">
                        <UserIcon className="mx-auto h-12 w-12 text-gray-400 mb-4" />
                        <h3 className="text-lg font-medium text-gray-900">Aucun membre</h3>
                        <p className="mt-1 text-sm text-gray-500">Commencez par inviter des collègues.</p>
                    </div>
                ) : (
                    members.map((member) => (
                        <Card key={member.id} className="flex items-center p-4 space-x-4">
                            <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-lg">
                                {member.firstName?.[0]}{member.lastName?.[0]}
                            </div>
                            <div>
                                <h3 className="font-medium text-gray-900">
                                    {member.firstName} {member.lastName}
                                </h3>
                                <div className="flex items-center text-sm text-gray-500">
                                    <Mail size={14} className="mr-1" />
                                    {member.email}
                                </div>
                            </div>
                        </Card>
                    ))
                )}
            </div>

            <InviteMemberModal
                isOpen={isInviteModalOpen}
                onClose={() => setIsInviteModalOpen(false)}
                organizationId={currentOrg.id}
            />
        </div>
    );
};

export default MembersPage;
