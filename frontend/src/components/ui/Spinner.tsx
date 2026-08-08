import React from 'react';
import { clsx } from 'clsx';

interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  color?: 'teal' | 'white' | 'dark';
  className?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({
  size = 'md',
  color = 'teal',
  className,
}) => {
  const sizes = {
    sm: 'w-4 h-4 border-2',
    md: 'w-6 h-6 border-2',
    lg: 'w-10 h-10 border-3',
  };

  const colors = {
    teal: 'border-teal-500 border-t-transparent',
    white: 'border-white border-t-transparent',
    dark: 'border-brand-950 border-t-transparent',
  };

  return (
    <div
      className={clsx(
        'rounded-full animate-spin',
        sizes[size],
        colors[color],
        className
      )}
      role="status"
      aria-label="Loading"
    />
  );
};
