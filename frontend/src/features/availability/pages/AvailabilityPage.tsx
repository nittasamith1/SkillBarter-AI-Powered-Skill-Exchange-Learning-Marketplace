import React, { useEffect, useState } from 'react';
import {
  getMyAvailability,
  createAvailability,
  deleteAvailability,
} from '../../../lib/apiClient';
import { UserAvailability } from '../../../types';
import { Card } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { Calendar, Plus, Trash2, Clock, ChevronDown } from 'lucide-react';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DAY_SHORT: Record<string, string> = {
  MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu',
  FRIDAY: 'Fri', SATURDAY: 'Sat', SUNDAY: 'Sun',
};
const TZ_OPTIONS = [
  'Asia/Kolkata', 'UTC', 'America/New_York', 'America/Los_Angeles',
  'Europe/London', 'Europe/Berlin', 'Asia/Tokyo', 'Asia/Singapore',
  'Australia/Sydney',
];

interface SlotForm {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  timezone: string;
}

const EMPTY_FORM: SlotForm = {
  dayOfWeek: 'MONDAY',
  startTime: '09:00',
  endTime: '10:00',
  timezone: 'Asia/Kolkata',
};

export const AvailabilityPage: React.FC = () => {
  const [slots, setSlots] = useState<UserAvailability[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<SlotForm>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState<string | null>(null);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const data = await getMyAvailability();
      setSlots(Array.isArray(data) ? data : []);
    } catch { /* silent */ }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const handleSave = async () => {
    setError('');
    if (form.startTime >= form.endTime) {
      setError('Start time must be before end time.');
      return;
    }
    setSaving(true);
    try {
      await createAvailability(form);
      setForm(EMPTY_FORM);
      setShowForm(false);
      await load();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      setError(err?.response?.data?.message ?? 'Failed to save slot.');
    } finally { setSaving(false); }
  };

  const handleDelete = async (id: string) => {
    setDeleting(id);
    try {
      await deleteAvailability(id);
      setSlots((prev) => prev.filter((s) => s.id !== id));
    } catch { /* silent */ }
    finally { setDeleting(null); }
  };

  const grouped = DAYS.reduce<Record<string, UserAvailability[]>>((acc, d) => {
    acc[d] = slots.filter((s) => s.dayOfWeek === d);
    return acc;
  }, {});

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
            <Calendar className="w-6 h-6 text-teal-500" />
            My Availability
          </h1>
          <p className="text-sm text-slateText-500 mt-0.5">
            Set your weekly schedule for skill exchange sessions
          </p>
        </div>
        <Button variant="primary" size="sm" onClick={() => { setShowForm(!showForm); setError(''); }}>
          <Plus className="w-3.5 h-3.5 mr-1.5" />
          Add Time Slot
        </Button>
      </div>

      {/* Add Form */}
      {showForm && (
        <Card className="p-5 border-teal-200 bg-teal-50/30">
          <h2 className="text-sm font-semibold text-slateText-900 mb-4">New Availability Slot</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Day */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slateText-600">Day of Week</label>
              <div className="relative">
                <select
                  value={form.dayOfWeek}
                  onChange={(e) => setForm((f) => ({ ...f, dayOfWeek: e.target.value }))}
                  className="w-full appearance-none border border-surface-200 rounded-btn px-3 py-2 text-sm text-slateText-900 bg-white focus:outline-none focus:ring-1 focus:ring-teal-500 pr-8"
                >
                  {DAYS.map((d) => <option key={d} value={d}>{d.charAt(0) + d.slice(1).toLowerCase()}</option>)}
                </select>
                <ChevronDown className="absolute right-2 top-1/2 -translate-y-1/2 w-4 h-4 text-slateText-400 pointer-events-none" />
              </div>
            </div>

            {/* Start time */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slateText-600">Start Time</label>
              <input
                type="time"
                value={form.startTime}
                onChange={(e) => setForm((f) => ({ ...f, startTime: e.target.value }))}
                className="w-full border border-surface-200 rounded-btn px-3 py-2 text-sm text-slateText-900 bg-white focus:outline-none focus:ring-1 focus:ring-teal-500"
              />
            </div>

            {/* End time */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slateText-600">End Time</label>
              <input
                type="time"
                value={form.endTime}
                onChange={(e) => setForm((f) => ({ ...f, endTime: e.target.value }))}
                className="w-full border border-surface-200 rounded-btn px-3 py-2 text-sm text-slateText-900 bg-white focus:outline-none focus:ring-1 focus:ring-teal-500"
              />
            </div>

            {/* Timezone */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slateText-600">Timezone</label>
              <div className="relative">
                <select
                  value={form.timezone}
                  onChange={(e) => setForm((f) => ({ ...f, timezone: e.target.value }))}
                  className="w-full appearance-none border border-surface-200 rounded-btn px-3 py-2 text-sm text-slateText-900 bg-white focus:outline-none focus:ring-1 focus:ring-teal-500 pr-8"
                >
                  {TZ_OPTIONS.map((tz) => <option key={tz} value={tz}>{tz}</option>)}
                </select>
                <ChevronDown className="absolute right-2 top-1/2 -translate-y-1/2 w-4 h-4 text-slateText-400 pointer-events-none" />
              </div>
            </div>
          </div>

          {error && <p className="mt-3 text-xs text-red-500">{error}</p>}

          <div className="flex items-center gap-2 mt-4">
            <Button size="sm" onClick={handleSave} disabled={saving}>
              {saving ? 'Saving…' : 'Save Slot'}
            </Button>
            <Button size="sm" variant="outline" onClick={() => { setShowForm(false); setError(''); }}>
              Cancel
            </Button>
          </div>
        </Card>
      )}

      {/* Weekly Grid */}
      {loading ? (
        <div className="grid grid-cols-7 gap-2">
          {DAYS.map((d) => (
            <div key={d} className="h-40 bg-surface-100 rounded-panel animate-pulse" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-3 lg:grid-cols-7 gap-3">
          {DAYS.map((day) => {
            const daySlots = grouped[day] ?? [];
            return (
              <div key={day} className="space-y-2">
                <div className={`text-center py-1.5 rounded-btn text-xs font-bold tracking-wide ${
                  daySlots.length > 0
                    ? 'bg-teal-500 text-white'
                    : 'bg-surface-100 text-slateText-400'
                }`}>
                  {DAY_SHORT[day]}
                </div>
                <div className="space-y-1.5 min-h-[80px]">
                  {daySlots.length === 0 && (
                    <div className="text-center py-4 text-[11px] text-slateText-300">—</div>
                  )}
                  {daySlots.map((slot) => (
                    <div
                      key={slot.id}
                      className="group relative bg-teal-50 border border-teal-200 rounded-btn px-2 py-2 text-center"
                    >
                      <div className="flex items-center justify-center gap-1 text-[10px] text-teal-700 font-medium">
                        <Clock className="w-2.5 h-2.5" />
                        {slot.startTime.slice(0, 5)}
                      </div>
                      <div className="text-[10px] text-teal-600 mt-0.5">{slot.endTime.slice(0, 5)}</div>
                      <div className="text-[9px] text-teal-400 truncate mt-0.5">{slot.timezone.split('/').pop()}</div>
                      <button
                        onClick={() => handleDelete(slot.id)}
                        disabled={deleting === slot.id}
                        className="absolute top-1 right-1 opacity-0 group-hover:opacity-100 transition-opacity p-0.5 rounded text-red-400 hover:bg-red-50 hover:text-red-600"
                        title="Delete slot"
                      >
                        <Trash2 className="w-3 h-3" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {!loading && slots.length === 0 && !showForm && (
        <Card className="p-10 text-center">
          <Calendar className="w-10 h-10 text-surface-300 mx-auto mb-3" />
          <p className="text-sm font-medium text-slateText-600">No availability slots yet</p>
          <p className="text-xs text-slateText-400 mt-1">Add your weekly schedule to help the AI match you with compatible peers</p>
          <Button variant="outline" size="sm" className="mt-4" onClick={() => setShowForm(true)}>
            <Plus className="w-3.5 h-3.5 mr-1.5" />
            Add First Slot
          </Button>
        </Card>
      )}
    </div>
  );
};
