import { Construction } from 'lucide-react';
import { Button } from '../components/ui';
import { useNavigate } from 'react-router-dom';

interface WorkInProgressProps {
    title: string;
}

export function WorkInProgress({ title }: WorkInProgressProps) {
    const navigate = useNavigate();

    return (
        <div className="empty-state">
            <div className="empty-state-icon">
                <Construction size={32} />
            </div>
            <h2 className="empty-state-title">{title}</h2>
            <p className="empty-state-description">
                Cette fonctionnalité est en cours de développement.
                Elle sera disponible très prochainement.
            </p>
            <Button variant="outline" onClick={() => navigate('/')}>
                Retour au tableau de bord
            </Button>
        </div>
    );
}

export default WorkInProgress;
