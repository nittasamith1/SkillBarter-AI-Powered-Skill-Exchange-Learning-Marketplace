import React, { useState, useRef, useEffect } from 'react';
import { Bell, CheckCheck, X, Info, Calendar, Star, DollarSign, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '../../lib/apiClient';
import { NotificationItem } from '../../types';

const typeIcon: Record<string, React.ReactNode> = {
  SESSION_SCHEDULED:  <Calendar className="w-4 h-4 text-aiBlue" />,
  SESSION_STARTED:    <Calendar className="w-4 h-4 text-teal-500" />,
  SESSION_COMPLETED:  <Calendar className="w-4 h-4 text-teal-600" />,
  SESSION_CANCELLED:  <Calendar className="w-4 h-4 text-red-400" />,
  SESSION_NO_SHOW:    <Calendar className="w-4 h-4 text-amber-500" />,
  CREDIT_EARNED:      <DollarSign className="w-4 h-4 text-teal-500" />,
  CREDIT_SPENT:       <DollarSign className="w-4 h-4 text-violet-500" />,
  REVIEW_RECEIVED:    <Star className="w-4 h-4 text-amber-400" />,
  DISPUTE_OPENED:     <AlertTriangle className="w-4 h-4 text-red-500" />,
  DISPUTE_RESOLVED:   <AlertTriangle className="w-4 h-4 text-teal-500" />,
  EXCHANGE_REQUESTED: <Info className="w-4 h-4 text-aiBlue" />,
  EXCHANGE_ACCEPTED:  <Info className="w-4 h-4 text-teal-500" />,
  EXCHANGE_REJECTED:  <Info className="w-4 h-4 text-red-400" />,
};

function timeAgo(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return 'just now';
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  return `${Math.floor(h / 24)}d ago`;
}

export const NotificationDropdown: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  const unread = items.filter((n) => !n.read).length;

  const load = async () => {
    setLoading(true);
    try {
      const data = await getNotifications();
      setItems(Array.isArray(data) ? data : []);
    } catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => {
    load();
    const id = setInterval(load, 30000);
    return () => clearInterval(id);
  }, []);

  useEffect(() => {
    if (open) load();
  }, [open]);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleMark = async (id: string) => {
    try {
      await markNotificationRead(id);
      setItems((prev) => prev.map((n) => n.id === id ? { ...n, read: true } : n));
    } catch { /* silent */ }
  };

  const handleMarkAll = async () => {
    try {
      await markAllNotificationsRead();
      setItems((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch { /* silent */ }
  };

  const handleClick = (n: NotificationItem) => {
    handleMark(n.id);
    if (n.referenceType === 'SESSION') navigate('/sessions');
    else if (n.referenceType === 'DISPUTE') navigate('/disputes');
    else if (n.referenceType === 'SESSION_CREDIT') navigate('/credits');
    setOpen(false);
  };

  return (
    <div className="relative" ref={ref}>
      <button
        id="notification-bell-btn"
        onClick={() => setOpen((v) => !v)}
        className="relative p-2 text-slateText-400 hover:text-slateText-700 hover:bg-surface-100 rounded-btn transition-colors"
        title="Notifications"
      >
        <Bell className="w-4 h-4" />
        {unread > 0 && (
          <span className="absolute top-1 right-1 min-w-[16px] h-4 bg-teal-500 text-white text-[9px] font-bold rounded-full flex items-center justify-center px-0.5 leading-none">
            {unread > 9 ? '9+' : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-1.5 w-80 bg-white border border-surface-200 rounded-panel shadow-xl z-50 overflow-hidden">
          {/* Header */}
          <div className="flex items-center justify-between px-4 py-3 border-b border-surface-200">
            <span className="text-sm font-semibold text-slateText-900">Notifications</span>
            <div className="flex items-center gap-2">
              {unread > 0 && (
                <button
                  onClick={handleMarkAll}
                  className="text-[11px] text-teal-600 hover:text-teal-700 font-medium flex items-center gap-1"
                  title="Mark all as read"
                >
                  <CheckCheck className="w-3.5 h-3.5" />
                  Mark all read
                </button>
              )}
              <button
                onClick={() => setOpen(false)}
                className="p-0.5 rounded text-slateText-400 hover:text-slateText-600 hover:bg-surface-100"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          {/* List */}
          <div className="max-h-80 overflow-y-auto divide-y divide-surface-100">
            {loading && items.length === 0 && (
              <div className="py-8 text-center text-sm text-slateText-400">Loading…</div>
            )}
            {!loading && items.length === 0 && (
              <div className="py-8 text-center">
                <Bell className="w-8 h-8 text-surface-300 mx-auto mb-2" />
                <p className="text-sm text-slateText-400">You're all caught up!</p>
              </div>
            )}
            {items.map((n) => (
              <button
                key={n.id}
                onClick={() => handleClick(n)}
                className={`w-full text-left px-4 py-3 flex items-start gap-3 hover:bg-surface-50 transition-colors ${
                  !n.read ? 'bg-teal-50/60' : ''
                }`}
              >
                <div className="mt-0.5 shrink-0">
                  {typeIcon[n.type] ?? <Info className="w-4 h-4 text-slateText-400" />}
                </div>
                <div className="flex-1 min-w-0">
                  <p className={`text-xs leading-snug ${!n.read ? 'text-slateText-900 font-medium' : 'text-slateText-600'}`}>
                    {n.message}
                  </p>
                  <p className="text-[10px] text-slateText-400 mt-0.5">{timeAgo(n.createdAt)}</p>
                </div>
                {!n.read && (
                  <div className="w-2 h-2 rounded-full bg-teal-500 shrink-0 mt-1.5" />
                )}
              </button>
            ))}
          </div>

          {items.length > 0 && (
            <div className="border-t border-surface-200 px-4 py-2.5 text-center">
              <span className="text-[11px] text-slateText-400">{items.length} notification{items.length !== 1 ? 's' : ''}</span>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
