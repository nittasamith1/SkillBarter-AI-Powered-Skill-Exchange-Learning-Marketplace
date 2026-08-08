import React, { useState } from 'react';
import { useAuth } from '../../../lib/auth';
import { useNavigate, Link } from 'react-router-dom';
import { Input } from '../../../components/ui/Input';
import { Button } from '../../../components/ui/Button';
import { AlertCircle, ArrowRight, ShieldCheck, Zap, Users } from 'lucide-react';
import { AxiosError } from 'axios';
import { ApiError } from '../../../types';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await login({ email, password });
      navigate('/dashboard');
    } catch (err) {
      const axiosErr = err as AxiosError<ApiError>;
      setError(axiosErr.response?.data?.message || 'Invalid email or password.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* ─── Left panel: Deep Navy hero ─── */}
      <div className="hidden lg:flex lg:w-[45%] bg-brand-950 flex-col justify-between p-12 relative overflow-hidden">
        {/* Subtle radial glows */}
        <div className="absolute top-0 left-0 w-full h-full pointer-events-none">
          <div className="absolute top-1/3 -left-32 w-80 h-80 bg-teal-500/8 rounded-full blur-3xl" />
          <div className="absolute bottom-1/4 -right-20 w-72 h-72 bg-aiBlue/6 rounded-full blur-3xl" />
        </div>

        {/* Logo */}
        <div className="relative z-10 flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-teal-500 flex items-center justify-center">
            <svg width="20" height="20" viewBox="0 0 18 18" fill="none" className="text-white">
              <path d="M3 6l4-3 4 3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M7 3v10" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
              <path d="M15 12l-4 3-4-3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M11 15V5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
            </svg>
          </div>
          <span className="font-bold text-xl text-white tracking-tight">
            Skill<span className="text-teal-400">Barter</span>
          </span>
        </div>

        {/* Hero copy */}
        <div className="relative z-10 space-y-8">
          <div className="space-y-4">
            <h1 className="text-4xl font-bold text-white leading-tight tracking-tight">
              Exchange what you know.<br />
              <span className="text-teal-400">Learn what you need.</span>
            </h1>
            <p className="text-white/50 text-base leading-relaxed max-w-sm">
              Peer-to-peer skill exchange for your institution. Teach what you know, learn what you don't.
            </p>
          </div>

          <div className="space-y-3">
            {[
              { icon: ShieldCheck, text: 'Multi-tenant institution isolation' },
              { icon: Zap,         text: 'Stateless JWT · 15m access · 7d refresh' },
              { icon: Users,       text: 'AI-powered skill matching engine' },
            ].map(({ icon: Icon, text }) => (
              <div key={text} className="flex items-center gap-3 text-white/50 text-sm">
                <div className="w-7 h-7 rounded-full bg-teal-500/10 flex items-center justify-center shrink-0">
                  <Icon className="w-3.5 h-3.5 text-teal-400" />
                </div>
                {text}
              </div>
            ))}
          </div>
        </div>

        <p className="relative z-10 text-[11px] text-white/20 font-mono">
          © 2026 SkillBarter AI · Phase 2 · Core Marketplace
        </p>
      </div>

      {/* ─── Right panel: White form ─── */}
      <div className="flex-1 flex items-center justify-center p-8 bg-white">
        <div className="w-full max-w-sm space-y-8">
          {/* Mobile logo */}
          <div className="flex items-center gap-2 lg:hidden">
            <div className="w-8 h-8 rounded-lg bg-teal-500 flex items-center justify-center">
              <svg width="16" height="16" viewBox="0 0 18 18" fill="none" className="text-white">
                <path d="M3 6l4-3 4 3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M7 3v10" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
                <path d="M15 12l-4 3-4-3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M11 15V5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
              </svg>
            </div>
            <span className="font-bold text-lg text-slateText-900 tracking-tight">
              Skill<span className="text-teal-500">Barter</span>
            </span>
          </div>

          <div>
            <h2 className="text-2xl font-bold text-slateText-900 tracking-tight">Welcome back</h2>
            <p className="text-sm text-slateText-500 mt-1">Continue your learning journey.</p>
          </div>

          {error && (
            <div className="flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-input text-red-600 text-sm">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              label="Email"
              type="email"
              placeholder="you@institution.edu"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <div className="space-y-1.5">
              <Input
                label="Password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <div className="flex justify-end">
                <button type="button" className="text-xs text-teal-600 hover:text-teal-700 font-medium">
                  Forgot password?
                </button>
              </div>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full"
              isLoading={isLoading}
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Continue
            </Button>
          </form>

          <p className="text-center text-sm text-slateText-500">
            Don't have an account?{' '}
            <Link to="/register" className="text-teal-600 hover:text-teal-700 font-semibold">
              Create account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
