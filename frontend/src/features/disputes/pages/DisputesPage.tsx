import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { createDispute, getMyDisputes } from '../../../lib/apiClient';
import { Dispute, DisputeStatus } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { ShieldAlert, Plus, RefreshCw, Clock, CheckCircle, X, ChevronDown } from 'lucide-react';

const STATUS_CONFIG: Record<DisputeStatus, { label: string; badge: string }> = {
  OPEN:         { label: 'Open',         badge: 'bg-amber-50 text-amber-700 border-amber-200' },
  UNDER_REVIEW: { label: 'Under Review', badge: 'bg-blue-50 text-aiBlue border-blue-200' },
  RESOLVED:     { label: 'Resolved',     badge: 'bg-teal-50 text-teal-700 border-teal-200' },
  REJECTED:     { label: 'Rejected',     badge: 'bg-red-50 text-red-600 border-red-200' },
};

const DISPUTE_REASONS = [
  'Session not conducted',
  'Teacher did not show up',
  'Learner did not show up',
  'Poor quality session',
  'Technical issues',
  'Inappropriate behavior',
  'Skill mismatch',
  'Other',
];

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', { dateStyle: 'medium' });
}

export const DisputesPage: React.FC = () => {
  const location = useLocation();
  const prefilledSessionId = (location.state as { sessionId?: string })?.sessionId ?? '';

  const [disputes, setDisputes] = useState<Dispute[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(!!prefilledSessionId);

  // Form state
  const [sessionId, setSessionId] = useState(prefilledSessionId);
  const [reason, setReason] = useState(DISPUTE_REASONS[0]);
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const data = await getMyDisputes();
      setDisputes(Array.isArray(data) ? data : []);
    } catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async () => {
    setFormError('');
    if (!sessionId.trim()) { setFormError('Session ID is required.'); return; }
    if (!reason.trim()) { setFormError('Reason is required.'); return; }

    setSubmitting(true);
    try {
      await createDispute(sessionId.trim(), { reason, description: description || undefined });
      setSessionId('');
      setReason(DISPUTE_REASONS[0]);
      setDescription('');
      setShowForm(false);
      await load();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      setFormError(err?.response?.data?.message ?? 'Failed to submit dispute.');
    } finally { setSubmitting(false); }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
            <ShieldAlert className="w-6 h-6 text-violet-500" />
            Disputes
          </h1>
          <p className="text-sm text-slateText-500 mt-0.5">Raise and track session dispute cases</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={load}>
            <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Button size="sm" onClick={() => { setShowForm(!showForm); setFormError(''); }}>
            <Plus className="w-3.5 h-3.5 mr-1.5" />
            New Dispute
          </Button>
        </div>
      </div>

      {/* New dispute form */}
      {showForm && (
        <Card className="p-5 border-violet-200 bg-violet-50/30 space-y-4">
          <h2 className="text-sm font-semibold text-slateText-900">Raise a Dispute</h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Session ID */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slateText-600">Session ID *</label>
              <input
                type="text"
                value={sessionId}
                onChange={(e) => setSessionId(e.target.value)}
                placeholder="Paste the session UUID"
                className="w-full border border-surface-200 rounded-btn px-3 py-2 text-sm text-slateText-900 bg-white focus:outline-none focus:ring-1 focus:ring-violet-400"
              />
            </div>

            {/* Reason */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slateText-600">Reason *</label>
              <div className="relative">
                <select
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  className="w-full appearance-none border border-surface-200 rounded-btn px-3 py-2 text-sm text-slateText-900 bg-white focus:outline-none focus:ring-1 focus:ring-violet-400 pr-8"
                >
                  {DISPUTE_REASONS.map((r) => <option key={r} value={r}>{r}</option>)}
                </select>
                <ChevronDown className="absolute right-2 top-1/2 -translate-y-1/2 w-4 h-4 text-slateText-400 pointer-events-none" />
              </div>
            </div>
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-slateText-600">Description (optional)</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Provide additional context about the issue…"
              className="w-full border border-surface-200 rounded-btn px-3 py-2 text-sm text-slateText-900 bg-white focus:outline-none focus:ring-1 focus:ring-violet-400 resize-none"
            />
          </div>

          {formError && (
            <p className="text-xs text-red-500 flex items-center gap-1.5">
              <X className="w-3.5 h-3.5" /> {formError}
            </p>
          )}

          <div className="flex gap-2">
            <Button size="sm" onClick={handleSubmit} disabled={submitting}>
              {submitting ? 'Submitting…' : 'Submit Dispute'}
            </Button>
            <Button size="sm" variant="outline" onClick={() => { setShowForm(false); setFormError(''); }}>
              Cancel
            </Button>
          </div>
        </Card>
      )}

      {/* Disputes list */}
      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => <div key={i} className="h-24 bg-surface-100 rounded-panel animate-pulse" />)}
        </div>
      ) : disputes.length === 0 ? (
        <Card className="p-10 text-center">
          <ShieldAlert className="w-10 h-10 text-surface-300 mx-auto mb-3" />
          <p className="text-sm font-medium text-slateText-600">No disputes raised</p>
          <p className="text-xs text-slateText-400 mt-1">Disputes can be raised from completed or in-progress sessions.</p>
        </Card>
      ) : (
        <div className="space-y-3">
          {disputes.map((dispute) => {
            const cfg = STATUS_CONFIG[dispute.status] ?? STATUS_CONFIG.OPEN;
            return (
              <Card key={dispute.id} className="p-4">
                <div className="flex flex-col sm:flex-row sm:items-start gap-3">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap mb-1">
                      <span className={`text-[11px] font-semibold border px-2 py-0.5 rounded-full ${cfg.badge}`}>
                        {cfg.label}
                      </span>
                      <span className="text-sm font-semibold text-slateText-900">{dispute.reason}</span>
                    </div>
                    <div className="flex items-center gap-3 text-xs text-slateText-500 flex-wrap">
                      <span>Session: <span className="font-mono text-slateText-700">{dispute.sessionId.slice(0, 8)}…</span></span>
                      <span className="flex items-center gap-1">
                        <Clock className="w-3 h-3" /> {formatDate(dispute.createdAt)}
                      </span>
                    </div>
                    {dispute.description && (
                      <p className="text-xs text-slateText-500 mt-1.5 italic">"{dispute.description}"</p>
                    )}
                    {dispute.resolution && (
                      <div className="mt-2 flex items-start gap-1.5 p-2 bg-teal-50 border border-teal-200 rounded-btn">
                        <CheckCircle className="w-3.5 h-3.5 text-teal-500 shrink-0 mt-0.5" />
                        <p className="text-xs text-teal-700">
                          <span className="font-medium">Resolution:</span> {dispute.resolution}
                        </p>
                      </div>
                    )}
                  </div>
                  {/* Dispute ID */}
                  <div className="shrink-0 text-right">
                    <p className="text-[10px] text-slateText-400 font-mono">{dispute.id.slice(0, 8)}…</p>
                    {dispute.resolvedAt && (
                      <p className="text-[10px] text-slateText-400 mt-1">
                        Resolved {formatDate(dispute.resolvedAt)}
                      </p>
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
