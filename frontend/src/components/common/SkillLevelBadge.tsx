import React from 'react';
import { SkillLevel } from '../../types';
import { clsx } from 'clsx';

interface SkillLevelBadgeProps {
  level: SkillLevel;
  className?: string;
}

export const SkillLevelBadge: React.FC<SkillLevelBadgeProps> = ({ level, className }) => {
  const styles: Record<SkillLevel, string> = {
    BEGINNER:     'bg-green-50 text-green-700 border border-green-200',
    INTERMEDIATE: 'bg-teal-50 text-teal-700 border border-teal-200',
    ADVANCED:     'bg-blue-50 text-blue-700 border border-blue-200',
    EXPERT:       'bg-violet-50 text-violet-700 border border-violet-200',
  };

  const labels: Record<SkillLevel, string> = {
    BEGINNER:     'Beginner',
    INTERMEDIATE: 'Intermediate',
    ADVANCED:     'Advanced',
    EXPERT:       'Expert',
  };

  return (
    <span
      className={clsx(
        'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border',
        styles[level] || styles.BEGINNER,
        className
      )}
    >
      {labels[level] || level}
    </span>
  );
};
