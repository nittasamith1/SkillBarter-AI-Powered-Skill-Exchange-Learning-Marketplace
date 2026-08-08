import React, { forwardRef } from 'react';
import { clsx } from 'clsx';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, className, id, ...props }, ref) => {
    const inputId = id || props.name;

    return (
      <div className="w-full space-y-1.5">
        {label && (
          <label htmlFor={inputId} className="block text-xs font-semibold text-slateText-700 uppercase tracking-wider">
            {label}
          </label>
        )}
        <input
          id={inputId}
          ref={ref}
          className={clsx(
            'w-full px-3.5 py-2.5 text-sm bg-white border rounded-input text-slateText-900 placeholder-slateText-400 transition-colors focus:outline-none focus:ring-1 focus:border-teal-500 focus:ring-teal-500 disabled:bg-surface-50 disabled:opacity-60 disabled:cursor-not-allowed',
            error ? 'border-red-400 focus:ring-red-400 focus:border-red-400' : 'border-surface-200',
            className
          )}
          {...props}
        />
        {error ? (
          <p className="text-xs text-red-500 mt-1">{error}</p>
        ) : helperText ? (
          <p className="text-xs text-slateText-400 mt-1">{helperText}</p>
        ) : null}
      </div>
    );
  }
);

Input.displayName = 'Input';
