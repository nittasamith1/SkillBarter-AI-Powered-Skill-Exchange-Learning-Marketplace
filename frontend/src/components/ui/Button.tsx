import React from 'react';
import { clsx } from 'clsx';
import { Spinner } from './Spinner';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  className,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled,
  leftIcon,
  rightIcon,
  ...props
}) => {
  const baseStyles =
    'inline-flex items-center justify-center font-medium rounded-btn transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-teal-500/40 disabled:opacity-50 disabled:cursor-not-allowed active:scale-[0.98] select-none';

  const variants = {
    primary:
      'bg-teal-500 hover:bg-teal-600 active:bg-teal-700 text-white font-semibold shadow-sm',
    secondary:
      'bg-surface-100 hover:bg-surface-200 text-slateText-900 border border-surface-200',
    outline:
      'border border-surface-200 bg-white hover:bg-surface-50 text-slateText-700 hover:border-surface-300',
    ghost:
      'text-slateText-500 hover:bg-surface-100 hover:text-slateText-900',
    danger:
      'bg-red-50 hover:bg-red-100 text-red-600 border border-red-200',
  };

  const sizes = {
    sm: 'px-3 py-1.5 text-xs gap-1.5',
    md: 'px-4 py-2 text-sm gap-2',
    lg: 'px-5 py-2.5 text-sm gap-2',
  };

  return (
    <button
      className={clsx(baseStyles, variants[variant], sizes[size], className)}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? <Spinner size="sm" color={variant === 'primary' ? 'white' : 'teal'} /> : leftIcon}
      <span>{children}</span>
      {!isLoading && rightIcon}
    </button>
  );
};
