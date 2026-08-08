import React from 'react';
import { clsx } from 'clsx';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'subtle' | 'ai';
}

export const Card: React.FC<CardProps> = ({
  children,
  className,
  variant = 'default',
  ...props
}) => {
  const baseStyles = 'rounded-card transition-all duration-150';

  const variants = {
    default: 'bg-white border border-surface-200 shadow-sm hover:shadow-md',
    subtle:  'bg-surface-50 border border-surface-200',
    ai:      'bg-gradient-to-r from-teal-50/80 via-white to-blue-50/80 border border-teal-200 shadow-sm',
  };

  return (
    <div className={clsx(baseStyles, variants[variant], className)} {...props}>
      {children}
    </div>
  );
};
