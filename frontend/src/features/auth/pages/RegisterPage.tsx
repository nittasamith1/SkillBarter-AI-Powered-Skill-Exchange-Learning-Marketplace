import React, { useState } from 'react';
import { useAuth } from '../../../lib/auth';
import { useNavigate, Link } from 'react-router-dom';
import { Input } from '../../../components/ui/Input';
import { Button } from '../../../components/ui/Button';
import { AlertCircle, ArrowRight } from 'lucide-react';
import { AxiosError } from 'axios';
import { ApiError } from '../../../types';

export const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    tenantSlug: 'skillbarter',
  });
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setFieldErrors({});
    setIsLoading(true);
    try {
      await register(formData);
      navigate('/dashboard');
    } catch (err) {
      const axiosErr = err as AxiosError<ApiError>;
      if (axiosErr.response?.data?.errors) setFieldErrors(axiosErr.response.data.errors);
      setError(axiosErr.response?.data?.message || 'Registration failed. Please check your information.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface-50 p-6">
      <div className="w-full max-w-lg bg-white border border-surface-200 rounded-panel shadow-sm p-8 space-y-6">
        {/* Logo */}
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-teal-500 flex items-center justify-center shrink-0">
            <svg width="16" height="16" viewBox="0 0 18 18" fill="none" className="text-white">
              <path d="M3 6l4-3 4 3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M7 3v10" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
              <path d="M15 12l-4 3-4-3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M11 15V5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
            </svg>
          </div>
          <span className="font-bold text-[15px] text-slateText-900 tracking-tight">
            Skill<span className="text-teal-500">Barter</span>
          </span>
        </div>

        <div>
          <h2 className="text-2xl font-bold text-slateText-900 tracking-tight">Create your account</h2>
          <p className="text-sm text-slateText-500 mt-1">Join your campus skill-exchange marketplace</p>
        </div>

        {error && (
          <div className="flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-input text-red-600 text-sm">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input label="First Name" name="firstName" placeholder="Samith"
              value={formData.firstName} onChange={handleChange} error={fieldErrors.firstName} required />
            <Input label="Last Name" name="lastName" placeholder="Nitta"
              value={formData.lastName} onChange={handleChange} error={fieldErrors.lastName} required />
          </div>

          <Input label="Email Address" name="email" type="email" placeholder="samith@university.edu"
            value={formData.email} onChange={handleChange} error={fieldErrors.email} required />

          <Input label="Password" name="password" type="password" placeholder="At least 8 characters"
            value={formData.password} onChange={handleChange} error={fieldErrors.password}
            helperText="Must be at least 8 characters" required />

          {/* Institution */}
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slateText-700 uppercase tracking-wider">
              Institution
            </label>
            <select
              name="tenantSlug"
              value={formData.tenantSlug}
              onChange={handleChange}
              className="w-full px-3.5 py-2.5 text-sm bg-white border border-surface-200 rounded-input text-slateText-900 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500"
            >
              <option value="skillbarter">SkillBarter Main Platform</option>
            </select>
          </div>

          <Button type="submit" variant="primary" size="lg" className="w-full"
            isLoading={isLoading} rightIcon={<ArrowRight className="w-4 h-4" />}>
            Create Account
          </Button>
        </form>

        <p className="text-center text-sm text-slateText-500 pt-2 border-t border-surface-200">
          Already have an account?{' '}
          <Link to="/login" className="text-teal-600 hover:text-teal-700 font-semibold">Sign in</Link>
        </p>
      </div>
    </div>
  );
};
