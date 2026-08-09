import React, { useState, useEffect } from 'react';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Skill, SkillCategory, SkillLevel } from '../../types';
import { getSkillCategories, searchSkills, addMySkill, createSkill } from '../../lib/apiClient';
import { X, AlertCircle, Plus } from 'lucide-react';
import { clsx } from 'clsx';

interface AddSkillModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

interface FlatCategory {
  id: string;
  name: string;
  parentId?: string;
}

const LEVELS: SkillLevel[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];
const selectCls = 'w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 placeholder-slateText-400 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500';
const labelCls  = 'block text-xs font-semibold text-slateText-700 uppercase tracking-wider mb-1.5';

function flattenCategoryTree(tree: SkillCategory[], prefix = ''): FlatCategory[] {
  let result: FlatCategory[] = [];
  for (const cat of tree) {
    const label = prefix ? `${prefix} › ${cat.name}` : cat.name;
    result.push({ id: cat.id, name: label, parentId: cat.parentId });
    if (cat.children && cat.children.length > 0) {
      result = result.concat(flattenCategoryTree(cat.children, label));
    }
  }
  return result;
}

export const AddSkillModal: React.FC<AddSkillModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const [flatCategories, setFlatCategories] = useState<FlatCategory[]>([]);
  const [skills, setSkills]                 = useState<Skill[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  const [selectedSkillId, setSelectedSkillId]       = useState('');
  
  // Custom skill creation state
  const [isCreatingCustom, setIsCreatingCustom] = useState(false);
  const [customSkillName, setCustomSkillName]   = useState('');
  const [customCategoryId, setCustomCategoryId] = useState('');

  const [level, setLevel]                     = useState<SkillLevel>('INTERMEDIATE');
  const [canTeach, setCanTeach]               = useState(true);
  const [wantToLearn, setWantToLearn]         = useState(false);
  const [yearsExperience, setYearsExperience] = useState(1);
  const [loading, setLoading]                 = useState(false);
  const [error, setError]                     = useState<string | null>(null);

  const loadData = async () => {
    try {
      const [catTree, skillList] = await Promise.all([getSkillCategories(), searchSkills()]);
      setFlatCategories(flattenCategoryTree(Array.isArray(catTree) ? catTree : []));
      setSkills(Array.isArray(skillList) ? skillList : []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (isOpen) {
      loadData();
      setIsCreatingCustom(false);
      setCustomSkillName('');
      setCustomCategoryId('');
      setSelectedCategoryId('');
      setSelectedSkillId('');
      setError(null);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!canTeach && !wantToLearn) {
      setError('Select at least one: Can Teach or Want to Learn');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      let targetSkillId = selectedSkillId;

      if (isCreatingCustom) {
        if (!customSkillName.trim()) { setError('Please enter a custom skill name'); setLoading(false); return; }
        if (!customCategoryId) { setError('Please select a category for the custom skill'); setLoading(false); return; }
        const created = await createSkill({
          name: customSkillName.trim(),
          categoryId: customCategoryId,
        });
        targetSkillId = created.id;
      }

      if (!targetSkillId) {
        setError('Please select or create a skill');
        setLoading(false);
        return;
      }

      await addMySkill({
        skillId: targetSkillId,
        level,
        canTeach,
        wantToLearn,
        yearsExperience: canTeach ? yearsExperience : undefined,
      });

      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to add skill');
    } finally {
      setLoading(false);
    }
  };

  // Filter skills by selected category (including subcategories)
  const childCategoryIds = new Set<string>();
  if (selectedCategoryId) {
    childCategoryIds.add(selectedCategoryId);
    flatCategories.forEach((c) => {
      if (c.parentId === selectedCategoryId) childCategoryIds.add(c.id);
    });
  }

  const filteredSkills = selectedCategoryId
    ? skills.filter((s) => childCategoryIds.has(s.categoryId))
    : skills;

  return (
    <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
      <div className="bg-white border border-surface-200 rounded-panel w-full max-w-lg shadow-xl overflow-hidden">
        {/* Header */}
        <div className="px-6 py-4 border-b border-surface-200 flex items-center justify-between">
          <h2 className="text-base font-semibold text-slateText-900">Add Skill to Profile</h2>
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

          {!isCreatingCustom ? (
            <>
              {/* Category filter */}
              <div>
                <label className={labelCls}>Filter by Category</label>
                <select
                  value={selectedCategoryId}
                  onChange={(e) => {
                    setSelectedCategoryId(e.target.value);
                    setSelectedSkillId('');
                  }}
                  className={selectCls}
                >
                  <option value="">All Categories ({flatCategories.length})</option>
                  {flatCategories.map((cat) => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>

              {/* Skill select */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slateText-700 uppercase tracking-wider">Select Skill *</label>
                  <button
                    type="button"
                    onClick={() => setIsCreatingCustom(true)}
                    className="text-xs text-teal-600 hover:text-teal-700 font-medium flex items-center gap-1"
                  >
                    <Plus className="w-3.5 h-3.5" /> Create Custom Skill
                  </button>
                </div>
                <select
                  value={selectedSkillId}
                  onChange={(e) => {
                    if (e.target.value === '__NEW__') {
                      setIsCreatingCustom(true);
                      setSelectedSkillId('');
                    } else {
                      setSelectedSkillId(e.target.value);
                    }
                  }}
                  className={selectCls}
                  required={!isCreatingCustom}
                >
                  <option value="">Choose a skill ({filteredSkills.length} available)…</option>
                  {filteredSkills.map((s) => (
                    <option key={s.id} value={s.id}>{s.name} ({s.categoryName})</option>
                  ))}
                  <option value="__NEW__">+ Create a new custom skill…</option>
                </select>
              </div>
            </>
          ) : (
            /* Custom Skill Creation Mode */
            <div className="p-4 bg-teal-50/50 border border-teal-200 rounded-btn space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-teal-700 uppercase tracking-wider">New Custom Skill</span>
                <button
                  type="button"
                  onClick={() => setIsCreatingCustom(false)}
                  className="text-xs text-slateText-500 hover:text-slateText-700 underline"
                >
                  Back to Skill Catalog
                </button>
              </div>

              <div>
                <label className={labelCls}>Skill Name *</label>
                <Input
                  placeholder="e.g. Next.js, Kubernetes, Graphic Design"
                  value={customSkillName}
                  onChange={(e) => setCustomSkillName(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className={labelCls}>Category *</label>
                <select
                  value={customCategoryId}
                  onChange={(e) => setCustomCategoryId(e.target.value)}
                  className={selectCls}
                  required
                >
                  <option value="">Select Category…</option>
                  {flatCategories.map((cat) => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>
            </div>
          )}

          {/* Level picker */}
          <div>
            <label className={labelCls}>Proficiency Level</label>
            <div className="grid grid-cols-4 gap-2">
              {LEVELS.map((lvl) => (
                <button
                  key={lvl}
                  type="button"
                  onClick={() => setLevel(lvl)}
                  className={clsx(
                    'py-2 px-1 rounded-btn text-xs font-semibold border transition-colors',
                    level === lvl
                      ? 'bg-teal-50 border-teal-400 text-teal-700'
                      : 'bg-surface-50 border-surface-200 text-slateText-500 hover:bg-surface-100'
                  )}
                >
                  {lvl.charAt(0) + lvl.slice(1).toLowerCase()}
                </button>
              ))}
            </div>
          </div>

          {/* Teach / Learn toggles */}
          <div className="space-y-3">
            <label className="flex items-start gap-3 cursor-pointer group">
              <input
                type="checkbox"
                checked={canTeach}
                onChange={(e) => setCanTeach(e.target.checked)}
                className="w-4 h-4 mt-0.5 rounded border-surface-300 text-teal-500 focus:ring-teal-500"
              />
              <div>
                <span className="text-sm font-medium text-slateText-900 group-hover:text-teal-600 transition-colors">I Can Teach This</span>
                <p className="text-xs text-slateText-400 mt-0.5">Offer to help peers learn this skill</p>
              </div>
            </label>
            <label className="flex items-start gap-3 cursor-pointer group">
              <input
                type="checkbox"
                checked={wantToLearn}
                onChange={(e) => setWantToLearn(e.target.checked)}
                className="w-4 h-4 mt-0.5 rounded border-surface-300 text-teal-500 focus:ring-teal-500"
              />
              <div>
                <span className="text-sm font-medium text-slateText-900 group-hover:text-teal-600 transition-colors">I Want to Learn This</span>
                <p className="text-xs text-slateText-400 mt-0.5">Get matched with peers who can teach it</p>
              </div>
            </label>
          </div>

          {/* Years of experience */}
          {canTeach && (
            <Input
              type="number"
              label="Years of Experience"
              min={0} max={50}
              value={yearsExperience}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setYearsExperience(parseInt(e.target.value) || 0)}
            />
          )}

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-2 border-t border-surface-200">
            <Button type="button" variant="ghost" onClick={onClose}>Cancel</Button>
            <Button type="submit" isLoading={loading}>
              {isCreatingCustom ? 'Create & Add Skill' : 'Save Skill'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
