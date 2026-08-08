import React from 'react';
import { clsx } from 'clsx';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'teal' | 'navy' | 'gray' | 'success' | 'warning' | 'error' | 'ai' | 'info';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'teal',
  className,
}) => {
  const baseStyles = 'inline-flex items-center px-2.5 py-0.5 rounded text-xs font-semibold';

  const variants = {
    teal:    'bg-teal-50 text-teal-700 border border-teal-200',
    navy:    'bg-brand-950 text-white',
    gray:    'bg-surface-100 text-slateText-500 border border-surface-200',
    success: 'bg-green-50 text-green-700 border border-green-200',
    warning: 'bg-amber-50 text-amber-700 border border-amber-200',
    error:   'bg-red-50 text-red-600 border border-red-200',
    info:    'bg-blue-50 text-blue-700 border border-blue-200',
    ai:      'bg-gradient-to-r from-teal-50 to-blue-50 text-teal-700 border border-teal-200',
  };

  return (
    <span className={clsx(baseStyles, variants[variant], className)}>
      {children}
    </span>
  );
};
