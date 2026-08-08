import React, { useState, useEffect } from 'react';
import { Button } from '../ui/Button';
import { UserSkill, PublicUserProfile } from '../../types';
import { getMySkills, sendExchangeRequest } from '../../lib/apiClient';
import { X, ArrowLeftRight, AlertCircle } from 'lucide-react';

interface ExchangeRequestModalProps {
  isOpen: boolean;
  onClose: () => void;
  targetUser: PublicUserProfile | null;
  onSuccess: () => void;
}

const selectCls = 'w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500';
const labelCls  = 'block text-xs font-semibold text-slateText-700 uppercase tracking-wider mb-1.5';

export const ExchangeRequestModal: React.FC<ExchangeRequestModalProps> = ({
  isOpen,
  onClose,
  targetUser,
  onSuccess,
}) => {
  const [mySkills, setMySkills] = useState<UserSkill[]>([]);
  const [selectedOfferedSkillId, setSelectedOfferedSkillId] = useState<string>('');
  const [selectedWantedSkillId, setSelectedWantedSkillId] = useState<string>('');
  const [message, setMessage] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      getMySkills().then(setMySkills).catch(console.error);
    }
  }, [isOpen]);

  if (!isOpen || !targetUser) return null;

  const teachableSkills = mySkills.filter((s) => s.canTeach);
  const targetTeachableSkills = targetUser.skillsTeaching;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedOfferedSkillId || !selectedWantedSkillId) {
      setError('Please select both a skill you can teach and a skill you want to learn');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await sendExchangeRequest({
        receiverId: targetUser.user.id,
        offeredSkillId: selectedOfferedSkillId,
        wantedSkillId: selectedWantedSkillId,
        message,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to send exchange request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
      <div className="bg-white border border-surface-200 rounded-panel w-full max-w-lg shadow-xl overflow-hidden">
        {/* Header */}
        <div className="px-6 py-4 border-b border-surface-200 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <ArrowLeftRight className="w-4 h-4 text-teal-500" />
            <h2 className="text-base font-semibold text-slateText-900">
              Request Skill Exchange with {targetUser.user.firstName}
            </h2>
          </div>
          <button onClick={onClose} className="text-slateText-400 hover:text-slateText-700 transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {error && (
            <div className="flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-input text-red-600 text-sm">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {/* Skill You Offer */}
          <div>
            <label className={labelCls}>
              Skill You Offer to Teach *
            </label>
            <select
              value={selectedOfferedSkillId}
              onChange={(e) => setSelectedOfferedSkillId(e.target.value)}
              className={selectCls}
              required
            >
              <option value="">Select one of your teachable skills...</option>
              {teachableSkills.map((s) => (
                <option key={s.skillId} value={s.skillId}>
                  {s.skillName} ({s.level})
                </option>
              ))}
            </select>
            {teachableSkills.length === 0 && (
              <p className="text-xs text-amber-600 mt-1.5">
                You haven't marked any skills as "Can Teach" on your profile yet.
              </p>
            )}
          </div>

          {/* Skill You Want */}
          <div>
            <label className={labelCls}>
              Skill You Want to Learn from {targetUser.user.firstName} *
            </label>
            <select
              value={selectedWantedSkillId}
              onChange={(e) => setSelectedWantedSkillId(e.target.value)}
              className={selectCls}
              required
            >
              <option value="">Select a skill they offer...</option>
              {targetTeachableSkills.map((s) => (
                <option key={s.skillId} value={s.skillId}>
                  {s.skillName} ({s.level})
                </option>
              ))}
            </select>
          </div>

          {/* Message */}
          <div>
            <label className={labelCls}>Message (Optional)</label>
            <textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              rows={3}
              placeholder="Hi! I saw you teach Java. I can help you with UI/UX Design..."
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 placeholder-slateText-400 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500 resize-none"
            />
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-2 border-t border-surface-200">
            <Button type="button" variant="ghost" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" isLoading={loading} disabled={teachableSkills.length === 0}>
              Send Request
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
