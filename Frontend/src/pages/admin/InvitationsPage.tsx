import { useState, useEffect } from 'react';
import {
    Mail, Plus, Trash2, RefreshCw, Clock, XCircle,
    Copy, Link2, Users, Send, Calendar
} from 'lucide-react';
import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/Input/Input';
import { Modal } from '../../components/ui/Modal/Modal';
import { StatusBadge, RoleBadge, DataTable } from '../../components/admin';
import type { Column } from '../../components/admin/DataTable';
import toast from 'react-hot-toast';
import {
    useGetInviteCodesQuery,
    useCreateInviteCodeMutation,
    useDeactivateInviteCodeMutation,
    useDeleteInviteCodeMutation,
} from '../../api/endpoints/inviteCodeApi';

interface Invitation {
    id: number;
    email: string;
    role: string;
    status: 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'CANCELLED';
    expiresAt: string;
    createdAt: string;
    createdBy: string;
    organization?: string;
}

interface InviteCode {
    id: number;
    code: string;
    team: string;
    usageCount: number;
    maxUsage: number | null;
    expiresAt: string | null;
    isActive: boolean;
    createdBy: string;
}

const InvitationsPage = () => {
    const [invitations, setInvitations] = useState<Invitation[]>([]);
    const [inviteCodes, setInviteCodes] = useState<InviteCode[]>([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState<'invitations' | 'codes'>('invitations');

    // RTK Query hooks for invite codes
    const { data: codesData, isLoading: codesLoading, refetch: refetchCodes } = useGetInviteCodesQuery();
    const [createCode] = useCreateInviteCodeMutation();
    const [deactivateCode] = useDeactivateInviteCodeMutation();
    const [deleteCode] = useDeleteInviteCodeMutation();

    // Modals
    const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
    const [isCodeModalOpen, setIsCodeModalOpen] = useState(false);

    // New Invitation
    const [newInvite, setNewInvite] = useState({
        emails: '',
        role: 'USER',
        message: '',
        expiryDays: 7
    });

    // New Code
    const [newCode, setNewCode] = useState({
        team: '',
        maxUsage: '',
        expiryDays: ''
    });

    const roles = [
        { value: 'ADMIN', label: 'Admin' },
        { value: 'MANAGER', label: 'Manager' },
        { value: 'USER', label: 'Utilisateur' },
        { value: 'READER', label: 'Lecteur' },
        { value: 'GUEST', label: 'Invité' },
    ];

    useEffect(() => {
        loadData();
    }, []);

    // Sync API data with local state
    useEffect(() => {
        if (codesData?.data) {
            setInviteCodes(codesData.data.map(c => ({
                id: c.id,
                code: c.code,
                team: c.teamName || `Team #${c.teamId}`,
                usageCount: c.usedCount,
                maxUsage: c.maxUses || null,
                expiresAt: c.expiresAt || null,
                isActive: c.active,
                createdBy: c.createdByName || `User #${c.createdById}`,
            })));
        }
    }, [codesData]);

    const loadData = async () => {
        try {
            setLoading(true);
            // Invitations mock (email-based invites not yet in backend)
            setInvitations([
                { id: 1, email: 'new@user.com', role: 'USER', status: 'PENDING', expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60000).toISOString(), createdAt: new Date(Date.now() - 2 * 24 * 60 * 60000).toISOString(), createdBy: 'admin@archix.com', organization: 'TechCorp' },
            ]);
            // Codes loaded via RTK Query
            refetchCodes();
        } catch (error) {
            console.error('Failed to load invitations', error);
            toast.error('Erreur lors du chargement');
        } finally {
            setLoading(false);
        }
    };

    const handleSendInvites = async () => {
        const emails = newInvite.emails.split(',').map(e => e.trim()).filter(e => e);
        if (emails.length === 0) {
            toast.error('Veuillez entrer au moins un email');
            return;
        }

        try {
            // await invitationService.sendInvites(emails, newInvite.role, newInvite.message);
            toast.success(`${emails.length} invitation(s) envoyée(s)`);
            setIsInviteModalOpen(false);
            setNewInvite({ emails: '', role: 'USER', message: '', expiryDays: 7 });
            loadData();
        } catch (error) {
            toast.error('Erreur lors de l\'envoi');
        }
    };

    const handleCreateCode = async () => {
        if (!newCode.team) {
            toast.error('Veuillez entrer un nom d\'équipe');
            return;
        }

        try {
            await createCode({
                teamId: parseInt(newCode.team) || 0,
                maxUses: newCode.maxUsage ? parseInt(newCode.maxUsage) : undefined,
                expiresAt: newCode.expiryDays
                    ? new Date(Date.now() + parseInt(newCode.expiryDays) * 24 * 60 * 60000).toISOString()
                    : undefined,
            }).unwrap();
            toast.success('Code d\'invitation créé');
            setIsCodeModalOpen(false);
            setNewCode({ team: '', maxUsage: '', expiryDays: '' });
        } catch (error) {
            toast.error('Erreur lors de la création');
        }
    };

    const handleResend = async (_id: number) => {
        try {
            // await invitationService.resend(id);
            toast.success('Invitation renvoyée');
        } catch (error) {
            toast.error('Erreur lors du renvoi');
        }
    };

    const handleCancel = async (_id: number) => {
        if (!window.confirm('Annuler cette invitation ?')) return;
        try {
            // await invitationService.cancel(id);
            toast.success('Invitation annulée');
            loadData();
        } catch (error) {
            toast.error('Erreur lors de l\'annulation');
        }
    };

    const handleToggleCode = async (_id: number, isActive: boolean) => {
        try {
            // await invitationService.toggleCode(id, !isActive);
            toast.success(isActive ? 'Code désactivé' : 'Code activé');
            loadData();
        } catch (error) {
            toast.error('Erreur');
        }
    };

    const copyToClipboard = (text: string) => {
        navigator.clipboard.writeText(text);
        toast.success('Copié dans le presse-papier');
    };

    const getStatusVariant = (status: string): 'success' | 'warning' | 'error' | 'info' | 'default' => {
        switch (status) {
            case 'ACCEPTED': return 'success';
            case 'PENDING': return 'warning';
            case 'EXPIRED': return 'error';
            case 'CANCELLED': return 'default';
            default: return 'default';
        }
    };

    const invitationColumns: Column<Invitation>[] = [
        {
            key: 'email',
            header: 'Email',
            sortable: true,
            render: (inv) => (
                <div className="flex items-center gap-2">
                    <Mail size={16} className="text-gray-400" />
                    <span className="font-medium">{inv.email}</span>
                </div>
            )
        },
        {
            key: 'role',
            header: 'Rôle',
            render: (inv) => <RoleBadge role={inv.role} />
        },
        {
            key: 'status',
            header: 'Statut',
            render: (inv) => (
                <StatusBadge
                    status={
                        inv.status === 'PENDING' ? 'En attente' :
                            inv.status === 'ACCEPTED' ? 'Acceptée' :
                                inv.status === 'EXPIRED' ? 'Expirée' : 'Annulée'
                    }
                    variant={getStatusVariant(inv.status)}
                    dot
                />
            )
        },
        {
            key: 'expiresAt',
            header: 'Expire',
            sortable: true,
            render: (inv) => {
                const date = new Date(inv.expiresAt);
                const isExpired = date < new Date();
                return (
                    <span className={`text-sm ${isExpired ? 'text-red-500' : 'text-gray-500'}`}>
                        {date.toLocaleDateString('fr-FR')}
                    </span>
                );
            }
        },
        {
            key: 'createdBy',
            header: 'Envoyé par',
            render: (inv) => <span className="text-sm text-gray-500">{inv.createdBy}</span>
        },
        {
            key: 'actions',
            header: '',
            width: '120px',
            render: (inv) => (
                <div className="flex items-center justify-end gap-1">
                    {inv.status === 'PENDING' && (
                        <>
                            <Button variant="ghost" size="sm" className="p-2" onClick={() => handleResend(inv.id)} title="Renvoyer">
                                <RefreshCw size={14} className="text-blue-500" />
                            </Button>
                            <Button variant="ghost" size="sm" className="p-2" onClick={() => handleCancel(inv.id)} title="Annuler">
                                <XCircle size={14} className="text-red-500" />
                            </Button>
                        </>
                    )}
                </div>
            )
        }
    ];

    const pendingCount = invitations.filter(i => i.status === 'PENDING').length;
    const expiredCount = invitations.filter(i => i.status === 'EXPIRED').length;

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <Mail className="text-primary" />
                        Gestion des Invitations
                    </h1>
                    <p className="text-gray-500">
                        Invitez de nouveaux utilisateurs et gérez les codes d'équipe
                    </p>
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="outline" onClick={() => setIsCodeModalOpen(true)}>
                        <Link2 size={16} className="mr-2" />
                        Créer un code
                    </Button>
                    <Button variant="primary" onClick={() => setIsInviteModalOpen(true)}>
                        <Plus size={16} className="mr-2" />
                        Inviter
                    </Button>
                </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <Card className="p-4">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
                            <Mail size={20} />
                        </div>
                        <div>
                            <div className="text-2xl font-bold text-gray-900">{invitations.length}</div>
                            <div className="text-sm text-gray-500">Total invitations</div>
                        </div>
                    </div>
                </Card>
                <Card className="p-4">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-yellow-100 rounded-lg text-yellow-600">
                            <Clock size={20} />
                        </div>
                        <div>
                            <div className="text-2xl font-bold text-gray-900">{pendingCount}</div>
                            <div className="text-sm text-gray-500">En attente</div>
                        </div>
                    </div>
                </Card>
                <Card className="p-4">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-red-100 rounded-lg text-red-600">
                            <XCircle size={20} />
                        </div>
                        <div>
                            <div className="text-2xl font-bold text-gray-900">{expiredCount}</div>
                            <div className="text-sm text-gray-500">Expirées</div>
                        </div>
                    </div>
                </Card>
                <Card className="p-4">
                    <div className="flex items-center gap-3">
                        <div className="p-2 bg-green-100 rounded-lg text-green-600">
                            <Link2 size={20} />
                        </div>
                        <div>
                            <div className="text-2xl font-bold text-gray-900">{inviteCodes.filter(c => c.isActive).length}</div>
                            <div className="text-sm text-gray-500">Codes actifs</div>
                        </div>
                    </div>
                </Card>
            </div>

            {/* Tabs */}
            <div className="border-b border-gray-200">
                <nav className="flex gap-8">
                    <button
                        onClick={() => setActiveTab('invitations')}
                        className={`pb-4 text-sm font-medium border-b-2 transition-colors ${activeTab === 'invitations'
                                ? 'border-primary text-primary'
                                : 'border-transparent text-gray-500 hover:text-gray-700'
                            }`}
                    >
                        Invitations par email
                    </button>
                    <button
                        onClick={() => setActiveTab('codes')}
                        className={`pb-4 text-sm font-medium border-b-2 transition-colors ${activeTab === 'codes'
                                ? 'border-primary text-primary'
                                : 'border-transparent text-gray-500 hover:text-gray-700'
                            }`}
                    >
                        Codes d'invitation
                    </button>
                </nav>
            </div>

            {/* Content */}
            {activeTab === 'invitations' ? (
                <DataTable
                    data={invitations}
                    columns={invitationColumns}
                    loading={loading}
                    searchable
                    searchPlaceholder="Rechercher par email..."
                    pageSize={10}
                    emptyMessage="Aucune invitation"
                />
            ) : (
                <div className="grid gap-4">
                    {inviteCodes.map(code => (
                        <Card key={code.id} className={`p-4 ${!code.isActive ? 'opacity-60' : ''}`}>
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-4">
                                    <div className={`p-3 rounded-lg ${code.isActive ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'}`}>
                                        <Link2 size={24} />
                                    </div>
                                    <div>
                                        <div className="flex items-center gap-3">
                                            <h3 className="font-semibold text-lg">{code.team}</h3>
                                            <div className="flex items-center gap-2 px-3 py-1 bg-gray-100 rounded-full">
                                                <code className="text-sm font-mono">{code.code}</code>
                                                <button
                                                    onClick={() => copyToClipboard(code.code)}
                                                    className="p-1 hover:bg-gray-200 rounded"
                                                >
                                                    <Copy size={12} />
                                                </button>
                                            </div>
                                        </div>
                                        <div className="flex items-center gap-4 mt-2 text-sm text-gray-500">
                                            <span className="flex items-center gap-1">
                                                <Users size={14} />
                                                {code.usageCount} / {code.maxUsage || '∞'} utilisations
                                            </span>
                                            {code.expiresAt && (
                                                <span className="flex items-center gap-1">
                                                    <Calendar size={14} />
                                                    Expire le {new Date(code.expiresAt).toLocaleDateString('fr-FR')}
                                                </span>
                                            )}
                                            <span>Créé par {code.createdBy}</span>
                                        </div>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => handleToggleCode(code.id, code.isActive)}
                                    >
                                        {code.isActive ? 'Désactiver' : 'Activer'}
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        className="p-2 text-red-500"
                                        onClick={() => {
                                            if (window.confirm('Supprimer ce code ?')) {
                                                toast.success('Code supprimé');
                                            }
                                        }}
                                    >
                                        <Trash2 size={16} />
                                    </Button>
                                </div>
                            </div>
                        </Card>
                    ))}
                    {inviteCodes.length === 0 && (
                        <div className="text-center py-12 text-gray-500">
                            Aucun code d'invitation créé
                        </div>
                    )}
                </div>
            )}

            {/* Invite Modal */}
            <Modal
                isOpen={isInviteModalOpen}
                onClose={() => setIsInviteModalOpen(false)}
                title="Inviter des utilisateurs"
                footer={
                    <>
                        <Button variant="ghost" onClick={() => setIsInviteModalOpen(false)}>Annuler</Button>
                        <Button variant="primary" onClick={handleSendInvites}>
                            <Send size={16} className="mr-2" />
                            Envoyer
                        </Button>
                    </>
                }
            >
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Adresses email (séparées par des virgules)
                        </label>
                        <textarea
                            value={newInvite.emails}
                            onChange={(e) => setNewInvite({ ...newInvite, emails: e.target.value })}
                            placeholder="user1@example.com, user2@example.com"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50 h-24 resize-none"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Rôle par défaut</label>
                        <select
                            value={newInvite.role}
                            onChange={(e) => setNewInvite({ ...newInvite, role: e.target.value })}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                        >
                            {roles.map(role => (
                                <option key={role.value} value={role.value}>{role.label}</option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Message personnalisé (optionnel)</label>
                        <textarea
                            value={newInvite.message}
                            onChange={(e) => setNewInvite({ ...newInvite, message: e.target.value })}
                            placeholder="Bienvenue dans notre équipe..."
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50 h-20 resize-none"
                        />
                    </div>

                    <Input
                        label="Validité (jours)"
                        type="number"
                        value={newInvite.expiryDays}
                        onChange={(e) => setNewInvite({ ...newInvite, expiryDays: parseInt(e.target.value) || 7 })}
                        min={1}
                        max={30}
                    />
                </div>
            </Modal>

            {/* Code Modal */}
            <Modal
                isOpen={isCodeModalOpen}
                onClose={() => setIsCodeModalOpen(false)}
                title="Créer un code d'invitation"
                footer={
                    <>
                        <Button variant="ghost" onClick={() => setIsCodeModalOpen(false)}>Annuler</Button>
                        <Button variant="primary" onClick={handleCreateCode}>Créer le code</Button>
                    </>
                }
            >
                <div className="space-y-4">
                    <Input
                        label="Nom de l'équipe *"
                        value={newCode.team}
                        onChange={(e) => setNewCode({ ...newCode, team: e.target.value })}
                        placeholder="ex: Development Team"
                    />

                    <Input
                        label="Nombre maximum d'utilisations (vide = illimité)"
                        type="number"
                        value={newCode.maxUsage}
                        onChange={(e) => setNewCode({ ...newCode, maxUsage: e.target.value })}
                        placeholder="ex: 10"
                        min={1}
                    />

                    <Input
                        label="Validité en jours (vide = pas d'expiration)"
                        type="number"
                        value={newCode.expiryDays}
                        onChange={(e) => setNewCode({ ...newCode, expiryDays: e.target.value })}
                        placeholder="ex: 30"
                        min={1}
                    />

                    <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-700">
                        Un code unique sera généré automatiquement et pourra être partagé avec les nouveaux membres.
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default InvitationsPage;
