import React, { useState } from 'react';
import { useAuth } from '../../../lib/auth';
import { Card } from '../../../components/ui/Card';
import { Input } from '../../../components/ui/Input';
import { Button } from '../../../components/ui/Button';
import { Badge } from '../../../components/ui/Badge';
import { Check, AlertCircle, Building2, Globe, MapPin, User as UserIcon } from 'lucide-react';
import { apiClient } from '../../../lib/apiClient';
import { ApiResponse, User } from '../../../types';

const selectCls = 'w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500';
const labelCls  = 'block text-xs font-semibold text-slateText-700 uppercase tracking-wider mb-1.5';

export const ProfilePage: React.FC = () => {
  const { user, updateUser } = useAuth();

  const [formData, setFormData] = useState({
    firstName: user?.firstName || '',
    lastName:  user?.lastName  || '',
    bio:       user?.bio       || '',
    location:  user?.location  || '',
    preferredLanguage: user?.preferredLanguage || 'en',
  });

  const [isLoading,      setIsLoading]      = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [error,          setError]          = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true); setSuccessMessage(null); setError(null);
    try {
      const { data } = await apiClient.put<ApiResponse<User>>('/users/me', formData);
      updateUser(data.data);
      setSuccessMessage('Profile updated successfully!');
    } catch {
      setError('Failed to update profile. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const initials = `${user?.firstName?.[0] ?? ''}${user?.lastName?.[0] ?? ''}`;

  return (
    <div className="max-w-4xl mx-auto space-y-7">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-slateText-900 tracking-tight flex items-center gap-2">
          <UserIcon className="w-6 h-6 text-teal-500" />
          User Profile
        </h1>
        <p className="text-sm text-slateText-500 mt-1">
          Manage your personal details and institutional identity.
        </p>
      </div>

      {/* Alerts */}
      {successMessage && (
        <div className="flex items-center gap-2.5 p-3.5 bg-green-50 border border-green-200 rounded-input text-green-700 text-sm">
          <Check className="w-4 h-4 shrink-0" />
          {successMessage}
        </div>
      )}
      {error && (
        <div className="flex items-center gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-input text-red-600 text-sm">
          <AlertCircle className="w-4 h-4 shrink-0" />
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* ─── Left: Overview card ─── */}
        <Card className="p-6 flex flex-col items-center text-center gap-4">
          {/* Avatar */}
          <div className="w-20 h-20 rounded-2xl bg-teal-500 text-white text-2xl font-bold flex items-center justify-center shadow-sm">
            {initials}
          </div>
          <div>
            <p className="font-bold text-slateText-900 text-base">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="text-xs text-slateText-400 font-mono mt-0.5">{user?.email}</p>
          </div>
          <div className="flex flex-wrap justify-center gap-2">
            <Badge variant="teal">{user?.roles?.[0] || 'STUDENT'}</Badge>
            <Badge variant="success">{user?.status}</Badge>
          </div>

          <div className="w-full border-t border-surface-200 pt-4 space-y-2.5 text-left text-xs text-slateText-600">
            <div className="flex items-center gap-2">
              <Building2 className="w-3.5 h-3.5 text-slateText-300 shrink-0" />
              <span>{user?.tenant?.name}</span>
            </div>
            <div className="flex items-center gap-2">
              <MapPin className="w-3.5 h-3.5 text-slateText-300 shrink-0" />
              <span>{user?.location || 'Location not set'}</span>
            </div>
            <div className="flex items-center gap-2">
              <Globe className="w-3.5 h-3.5 text-slateText-300 shrink-0" />
              <span>Language: {user?.preferredLanguage?.toUpperCase()}</span>
            </div>
          </div>
        </Card>

        {/* ─── Right: Edit form ─── */}
        <Card className="md:col-span-2 p-6">
          <h2 className="text-sm font-semibold text-slateText-900 border-b border-surface-200 pb-3 mb-5">
            Edit Information
          </h2>
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input label="First Name" name="firstName" value={formData.firstName} onChange={handleChange} required />
              <Input label="Last Name"  name="lastName"  value={formData.lastName}  onChange={handleChange} required />
            </div>

            <div>
              <label className={labelCls}>Biography</label>
              <textarea
                name="bio"
                rows={3}
                placeholder="Share your skills, goals, or interests…"
                value={formData.bio}
                onChange={handleChange}
                className="w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 placeholder-slateText-400 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500 resize-none"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Location / Campus"
                name="location"
                placeholder="e.g. Hyderabad, India"
                value={formData.location}
                onChange={handleChange}
              />
              <div>
                <label className={labelCls}>Preferred Language</label>
                <select name="preferredLanguage" value={formData.preferredLanguage} onChange={handleChange} className={selectCls}>
                  <option value="en">English</option>
                  <option value="es">Spanish</option>
                  <option value="fr">French</option>
                  <option value="hi">Hindi</option>
                  <option value="de">German</option>
                </select>
              </div>
            </div>

            <div className="pt-2 flex justify-end">
              <Button type="submit" isLoading={isLoading}>Save Changes</Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
};
