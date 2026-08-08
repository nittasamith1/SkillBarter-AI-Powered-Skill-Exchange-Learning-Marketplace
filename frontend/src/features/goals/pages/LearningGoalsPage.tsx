import React, { useState, useEffect } from 'react';
import { getMyLearningGoals, createLearningGoal, deleteLearningGoal, searchSkills } from '../../../lib/apiClient';
import { LearningGoal, Skill, SkillLevel } from '../../../types';
import { Button } from '../../../components/ui/Button';
import { Card } from '../../../components/ui/Card';
import { GoalCard } from '../../../components/goals/GoalCard';
import { Target, Plus, X, AlertCircle } from 'lucide-react';

const LEVELS: SkillLevel[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

const selectCls = 'w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500';
const labelCls = 'block text-xs font-semibold text-slateText-700 uppercase tracking-wider mb-1.5';

export const LearningGoalsPage: React.FC = () => {
  const [goals, setGoals] = useState<LearningGoal[]>([]);
  const [skills, setSkills] = useState<Skill[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [targetSkillId, setTargetSkillId] = useState('');
  const [goalText, setGoalText] = useState('');
  const [currentLevel, setCurrentLevel] = useState<SkillLevel>('BEGINNER');
  const [targetLevel, setTargetLevel] = useState<SkillLevel>('INTERMEDIATE');
  const [deadline, setDeadline] = useState('');
  const [learningPreferences, setLearningPreferences] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchGoals = () => {
    setLoading(true);
    getMyLearningGoals().then(setGoals).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchGoals();
    searchSkills().then(setSkills).catch(console.error);
  }, []);

  const handleCreateGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetSkillId) { setError('Please select a target skill'); return; }
    try {
      setSubmitting(true); setError(null);
      await createLearningGoal({ targetSkillId, goalText, currentLevel, targetLevel, deadline: deadline || undefined, learningPreferences: learningPreferences || undefined });
      setIsModalOpen(false); setGoalText(''); setTargetSkillId(''); fetchGoals();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create goal');
    } finally { setSubmitting(false); }
  };

  const handleDeleteGoal = async (id: string) => {
    try { await deleteLearningGoal(id); fetchGoals(); } catch (err) { console.error(err); }
  };

  return (
    <div className="space-y-7">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
            <Target className="w-6 h-6 text-teal-500" />
            My Learning Goals
          </h1>
          <p className="text-sm text-slateText-500 mt-1">
            Define your objectives, target levels, and deadlines to stay on track.
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)}>
          <Plus className="w-4 h-4 mr-1.5" /> Create Goal
        </Button>
      </div>

      {/* Goals Grid */}
      {loading ? (
        <div className="py-12 text-center text-sm text-slateText-400">Loading goals…</div>
      ) : goals.length === 0 ? (
        <Card className="p-10 text-center">
          <Target className="w-10 h-10 text-slateText-200 mx-auto mb-3" />
          <p className="text-sm font-medium text-slateText-600">No learning goals yet</p>
          <p className="text-xs text-slateText-400 mt-1 mb-4">Set your first milestone to unlock peer recommendations.</p>
          <Button size="sm" onClick={() => setIsModalOpen(true)}>
            <Plus className="w-3.5 h-3.5 mr-1.5" /> Create your first goal
          </Button>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {goals.map((g) => <GoalCard key={g.id} goal={g} onDelete={handleDeleteGoal} />)}
        </div>
      )}

      {/* Create Goal Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white border border-surface-200 rounded-panel w-full max-w-lg shadow-xl overflow-hidden">
            {/* Modal Header */}
            <div className="px-6 py-4 border-b border-surface-200 flex items-center justify-between">
              <h2 className="text-base font-semibold text-slateText-900">Create Learning Goal</h2>
              <button onClick={() => setIsModalOpen(false)} className="text-slateText-400 hover:text-slateText-700">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateGoal} className="p-6 space-y-4">
              {error && (
                <div className="flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-input text-red-600 text-sm">
                  <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                  <span>{error}</span>
                </div>
              )}

              <div>
                <label className={labelCls}>Target Skill *</label>
                <select value={targetSkillId} onChange={(e) => setTargetSkillId(e.target.value)} className={selectCls} required>
                  <option value="">Choose a skill…</option>
                  {skills.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.categoryName})</option>)}
                </select>
              </div>

              <div>
                <label className={labelCls}>Goal Statement</label>
                <input
                  type="text"
                  placeholder="e.g. I want to become a Java backend developer."
                  value={goalText}
                  onChange={(e) => setGoalText(e.target.value)}
                  className="w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 placeholder-slateText-400 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className={labelCls}>Current Level</label>
                  <select value={currentLevel} onChange={(e) => setCurrentLevel(e.target.value as SkillLevel)} className={selectCls}>
                    {LEVELS.map((l) => <option key={l} value={l}>{l}</option>)}
                  </select>
                </div>
                <div>
                  <label className={labelCls}>Target Level</label>
                  <select value={targetLevel} onChange={(e) => setTargetLevel(e.target.value as SkillLevel)} className={selectCls}>
                    {LEVELS.map((l) => <option key={l} value={l}>{l}</option>)}
                  </select>
                </div>
              </div>

              <div>
                <label className={labelCls}>Deadline</label>
                <input
                  type="date"
                  value={deadline}
                  onChange={(e) => setDeadline(e.target.value)}
                  className="w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500"
                />
              </div>

              <div>
                <label className={labelCls}>Learning Preferences</label>
                <textarea
                  placeholder="e.g. 1-on-1 pair programming, weekend sessions"
                  value={learningPreferences}
                  onChange={(e) => setLearningPreferences(e.target.value)}
                  rows={2}
                  className="w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 placeholder-slateText-400 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500 resize-none"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2 border-t border-surface-200">
                <Button type="button" variant="ghost" onClick={() => setIsModalOpen(false)}>Cancel</Button>
                <Button type="submit" isLoading={submitting}>Save Goal</Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
