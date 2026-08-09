import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMySessions, startSession, completeSession, cancelSession, markNoShow } from '../../../lib/apiClient';
import { Session, SessionStatus } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import {
  ArrowLeftRight, Clock, CheckCircle, XCircle, AlertTriangle,
  Play, Eye, RefreshCw, Filter,
} from 'lucide-react';

const STATUS_CONFIG: Record<SessionStatus, { label: string; color: string; badge: string }> = {
  SCHEDULED:  { label: 'Scheduled',   color: 'text-aiBlue',    badge: 'bg-blue-50 text-aiBlue border-blue-200' },
  IN_PROGRESS:{ label: 'In Progress', color: 'text-teal-600',  badge: 'bg-teal-50 text-teal-700 border-teal-200' },
  COMPLETED:  { label: 'Completed',   color: 'text-teal-700',  badge: 'bg-teal-100 text-teal-800 border-teal-300' },
  CANCELLED:  { label: 'Cancelled',   color: 'text-red-500',   badge: 'bg-red-50 text-red-600 border-red-200' },
  NO_SHOW:    { label: 'No Show',     color: 'text-amber-600', badge: 'bg-amber-50 text-amber-700 border-amber-200' },
  DISPUTED:   { label: 'Disputed',    color: 'text-violet-600',badge: 'bg-violet-50 text-violet-700 border-violet-200' },
};

const ALL_STATUSES: SessionStatus[] = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'DISPUTED'];

function formatDateTime(iso: string) {
  const d = new Date(iso);
  return d.toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

export const SessionsPage: React.FC = () => {
  const navigate = useNavigate();
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<SessionStatus | 'ALL'>('ALL');
  const [actioning, setActioning] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      const data = await getMySessions();
      setSessions(Array.isArray(data) ? data : []);
    } catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const action = async (fn: () => Promise<unknown>, sessionId: string) => {
    setActioning(sessionId);
    try { await fn(); await load(); }
    catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      alert(err?.response?.data?.message ?? 'Action failed');
    }
    finally { setActioning(null); }
  };

  const filtered = filter === 'ALL' ? sessions : sessions.filter((s) => s.status === filter);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
            <ArrowLeftRight className="w-6 h-6 text-teal-500" />
            Sessions
          </h1>
          <p className="text-sm text-slateText-500 mt-0.5">Manage your skill exchange sessions</p>
        </div>
        <Button variant="outline" size="sm" onClick={load}>
          <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Status filter pills */}
      <div className="flex items-center gap-2 flex-wrap">
        <Filter className="w-3.5 h-3.5 text-slateText-400 shrink-0" />
        {(['ALL', ...ALL_STATUSES] as const).map((s) => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            className={`px-3 py-1 rounded-full text-xs font-medium border transition-colors ${
              filter === s
                ? 'bg-teal-500 text-white border-teal-500'
                : 'bg-white text-slateText-600 border-surface-200 hover:border-teal-300'
            }`}
          >
            {s === 'ALL' ? 'All' : STATUS_CONFIG[s].label}
            {s !== 'ALL' && (
              <span className="ml-1.5 text-[10px] opacity-70">
                {sessions.filter((ss) => ss.status === s).length}
              </span>
            )}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => <div key={i} className="h-24 bg-surface-100 rounded-panel animate-pulse" />)}
        </div>
      ) : filtered.length === 0 ? (
        <Card className="p-10 text-center">
          <ArrowLeftRight className="w-10 h-10 text-surface-300 mx-auto mb-3" />
          <p className="text-sm font-medium text-slateText-600">No sessions found</p>
          <p className="text-xs text-slateText-400 mt-1">
            {filter === 'ALL' ? 'Accept exchange requests to create sessions.' : `No ${STATUS_CONFIG[filter as SessionStatus]?.label} sessions.`}
          </p>
        </Card>
      ) : (
        <div className="space-y-3">
          {filtered.map((session) => {
            const cfg = STATUS_CONFIG[session.status];
            const busy = actioning === session.id;
            return (
              <Card key={session.id} className="p-4 hover:shadow-sm transition-shadow">
                <div className="flex flex-col sm:flex-row sm:items-center gap-3">
                  {/* Status + skill info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1 flex-wrap">
                      <span className={`text-[11px] font-semibold border px-2 py-0.5 rounded-full ${cfg.badge}`}>
                        {cfg.label}
                      </span>
                      <span className="text-sm font-semibold text-slateText-900 truncate">
                        {session.skillName}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 text-xs text-slateText-500 flex-wrap">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3 h-3" />
                        {formatDateTime(session.scheduledStart)}
                      </span>
                      {session.durationMinutes && (
                        <span>{session.durationMinutes} min</span>
                      )}
                      <span>Teacher: <span className="font-medium text-slateText-700">{session.teacherName}</span></span>
                      <span>Learner: <span className="font-medium text-slateText-700">{session.learnerName}</span></span>
                    </div>
                    {session.creditsSettled && (
                      <span className="inline-flex items-center gap-1 mt-1 text-[10px] text-teal-600 font-medium">
                        <CheckCircle className="w-3 h-3" /> Credits settled
                      </span>
                    )}
                  </div>

                  {/* Action buttons */}
                  <div className="flex items-center gap-2 shrink-0 flex-wrap">
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => navigate(`/sessions/${session.id}`)}
                    >
                      <Eye className="w-3.5 h-3.5 mr-1" />
                      Detail
                    </Button>
                    {session.status === 'SCHEDULED' && (
                      <>
                        <Button
                          size="sm"
                          disabled={busy}
                          onClick={() => action(() => startSession(session.id), session.id)}
                        >
                          <Play className="w-3.5 h-3.5 mr-1" />
                          Start
                        </Button>
                        <Button
                          size="sm"
                          variant="danger"
                          disabled={busy}
                          onClick={() => action(() => cancelSession(session.id), session.id)}
                        >
                          <XCircle className="w-3.5 h-3.5 mr-1" />
                          Cancel
                        </Button>
                      </>
                    )}
                    {session.status === 'IN_PROGRESS' && (
                      <>
                        <Button
                          size="sm"
                          disabled={busy}
                          onClick={() => action(() => completeSession(session.id), session.id)}
                        >
                          <CheckCircle className="w-3.5 h-3.5 mr-1" />
                          Complete
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          disabled={busy}
                          onClick={() => action(() => markNoShow(session.id), session.id)}
                        >
                          <AlertTriangle className="w-3.5 h-3.5 mr-1" />
                          No-Show
                        </Button>
                      </>
                    )}
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
};
