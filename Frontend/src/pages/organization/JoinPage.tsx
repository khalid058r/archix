import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CheckCircle, XCircle, ArrowRight, Loader } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { organizationService } from '../../services/organization.service';
import toast from 'react-hot-toast';

export const JoinPage = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const navigate = useNavigate();

    const [status, setStatus] = useState<'IDLE' | 'PROCESSING' | 'SUCCESS' | 'ERROR'>('IDLE');
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        if (!token) {
            setStatus('ERROR');
            setErrorMessage('Lien d\'invitation invalide (Token manquant).');
        }
    }, [token]);

    const handleJoin = async () => {
        if (!token) return;

        try {
            setStatus('PROCESSING');
            await organizationService.joinOrganization(token);
            setStatus('SUCCESS');
            toast.success('Vous avez rejoint l\'organisation !');

            // Wait a moment then redirect
            setTimeout(() => {
                // Must force reload to refresh user permission/org list
                window.location.href = '/dashboard';
            }, 1500);

        } catch (error: any) {
            setStatus('ERROR');
            setErrorMessage(error.response?.data?.message || 'Impossible de rejoindre l\'organisation. L\'invitation est peut-être expirée.');
            console.error(error);
        }
    };

    return (
        <div className="min-h-screen bg-black text-white flex items-center justify-center p-4">
            <Card className="max-w-md w-full p-8 text-center space-y-6">

                {status === 'IDLE' && (
                    <>
                        <h1 className="text-2xl font-bold">Invitation Reçue</h1>
                        <p className="text-gray-400">
                            Vous avez été invité à rejoindre une organisation.
                        </p>
                        <Button onClick={handleJoin} className="w-full">
                            Accepter l'invitation <ArrowRight className="ml-2 w-4 h-4" />
                        </Button>
                    </>
                )}

                {status === 'PROCESSING' && (
                    <div className="flex flex-col items-center py-8">
                        <Loader className="w-12 h-12 animate-spin text-primary mb-4" />
                        <p>Traitement en cours...</p>
                    </div>
                )}

                {status === 'SUCCESS' && (
                    <div className="flex flex-col items-center py-8">
                        <CheckCircle className="w-16 h-16 text-green-500 mb-4" />
                        <h2 className="text-xl font-bold text-green-500">Félicitations !</h2>
                        <p className="text-gray-400 mt-2">Vous êtes maintenant membre.</p>
                        <p className="text-sm text-gray-500 mt-4">Redirection vers le tableau de bord...</p>
                    </div>
                )}

                {status === 'ERROR' && (
                    <div className="flex flex-col items-center py-8">
                        <XCircle className="w-16 h-16 text-red-500 mb-4" />
                        <h2 className="text-xl font-bold text-red-500">Erreur</h2>
                        <p className="text-gray-400 mt-2">{errorMessage}</p>
                        <Button
                            variant="outline"
                            className="mt-6"
                            onClick={() => navigate('/dashboard')}
                        >
                            Retour au Dashboard
                        </Button>
                    </div>
                )}
            </Card>
        </div>
    );
};

export default JoinPage;
