import React, { useState, useEffect } from 'react';
import { getMySkills, removeMySkill } from '../../../lib/apiClient';
import { UserSkill } from '../../../types';
import { Button } from '../../../components/ui/Button';
import { Card } from '../../../components/ui/Card';
import { SkillLevelBadge } from '../../../components/common/SkillLevelBadge';
import { AddSkillModal } from '../../../components/skills/AddSkillModal';
import { Award, Plus, Trash2, BookOpen, GraduationCap } from 'lucide-react';

export const SkillsPage: React.FC = () => {
  const [skills, setSkills] = useState<UserSkill[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchSkills = () => {
    setLoading(true);
    getMySkills().then(setSkills).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { fetchSkills(); }, []);

  const handleRemove = async (skillId: string) => {
    try { await removeMySkill(skillId); fetchSkills(); } catch (err) { console.error(err); }
  };

  const teachingSkills = skills.filter((s) => s.canTeach);
  const learningSkills = skills.filter((s) => s.wantToLearn);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
            <Award className="w-6 h-6 text-teal-500" />
            My Skill Profile
          </h1>
          <p className="text-sm text-slateText-500 mt-1">
            Manage skills you can teach and skills you want to learn from peers.
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)}>
          <Plus className="w-4 h-4 mr-1.5" />
          Add Skill
        </Button>
      </div>

      {loading ? (
        <div className="py-12 text-center text-sm text-slateText-400">Loading skills…</div>
      ) : (
        <div className="space-y-8">
          {/* Can Teach */}
          <section>
            <div className="flex items-center gap-2 mb-4">
              <GraduationCap className="w-4 h-4 text-teal-500" />
              <h2 className="text-sm font-semibold text-slateText-900">Skills I Can Teach</h2>
              <span className="text-xs bg-teal-50 text-teal-600 border border-teal-200 px-1.5 py-0.5 rounded font-mono">
                {teachingSkills.length}
              </span>
            </div>

            {teachingSkills.length === 0 ? (
              <Card className="p-8 text-center">
                <GraduationCap className="w-8 h-8 text-slateText-300 mx-auto mb-2" />
                <p className="text-sm text-slateText-500">No teachable skills listed yet.</p>
                <p className="text-xs text-slateText-400 mt-1">Click "Add Skill" to offer your expertise to peers.</p>
              </Card>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {teachingSkills.map((s) => (
                  <Card key={s.id} className="p-5">
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <h3 className="font-semibold text-slateText-900 text-sm">{s.skillName}</h3>
                        <p className="text-xs text-slateText-400 mt-0.5">{s.categoryName}</p>
                      </div>
                      <button
                        onClick={() => handleRemove(s.skillId)}
                        className="text-slateText-300 hover:text-red-500 transition-colors p-1 -mr-1 -mt-1 rounded"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                    <div className="flex items-center gap-2 pt-3 border-t border-surface-100">
                      <SkillLevelBadge level={s.level} />
                      {s.yearsExperience && (
                        <span className="text-xs text-slateText-400 font-mono">
                          {s.yearsExperience} yr{s.yearsExperience > 1 ? 's' : ''} exp
                        </span>
                      )}
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </section>

          {/* Want to Learn */}
          <section>
            <div className="flex items-center gap-2 mb-4">
              <BookOpen className="w-4 h-4 text-aiBlue" />
              <h2 className="text-sm font-semibold text-slateText-900">Skills I Want to Learn</h2>
              <span className="text-xs bg-blue-50 text-aiBlue border border-blue-200 px-1.5 py-0.5 rounded font-mono">
                {learningSkills.length}
              </span>
            </div>

            {learningSkills.length === 0 ? (
              <Card className="p-8 text-center">
                <BookOpen className="w-8 h-8 text-slateText-300 mx-auto mb-2" />
                <p className="text-sm text-slateText-500">No learning targets on your profile.</p>
                <p className="text-xs text-slateText-400 mt-1">Add skills you want to learn to get matched with teachers.</p>
              </Card>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {learningSkills.map((s) => (
                  <Card key={s.id} className="p-5">
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <h3 className="font-semibold text-slateText-900 text-sm">{s.skillName}</h3>
                        <p className="text-xs text-slateText-400 mt-0.5">{s.categoryName}</p>
                      </div>
                      <button
                        onClick={() => handleRemove(s.skillId)}
                        className="text-slateText-300 hover:text-red-500 transition-colors p-1 -mr-1 -mt-1 rounded"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                    <div className="flex items-center gap-2 pt-3 border-t border-surface-100">
                      <span className="text-xs text-slateText-400">Current level:</span>
                      <SkillLevelBadge level={s.level} />
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </section>
        </div>
      )}

      <AddSkillModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={fetchSkills} />
    </div>
  );
};
