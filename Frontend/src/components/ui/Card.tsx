import React from 'react';
import './Card.css';

interface CardProps {
    children: React.ReactNode;
    className?: string;
    padding?: 'none' | 'sm' | 'md' | 'lg';
    hover?: boolean;
    onClick?: () => void;
}

export function Card({
    children,
    className = '',
    padding = 'md',
    hover = false,
    onClick,
}: CardProps) {
    const classNames = [
        'card',
        `card-padding-${padding}`,
        hover && 'card-hover',
        onClick && 'card-clickable',
        className,
    ]
        .filter(Boolean)
        .join(' ');

    return (
        <div
            className={classNames}
            onClick={onClick}
            role={onClick ? 'button' : undefined}
            tabIndex={onClick ? 0 : undefined}
            onKeyDown={onClick ? (e) => e.key === 'Enter' && onClick() : undefined}
        >
            {children}
        </div>
    );
}

interface CardHeaderProps {
    children: React.ReactNode;
    className?: string;
    action?: React.ReactNode;
}

export function CardHeader({ children, className = '', action }: CardHeaderProps) {
    return (
        <div className={`card-header ${className}`}>
            <div className="card-header-content">{children}</div>
            {action && <div className="card-header-action">{action}</div>}
        </div>
    );
}

interface CardTitleProps {
    children: React.ReactNode;
    className?: string;
    as?: 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6';
}

export function CardTitle({ children, className = '', as: Tag = 'h3' }: CardTitleProps) {
    return <Tag className={`card-title ${className}`}>{children}</Tag>;
}

interface CardDescriptionProps {
    children: React.ReactNode;
    className?: string;
}

export function CardDescription({ children, className = '' }: CardDescriptionProps) {
    return <p className={`card-description ${className}`}>{children}</p>;
}

interface CardContentProps {
    children: React.ReactNode;
    className?: string;
}

export function CardContent({ children, className = '' }: CardContentProps) {
    return <div className={`card-content ${className}`}>{children}</div>;
}

interface CardFooterProps {
    children: React.ReactNode;
    className?: string;
}

export function CardFooter({ children, className = '' }: CardFooterProps) {
    return <div className={`card-footer ${className}`}>{children}</div>;
}

export default Card;
