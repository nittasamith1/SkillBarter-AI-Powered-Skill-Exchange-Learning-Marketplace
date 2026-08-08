import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getPublicProfile } from '../../../lib/apiClient';
import { PublicUserProfile } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { Badge } from '../../../components/ui/Badge';
import { SkillLevelBadge } from '../../../components/common/SkillLevelBadge';
import { ExchangeRequestModal } from '../../../components/marketplace/ExchangeRequestModal';
import { MapPin, Mail, GraduationCap, BookOpen, Target, ArrowLeftRight, CheckCircle2, Building2 } from 'lucide-react';

export const UserProfilePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [profile, setProfile] = useState<PublicUserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [requestSent, setRequestSent] = useState(false);

  useEffect(() => {
    if (id) {
      setLoading(true);
      getPublicProfile(id).then(setProfile).catch(console.error).finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) {
    return <div className="py-12 text-center text-sm text-slateText-400">Loading profile…</div>;
  }

  if (!profile) {
    return (
      <Card className="p-10 text-center">
        <p className="text-sm text-slateText-500">User profile not found or does not belong to your institution.</p>
      </Card>
    );
  }

  const { user, skillsTeaching, skillsLearning, learningGoals } = profile;

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* ─── Profile Banner ─── */}
      <Card className="p-6 md:p-8">
        <div className="flex flex-col sm:flex-row items-start gap-6">
          {/* Avatar */}
          <div className="w-20 h-20 rounded-2xl bg-teal-500 text-white text-2xl font-bold flex items-center justify-center shrink-0 shadow-sm">
            {user.firstName[0]}{user.lastName[0]}
          </div>

          {/* Info */}
          <div className="flex-1 min-w-0">
            <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
              <div>
                <h1 className="text-xl font-bold text-slateText-900 tracking-tight">
                  {user.firstName} {user.lastName}
                </h1>
                <div className="flex flex-wrap items-center gap-3 mt-2 text-xs text-slateText-500">
                  <span className="flex items-center gap-1">
                    <Mail className="w-3.5 h-3.5 text-slateText-300" />
                    {user.email}
                  </span>
                  {user.location && (
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-slateText-300" />
                      {user.location}
                    </span>
                  )}
                  <span className="flex items-center gap-1">
                    <Building2 className="w-3.5 h-3.5 text-slateText-300" />
                    {user.tenant.name}
                  </span>
                </div>
                <div className="flex items-center gap-2 mt-3">
                  <Badge variant="teal">
                    {skillsTeaching.length} Teaching
                  </Badge>
                  <Badge variant="info">
                    {skillsLearning.length} Learning
                  </Badge>
                </div>
                {user.bio && (
                  <p className="text-sm text-slateText-600 mt-3 leading-relaxed max-w-lg">{user.bio}</p>
                )}
              </div>

              <Button
                onClick={() => setIsModalOpen(true)}
                disabled={skillsTeaching.length === 0 || requestSent}
                variant={requestSent ? 'secondary' : 'primary'}
                className="shrink-0"
              >
                {requestSent ? (
                  <><CheckCircle2 className="w-4 h-4 mr-1.5 text-green-500" /> Sent</>
                ) : (
                  <><ArrowLeftRight className="w-4 h-4 mr-1.5" /> Request Exchange</>
                )}
              </Button>
            </div>
          </div>
        </div>
      </Card>

      {/* ─── Skills Grid ─── */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Can Teach */}
        <Card className="p-5">
          <h2 className="text-sm font-semibold text-slateText-900 flex items-center gap-2 mb-4">
            <GraduationCap className="w-4 h-4 text-teal-500" />
            Skills {user.firstName} Can Teach
          </h2>
          {skillsTeaching.length === 0 ? (
            <p className="text-xs text-slateText-400 py-2">No teachable skills listed yet.</p>
          ) : (
            <div className="space-y-2">
              {skillsTeaching.map((s) => (
                <div key={s.id} className="flex items-center justify-between py-2.5 px-3 bg-surface-50 border border-surface-200 rounded-btn">
                  <div>
                    <p className="text-sm font-medium text-slateText-900">{s.skillName}</p>
                    <p className="text-xs text-slateText-400">{s.categoryName}</p>
                  </div>
                  <SkillLevelBadge level={s.level} />
                </div>
              ))}
            </div>
          )}
        </Card>

        {/* Want to Learn */}
        <Card className="p-5">
          <h2 className="text-sm font-semibold text-slateText-900 flex items-center gap-2 mb-4">
            <BookOpen className="w-4 h-4 text-aiBlue" />
            Skills {user.firstName} Wants to Learn
          </h2>
          {skillsLearning.length === 0 ? (
            <p className="text-xs text-slateText-400 py-2">No learning targets listed yet.</p>
          ) : (
            <div className="space-y-2">
              {skillsLearning.map((s) => (
                <div key={s.id} className="flex items-center justify-between py-2.5 px-3 bg-surface-50 border border-surface-200 rounded-btn">
                  <div>
                    <p className="text-sm font-medium text-slateText-900">{s.skillName}</p>
                    <p className="text-xs text-slateText-400">{s.categoryName}</p>
                  </div>
                  <SkillLevelBadge level={s.level} />
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      {/* ─── Learning Goals ─── */}
      {learningGoals.length > 0 && (
        <Card className="p-5">
          <h2 className="text-sm font-semibold text-slateText-900 flex items-center gap-2 mb-4">
            <Target className="w-4 h-4 text-teal-500" />
            Learning Goals
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {learningGoals.map((g) => (
              <div key={g.id} className="p-4 bg-surface-50 border border-surface-200 rounded-card">
                <p className="text-sm font-semibold text-slateText-900">{g.targetSkillName}</p>
                {g.goalText && <p className="text-xs text-slateText-500 italic mt-1">"{g.goalText}"</p>}
                {g.deadline && (
                  <p className="text-xs text-teal-600 font-medium mt-2">
                    Deadline: {new Date(g.deadline).toLocaleDateString()}
                  </p>
                )}
              </div>
            ))}
          </div>
        </Card>
      )}

      <ExchangeRequestModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        targetUser={profile}
        onSuccess={() => setRequestSent(true)}
      />
    </div>
  );
};
