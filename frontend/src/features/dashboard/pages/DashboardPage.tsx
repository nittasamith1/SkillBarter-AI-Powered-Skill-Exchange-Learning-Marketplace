import React, { useState, useEffect } from 'react';
import { useAuth } from '../../../lib/auth';
import { getDashboardData, respondToExchangeRequest } from '../../../lib/apiClient';
import { DashboardSummary } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { Badge } from '../../../components/ui/Badge';
import { SkillLevelBadge } from '../../../components/common/SkillLevelBadge';
import {
  Sparkles, Award, Target, ArrowLeftRight, Plus, Compass,
  CheckCircle, XCircle, UserCheck, TrendingUp, Clock,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchDashboard = () => {
    getDashboardData().then(setData).catch(console.error).finally(() => setLoading(false));
  };

  useEffect(() => { fetchDashboard(); }, []);

  const handleRespond = async (requestId: string, status: 'ACCEPTED' | 'REJECTED') => {
    try {
      await respondToExchangeRequest(requestId, status);
      fetchDashboard();
    } catch (err) { console.error(err); }
  };

  const teachingSkills = data?.mySkills.filter((s) => s.canTeach) ?? [];
  const learningSkills = data?.mySkills.filter((s) => s.wantToLearn) ?? [];

  const getHour = () => new Date().getHours();
  const greeting = getHour() < 12 ? 'Good morning' : getHour() < 17 ? 'Good afternoon' : 'Good evening';

  return (
    <div className="space-y-7">

      {/* ─── Welcome Header ─── */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slateText-900 tracking-tight">
            {greeting}, {user?.firstName} 👋
          </h1>
          <p className="text-sm text-slateText-500 mt-0.5">
            Continue building your skills under{' '}
            <span className="text-teal-600 font-medium">{user?.tenant?.name}</span>
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <Button variant="outline" size="sm" onClick={() => navigate('/skills/explore')}>
            <Compass className="w-3.5 h-3.5 mr-1.5" />
            Explore
          </Button>
          <Button variant="primary" size="sm" onClick={() => navigate('/skills')}>
            <Plus className="w-3.5 h-3.5 mr-1.5" />
            Add Skill
          </Button>
        </div>
      </div>

      {/* ─── Stat Cards ─── */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[
          {
            label: 'Skills Listed',
            value: data?.mySkills.length ?? 0,
            icon: Award,
            color: 'text-teal-600',
            bg: 'bg-teal-50',
          },
          {
            label: 'Pending Exchanges',
            value: data?.pendingRequests.length ?? 0,
            icon: ArrowLeftRight,
            color: 'text-aiBlue',
            bg: 'bg-blue-50',
          },
          {
            label: 'Learning Goals',
            value: data?.myLearningGoals.length ?? 0,
            icon: Target,
            color: 'text-violet-600',
            bg: 'bg-violet-50',
          },
        ].map((stat) => {
          const Icon = stat.icon;
          return (
            <Card key={stat.label} className="p-5 flex items-center gap-4">
              <div className={`w-11 h-11 rounded-btn ${stat.bg} flex items-center justify-center shrink-0`}>
                <Icon className={`w-5 h-5 ${stat.color}`} />
              </div>
              <div>
                <div className="text-2xl font-bold text-slateText-900">{loading ? '—' : stat.value}</div>
                <div className="text-xs text-slateText-500 font-medium">{stat.label}</div>
              </div>
            </Card>
          );
        })}
      </div>

      {/* ─── Pending Exchange Requests ─── */}
      {data && data.pendingRequests.length > 0 && (
        <div>
          <div className="flex items-center gap-2 mb-3">
            <Clock className="w-4 h-4 text-amber-500" />
            <h2 className="text-base font-semibold text-slateText-900">
              Pending Requests
            </h2>
            <Badge variant="warning">{data.pendingRequests.length}</Badge>
          </div>
          <div className="space-y-3">
            {data.pendingRequests.map((req) => (
              <Card key={req.id} className="p-4 border-l-4 border-l-amber-400">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                  <div className="space-y-0.5">
                    <p className="text-sm text-slateText-900">
                      <span className="font-semibold text-teal-600">{req.requesterName}</span>
                      {' '}wants to learn{' '}
                      <span className="font-semibold">{req.wantedSkillName}</span>
                      {' '}in exchange for teaching{' '}
                      <span className="font-semibold text-aiBlue">{req.offeredSkillName}</span>
                    </p>
                    {req.message && (
                      <p className="text-xs text-slateText-400 italic">"{req.message}"</p>
                    )}
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <Button size="sm" variant="danger" onClick={() => handleRespond(req.id, 'REJECTED')}>
                      <XCircle className="w-3.5 h-3.5 mr-1" />
                      Decline
                    </Button>
                    <Button size="sm" onClick={() => handleRespond(req.id, 'ACCEPTED')}>
                      <CheckCircle className="w-3.5 h-3.5 mr-1" />
                      Accept
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* ─── Two-Column: My Skills + Learning Goals ─── */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* My Teachable Skills */}
        <Card className="p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Award className="w-4 h-4 text-teal-500" />
              <h2 className="text-sm font-semibold text-slateText-900">My Teachable Skills</h2>
              <span className="text-xs bg-teal-50 text-teal-600 border border-teal-200 px-1.5 py-0.5 rounded font-mono">
                {teachingSkills.length}
              </span>
            </div>
            <button
              onClick={() => navigate('/skills')}
              className="text-xs text-teal-600 hover:text-teal-700 font-medium"
            >
              Manage →
            </button>
          </div>

          {loading ? (
            <div className="py-6 text-center text-sm text-slateText-400">Loading…</div>
          ) : teachingSkills.length === 0 ? (
            <div className="py-6 text-center">
              <p className="text-sm text-slateText-400">No teachable skills yet.</p>
              <Button size="sm" variant="outline" className="mt-3" onClick={() => navigate('/skills')}>
                <Plus className="w-3.5 h-3.5 mr-1" /> Add Skills
              </Button>
            </div>
          ) : (
            <div className="space-y-2">
              {teachingSkills.slice(0, 5).map((s) => (
                <div key={s.id} className="flex items-center justify-between py-2.5 px-3 rounded-btn bg-surface-50 border border-surface-200">
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

        {/* I Want To Learn */}
        <Card className="p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Target className="w-4 h-4 text-aiBlue" />
              <h2 className="text-sm font-semibold text-slateText-900">I Want To Learn</h2>
              <span className="text-xs bg-blue-50 text-aiBlue border border-blue-200 px-1.5 py-0.5 rounded font-mono">
                {(learningSkills.length) + (data?.myLearningGoals.length ?? 0)}
              </span>
            </div>
            <button
              onClick={() => navigate('/learning-goals')}
              className="text-xs text-aiBlue hover:text-blue-700 font-medium"
            >
              Manage →
            </button>
          </div>

          {loading ? (
            <div className="py-6 text-center text-sm text-slateText-400">Loading…</div>
          ) : learningSkills.length === 0 && (data?.myLearningGoals.length ?? 0) === 0 ? (
            <div className="py-6 text-center">
              <p className="text-sm text-slateText-400">No learning goals yet.</p>
              <Button size="sm" variant="outline" className="mt-3" onClick={() => navigate('/learning-goals')}>
                <Plus className="w-3.5 h-3.5 mr-1" /> Set Goals
              </Button>
            </div>
          ) : (
            <div className="space-y-2">
              {data?.myLearningGoals.slice(0, 3).map((g) => (
                <div key={g.id} className="flex items-center justify-between py-2.5 px-3 rounded-btn bg-surface-50 border border-surface-200">
                  <div>
                    <p className="text-sm font-medium text-slateText-900">{g.targetSkillName}</p>
                    {g.goalText && <p className="text-xs text-slateText-400 italic line-clamp-1">"{g.goalText}"</p>}
                  </div>
                  {g.targetLevel && <SkillLevelBadge level={g.targetLevel} />}
                </div>
              ))}
              {learningSkills.slice(0, 2).map((s) => (
                <div key={s.id} className="flex items-center justify-between py-2.5 px-3 rounded-btn bg-surface-50 border border-surface-200">
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

      {/* ─── AI Recommended Exchanges ─── */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-teal-500" />
            <h2 className="text-base font-semibold text-slateText-900">Recommended Skill Exchange</h2>
            <Badge variant="ai">AI Match Engine</Badge>
          </div>
          <button
            onClick={() => navigate('/skills/explore')}
            className="text-xs text-teal-600 hover:text-teal-700 font-medium"
          >
            Explore all →
          </button>
        </div>

        {/* AI Component: teal→blue gradient only here */}
        {!data || data.recommendedMatches.length === 0 ? (
          <Card variant="ai" className="p-6 text-center">
            <TrendingUp className="w-8 h-8 text-teal-400 mx-auto mb-2 opacity-60" />
            <p className="text-sm text-slateText-500">
              Add skills you want to learn or create a learning goal to see peer exchange recommendations here.
            </p>
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {data.recommendedMatches.map((m, idx) => (
              <Card key={idx} className="p-5 hover:shadow-md transition-shadow">
                <div className="flex items-start gap-3 mb-4">
                  {/* Avatar */}
                  <div className="w-11 h-11 rounded-full bg-teal-500 text-white font-bold flex items-center justify-center text-sm shrink-0">
                    {m.matchedUser.user.firstName[0]}{m.matchedUser.user.lastName[0]}
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-slateText-900 text-sm">
                      {m.matchedUser.user.firstName} {m.matchedUser.user.lastName}
                    </h3>
                    <p className="text-xs text-slateText-400 truncate">{m.matchedUser.user.email}</p>
                  </div>
                  {/* Match % pill */}
                  <span className="text-xs font-bold text-teal-600 bg-teal-50 border border-teal-200 px-2 py-0.5 rounded shrink-0">
                    Match
                  </span>
                </div>

                {/* AI match reason — light teal accent */}
                <div className="mb-4 p-2.5 bg-teal-50 border-l-2 border-teal-400 rounded-r-btn">
                  <p className="text-xs text-teal-700 leading-relaxed">{m.matchReason}</p>
                </div>

                <Button
                  size="sm"
                  variant="outline"
                  className="w-full"
                  onClick={() => navigate(`/users/${m.matchedUser.user.id}`)}
                >
                  <UserCheck className="w-3.5 h-3.5 mr-1.5" />
                  View Profile & Request
                </Button>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
