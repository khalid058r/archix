import { Navigate } from 'react-router-dom';
import { useAppSelector } from '../../store/hooks';

interface OnboardingGuardProps {
    children: React.ReactNode;
}

/**
 * Redirects to /onboarding if user hasn't completed onboarding
 */
export const OnboardingGuard = ({ children }: OnboardingGuardProps) => {
    const user = useAppSelector((state) => state.auth.user);

    // If user exists and hasn't completed onboarding, redirect
    if (user && user.onboardingCompleted === false) {
        return <Navigate to="/onboarding" replace />;
    }

    return <>{children}</>;
};
