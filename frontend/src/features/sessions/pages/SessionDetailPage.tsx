import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getSession, startSession, completeSession, cancelSession, markNoShow, submitReview } from '../../../lib/apiClient';
import { Session } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { useAuth } from '../../../lib/auth';
import {
  CheckCircle, XCircle, AlertTriangle, Play, Star, ChevronLeft, ShieldAlert,
} from 'lucide-react';

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString('en-IN', { dateStyle: 'long', timeStyle: 'short' });
}

const STATUS_BADGE: Record<string, string> = {
  SCHEDULED:   'bg-blue-50 text-aiBlue border-blue-200',
  IN_PROGRESS: 'bg-teal-50 text-teal-700 border-teal-200',
  COMPLETED:   'bg-teal-100 text-teal-800 border-teal-300',
  CANCELLED:   'bg-red-50 text-red-600 border-red-200',
  NO_SHOW:     'bg-amber-50 text-amber-700 border-amber-200',
  DISPUTED:    'bg-violet-50 text-violet-700 border-violet-200',
};

export const SessionDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(true);
  const [actioning, setActioning] = useState(false);
  const [showReview, setShowReview] = useState(false);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewDone, setReviewDone] = useState(false);

  const load = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await getSession(id);
      setSession(data);
    } catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [id]);

  const doAction = async (fn: () => Promise<unknown>) => {
    setActioning(true);
    try { await fn(); await load(); }
    catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      alert(err?.response?.data?.message ?? 'Action failed');
    }
    finally { setActioning(false); }
  };

  const handleReview = async () => {
    if (!id) return;
    setSubmittingReview(true);
    try {
      await submitReview(id, { rating, comment });
      setReviewDone(true);
      setShowReview(false);
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      alert(err?.response?.data?.message ?? 'Review failed');
    }
    finally { setSubmittingReview(false); }
  };

  if (loading) {
    return (
      <div className="space-y-4">
        <div className="h-8 w-48 bg-surface-100 rounded animate-pulse" />
        <div className="h-64 bg-surface-100 rounded-panel animate-pulse" />
      </div>
    );
  }

  if (!session) {
    return (
      <Card className="p-10 text-center">
        <p className="text-sm text-slateText-600">Session not found.</p>
        <Button variant="outline" size="sm" className="mt-4" onClick={() => navigate('/sessions')}>
          Back to Sessions
        </Button>
      </Card>
    );
  }

  const isParticipant = user?.id === session.teacherId || user?.id === session.learnerId;
  const canReview = session.status === 'COMPLETED' && isParticipant && !reviewDone;

  return (
    <div className="space-y-6 max-w-2xl">
      {/* Back */}
      <button
        onClick={() => navigate('/sessions')}
        className="flex items-center gap-1.5 text-sm text-slateText-500 hover:text-teal-600 transition-colors"
      >
        <ChevronLeft className="w-4 h-4" />
        Back to Sessions
      </button>

      {/* Main card */}
      <Card className="p-6 space-y-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h1 className="text-xl font-bold text-slateText-900">{session.skillName}</h1>
            <p className="text-sm text-slateText-500 mt-0.5">Session ID: {session.id.slice(0, 8)}…</p>
          </div>
          <span className={`text-xs font-semibold border px-2.5 py-1 rounded-full ${STATUS_BADGE[session.status] ?? ''}`}>
            {session.status.replace('_', ' ')}
          </span>
        </div>

        {/* Details grid */}
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-xs text-slateText-400 mb-0.5">Teacher</p>
            <p className="font-medium text-slateText-900">{session.teacherName}</p>
          </div>
          <div>
            <p className="text-xs text-slateText-400 mb-0.5">Learner</p>
            <p className="font-medium text-slateText-900">{session.learnerName}</p>
          </div>
          <div>
            <p className="text-xs text-slateText-400 mb-0.5">Scheduled Start</p>
            <p className="font-medium text-slateText-900">{formatDateTime(session.scheduledStart)}</p>
          </div>
          <div>
            <p className="text-xs text-slateText-400 mb-0.5">Scheduled End</p>
            <p className="font-medium text-slateText-900">{formatDateTime(session.scheduledEnd)}</p>
          </div>
          {session.startedAt && (
            <div>
              <p className="text-xs text-slateText-400 mb-0.5">Started At</p>
              <p className="font-medium text-slateText-900">{formatDateTime(session.startedAt)}</p>
            </div>
          )}
          {session.completedAt && (
            <div>
              <p className="text-xs text-slateText-400 mb-0.5">Completed At</p>
              <p className="font-medium text-slateText-900">{formatDateTime(session.completedAt)}</p>
            </div>
          )}
          {session.meetingLink && (
            <div className="col-span-2">
              <p className="text-xs text-slateText-400 mb-0.5">Meeting Link</p>
              <a
                href={session.meetingLink}
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-teal-600 hover:underline break-all"
              >
                {session.meetingLink}
              </a>
            </div>
          )}
        </div>

        {session.creditsSettled && (
          <div className="flex items-center gap-2 px-3 py-2 bg-teal-50 border border-teal-200 rounded-btn">
            <CheckCircle className="w-4 h-4 text-teal-500" />
            <span className="text-sm text-teal-700 font-medium">Credits have been settled for this session</span>
          </div>
        )}

        {/* Action buttons */}
        <div className="flex flex-wrap gap-2 pt-2 border-t border-surface-100">
          {session.status === 'SCHEDULED' && (
            <>
              <Button size="sm" disabled={actioning} onClick={() => doAction(() => startSession(session.id))}>
                <Play className="w-3.5 h-3.5 mr-1.5" /> Start Session
              </Button>
              <Button size="sm" variant="danger" disabled={actioning} onClick={() => doAction(() => cancelSession(session.id))}>
                <XCircle className="w-3.5 h-3.5 mr-1.5" /> Cancel
              </Button>
            </>
          )}
          {session.status === 'IN_PROGRESS' && (
            <>
              <Button size="sm" disabled={actioning} onClick={() => doAction(() => completeSession(session.id))}>
                <CheckCircle className="w-3.5 h-3.5 mr-1.5" /> Complete
              </Button>
              <Button size="sm" variant="outline" disabled={actioning} onClick={() => doAction(() => markNoShow(session.id))}>
                <AlertTriangle className="w-3.5 h-3.5 mr-1.5" /> Mark No-Show
              </Button>
            </>
          )}
          {canReview && (
            <Button size="sm" variant="outline" onClick={() => setShowReview(true)}>
              <Star className="w-3.5 h-3.5 mr-1.5" /> Leave Review
            </Button>
          )}
          {reviewDone && (
            <span className="flex items-center gap-1.5 text-sm text-teal-600 font-medium">
              <CheckCircle className="w-4 h-4" /> Review submitted!
            </span>
          )}
          {(session.status === 'COMPLETED' || session.status === 'IN_PROGRESS') && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => navigate('/disputes', { state: { sessionId: session.id } })}
            >
              <ShieldAlert className="w-3.5 h-3.5 mr-1.5" /> Raise Dispute
            </Button>
          )}
        </div>
      </Card>

      {/* Inline review form */}
      {showReview && (
        <Card className="p-5 space-y-4">
          <h2 className="text-sm font-semibold text-slateText-900">Leave a Review</h2>

          <div className="space-y-1.5">
            <label className="text-xs font-medium text-slateText-600">Rating</label>
            <div className="flex gap-1.5 items-center">
              {[1, 2, 3, 4, 5].map((n) => (
                <button
                  key={n}
                  onClick={() => setRating(n)}
                  className={`w-8 h-8 rounded-btn transition-colors flex items-center justify-center ${
                    n <= rating ? 'text-amber-400 bg-amber-50' : 'text-surface-300 hover:text-amber-300'
                  }`}
                >
                  <Star className="w-5 h-5 fill-current" />
                </button>
              ))}
              <span className="ml-2 text-sm font-medium text-slateText-700">{rating}/5</span>
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-medium text-slateText-600">Comment (optional)</label>
            <textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              rows={3}
              placeholder="Share your experience…"
              className="w-full border border-surface-200 rounded-btn px-3 py-2 text-sm text-slateText-900 bg-white focus:outline-none focus:ring-1 focus:ring-teal-500 resize-none"
            />
          </div>

          <div className="flex gap-2">
            <Button size="sm" onClick={handleReview} disabled={submittingReview}>
              {submittingReview ? 'Submitting…' : 'Submit Review'}
            </Button>
            <Button size="sm" variant="outline" onClick={() => setShowReview(false)}>
              Cancel
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
};
