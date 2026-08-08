import React from 'react';
import { LearningGoal } from '../../types';
import { Target, Calendar, Trash2, ArrowRight } from 'lucide-react';
import { SkillLevelBadge } from '../common/SkillLevelBadge';

interface GoalCardProps {
  goal: LearningGoal;
  onDelete?: (id: string) => void;
}

const statusColors: Record<string, string> = {
  IN_PROGRESS: 'bg-teal-50 text-teal-700 border border-teal-200',
  COMPLETED:   'bg-green-50 text-green-700 border border-green-200',
  PAUSED:      'bg-amber-50 text-amber-700 border border-amber-200',
};

export const GoalCard: React.FC<GoalCardProps> = ({ goal, onDelete }) => {
  const statusCls = statusColors[goal.status] ?? 'bg-surface-100 text-slateText-500 border border-surface-200';

  return (
    <div className="bg-white border border-surface-200 rounded-card p-5 shadow-sm hover:shadow-md transition-shadow flex flex-col justify-between">
      <div>
        {/* Header row */}
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-btn bg-teal-50 border border-teal-200 flex items-center justify-center shrink-0">
              <Target className="w-4 h-4 text-teal-600" />
            </div>
            <div>
              <h3 className="font-semibold text-slateText-900 text-sm leading-tight">{goal.targetSkillName}</h3>
              <span className={`inline-flex items-center mt-0.5 px-2 py-0.5 rounded text-[10px] font-semibold ${statusCls}`}>
                {goal.status.replace('_', ' ')}
              </span>
            </div>
          </div>
          {onDelete && (
            <button
              onClick={() => onDelete(goal.id)}
              className="text-slateText-300 hover:text-red-500 transition-colors p-1 -mr-1 -mt-1 rounded"
              title="Delete Goal"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Goal text */}
        {goal.goalText && (
          <p className="text-xs text-slateText-500 italic bg-surface-50 border border-surface-200 rounded-btn px-3 py-2 mb-3 leading-relaxed">
            "{goal.goalText}"
          </p>
        )}

        {/* Level progression */}
        {(goal.currentLevel || goal.targetLevel) && (
          <div className="flex items-center gap-2 mb-3">
            {goal.currentLevel && <SkillLevelBadge level={goal.currentLevel} />}
            <ArrowRight className="w-3.5 h-3.5 text-slateText-300" />
            {goal.targetLevel && <SkillLevelBadge level={goal.targetLevel} />}
          </div>
        )}

        {/* Learning preferences */}
        {goal.learningPreferences && (
          <p className="text-xs text-slateText-400 mb-3">
            <span className="font-medium text-slateText-600">Preferences: </span>
            {goal.learningPreferences}
          </p>
        )}
      </div>

      {/* Footer: Deadline */}
      {goal.deadline && (
        <div className="pt-3 border-t border-surface-100 flex items-center gap-1.5 text-xs text-teal-600 font-medium">
          <Calendar className="w-3.5 h-3.5" />
          <span>Deadline: {new Date(goal.deadline).toLocaleDateString()}</span>
        </div>
      )}
    </div>
  );
};
