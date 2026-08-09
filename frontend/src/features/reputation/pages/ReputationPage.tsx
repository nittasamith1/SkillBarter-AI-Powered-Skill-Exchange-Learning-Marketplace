import React, { useEffect, useState } from 'react';
import { useAuth } from '../../../lib/auth';
import { getMyTrustScore, getMyReviews } from '../../../lib/apiClient';
import { TrustScore, Review } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { Star, Shield, RefreshCw, TrendingUp, CheckCircle, Clock, MessageSquare } from 'lucide-react';

function StarRow({ rating }: { rating: number }) {
  return (
    <div className="flex gap-0.5">
      {[1, 2, 3, 4, 5].map((n) => (
        <Star
          key={n}
          className={`w-3.5 h-3.5 ${n <= rating ? 'text-amber-400 fill-amber-400' : 'text-surface-300'}`}
        />
      ))}
    </div>
  );
}

function ScoreGauge({ value, label, color }: { value: number; label: string; color: string }) {
  return (
    <div className="space-y-1.5">
      <div className="flex justify-between items-center text-xs">
        <span className="text-slateText-500">{label}</span>
        <span className="font-bold text-slateText-900">{value.toFixed(1)}</span>
      </div>
      <div className="h-2 bg-surface-100 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full ${color} transition-all duration-700`}
          style={{ width: `${Math.min(value, 100)}%` }}
        />
      </div>
    </div>
  );
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-IN', { dateStyle: 'medium' });
}

export const ReputationPage: React.FC = () => {
  const { user } = useAuth();
  const [trustScore, setTrustScore] = useState<TrustScore | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    if (!user?.id) return;
    setLoading(true);
    try {
      const [ts, rv] = await Promise.all([
        getMyTrustScore(user.id),
        getMyReviews(user.id),
      ]);
      setTrustScore(ts);
      setReviews(Array.isArray(rv) ? rv : []);
    } catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, [user?.id]);

  const scoreColor = (v: number) =>
    v >= 80 ? 'bg-teal-500' : v >= 60 ? 'bg-aiBlue' : v >= 40 ? 'bg-amber-400' : 'bg-red-400';

  const scoreGrade = (v: number) =>
    v >= 90 ? 'Excellent' : v >= 75 ? 'Good' : v >= 50 ? 'Fair' : 'Building';

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
            <Star className="w-6 h-6 text-amber-400" />
            Reputation
          </h1>
          <p className="text-sm text-slateText-500 mt-0.5">Your trust score and peer reviews</p>
        </div>
        <Button variant="outline" size="sm" onClick={load}>
          <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div className="h-64 bg-surface-100 rounded-panel animate-pulse" />
          <div className="h-64 bg-surface-100 rounded-panel animate-pulse" />
        </div>
      ) : (
        <>
          {/* Trust Score card */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {/* Overall score */}
            <Card className="p-6 space-y-5">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-full bg-amber-50 flex items-center justify-center shrink-0">
                  <Shield className="w-6 h-6 text-amber-500" />
                </div>
                <div>
                  <h2 className="text-sm font-semibold text-slateText-900">Trust Score</h2>
                  <p className="text-xs text-slateText-400">{scoreGrade(trustScore?.overallScore ?? 0)}</p>
                </div>
                {/* Big score circle */}
                <div className="ml-auto">
                  <div className={`w-16 h-16 rounded-full flex items-center justify-center text-white font-bold text-lg shadow-md ${
                    scoreColor(trustScore?.overallScore ?? 0)
                  }`}>
                    {trustScore ? Math.round(trustScore.overallScore) : '—'}
                  </div>
                </div>
              </div>

              {/* Breakdown */}
              <div className="space-y-3">
                <ScoreGauge
                  label="Average Rating (40%)"
                  value={(trustScore?.ratingScore ?? 0) * 100}
                  color={scoreColor((trustScore?.ratingScore ?? 0) * 100)}
                />
                <ScoreGauge
                  label="Completion Rate (20%)"
                  value={(trustScore?.completionScore ?? 0) * 100}
                  color={scoreColor((trustScore?.completionScore ?? 0) * 100)}
                />
                <ScoreGauge
                  label="Reliability (20%)"
                  value={(trustScore?.reliabilityScore ?? 0) * 100}
                  color={scoreColor((trustScore?.reliabilityScore ?? 0) * 100)}
                />
                <ScoreGauge
                  label="Response Rate (10%)"
                  value={(trustScore?.responseScore ?? 0) * 100}
                  color={scoreColor((trustScore?.responseScore ?? 0) * 100)}
                />
                <ScoreGauge
                  label="Cancellation Penalty (10%)"
                  value={Math.max(0, 100 - (trustScore?.cancellationPenalty ?? 0) * 100)}
                  color={scoreColor(Math.max(0, 100 - (trustScore?.cancellationPenalty ?? 0) * 100))}
                />
              </div>
            </Card>

            {/* Stats summary */}
            <Card className="p-6 space-y-4">
              <h2 className="text-sm font-semibold text-slateText-900">Your Stats</h2>
              <div className="grid grid-cols-2 gap-4">
                {[
                  { label: 'Total Reviews',       value: trustScore?.totalReviews ?? 0,       icon: MessageSquare, color: 'text-aiBlue', bg: 'bg-blue-50' },
                  { label: 'Sessions Completed',  value: trustScore?.completedSessions ?? 0,  icon: CheckCircle,   color: 'text-teal-600', bg: 'bg-teal-50' },
                  { label: 'Sessions Cancelled',  value: trustScore?.cancelledSessions ?? 0,  icon: RefreshCw,     color: 'text-amber-600', bg: 'bg-amber-50' },
                  { label: 'Average Rating',      value: (trustScore?.averageRating ?? 0).toFixed(1), icon: Star, color: 'text-amber-500', bg: 'bg-amber-50' },
                ].map((stat) => {
                  const Icon = stat.icon;
                  return (
                    <div key={stat.label} className="flex items-center gap-3 p-3 bg-surface-50 border border-surface-200 rounded-btn">
                      <div className={`w-9 h-9 rounded-btn ${stat.bg} flex items-center justify-center shrink-0`}>
                        <Icon className={`w-4 h-4 ${stat.color}`} />
                      </div>
                      <div>
                        <div className="text-lg font-bold text-slateText-900">{stat.value}</div>
                        <div className="text-[10px] text-slateText-400 leading-tight">{stat.label}</div>
                      </div>
                    </div>
                  );
                })}
              </div>

              {trustScore && (
                <div className="pt-3 border-t border-surface-100 flex items-center gap-2">
                  <Clock className="w-3.5 h-3.5 text-slateText-400" />
                  <p className="text-xs text-slateText-400">
                    Last updated: {formatDate(trustScore.updatedAt)}
                  </p>
                </div>
              )}

              {!trustScore && (
                <div className="text-center py-6">
                  <TrendingUp className="w-8 h-8 text-surface-300 mx-auto mb-2" />
                  <p className="text-sm text-slateText-500">Complete sessions to build your trust score!</p>
                </div>
              )}
            </Card>
          </div>

          {/* Reviews list */}
          <div>
            <div className="flex items-center gap-2 mb-3">
              <MessageSquare className="w-4 h-4 text-slateText-400" />
              <h2 className="text-sm font-semibold text-slateText-900">
                Peer Reviews
              </h2>
              <span className="text-xs bg-surface-100 text-slateText-500 border border-surface-200 px-1.5 py-0.5 rounded font-mono">
                {reviews.length}
              </span>
            </div>

            {reviews.length === 0 ? (
              <Card className="p-8 text-center">
                <Star className="w-8 h-8 text-surface-300 mx-auto mb-2" />
                <p className="text-sm text-slateText-500">No reviews yet. Complete sessions to receive feedback!</p>
              </Card>
            ) : (
              <div className="space-y-3">
                {reviews.map((review) => (
                  <Card key={review.id} className="p-4">
                    <div className="flex items-start gap-3">
                      <div className="w-9 h-9 rounded-full bg-gradient-to-br from-teal-400 to-aiBlue text-white font-bold flex items-center justify-center text-xs shrink-0">
                        {review.reviewerName?.[0] ?? '?'}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between gap-2 flex-wrap">
                          <span className="text-sm font-semibold text-slateText-900">{review.reviewerName}</span>
                          <div className="flex items-center gap-2">
                            <StarRow rating={review.rating} />
                            <span className="text-xs text-slateText-400">{formatDate(review.createdAt)}</span>
                          </div>
                        </div>
                        {review.comment && (
                          <p className="text-sm text-slateText-600 mt-1.5 italic">"{review.comment}"</p>
                        )}
                      </div>
                    </div>
                  </Card>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
};
