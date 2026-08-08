import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '../../lib/auth';
import { Bell, User as UserIcon, LogOut, ChevronDown, Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const initials = `${user?.firstName?.[0] ?? ''}${user?.lastName?.[0] ?? ''}`;

  return (
    <header className="h-14 bg-white border-b border-surface-200 px-6 flex items-center justify-between sticky top-0 z-30">
      {/* Left: Search bar */}
      <div className="relative hidden sm:block">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slateText-400 pointer-events-none" />
        <input
          type="text"
          placeholder="Search skills, users..."
          className="pl-9 pr-4 py-1.5 text-sm bg-surface-50 border border-surface-200 rounded-btn text-slateText-900 placeholder-slateText-400 focus:outline-none focus:ring-1 focus:ring-teal-500 focus:border-teal-500 w-56"
        />
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-2 ml-auto">
        {/* Notification Bell */}
        <button
          className="relative p-2 text-slateText-400 hover:text-slateText-700 hover:bg-surface-100 rounded-btn transition-colors"
          title="Notifications"
        >
          <Bell className="w-4 h-4" />
          <span className="absolute top-1.5 right-1.5 w-1.5 h-1.5 bg-teal-500 rounded-full" />
        </button>

        <div className="h-5 w-px bg-surface-200 mx-1" />

        {/* User Dropdown */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="flex items-center gap-2.5 px-2 py-1.5 rounded-btn hover:bg-surface-50 transition-colors"
          >
            <div className="w-7 h-7 rounded-full bg-teal-500 text-white flex items-center justify-center text-xs font-bold shrink-0">
              {initials}
            </div>
            <div className="text-left hidden md:block">
              <div className="text-sm font-semibold text-slateText-900 leading-tight">
                {user?.firstName} {user?.lastName}
              </div>
              <div className="text-[11px] text-slateText-400 leading-tight">
                {user?.tenant?.name}
              </div>
            </div>
            <ChevronDown className={`w-3.5 h-3.5 text-slateText-400 transition-transform duration-150 ${dropdownOpen ? 'rotate-180' : ''}`} />
          </button>

          {dropdownOpen && (
            <div className="absolute right-0 mt-1.5 w-56 bg-white border border-surface-200 rounded-panel shadow-lg py-1 z-50">
              <div className="px-4 py-3 border-b border-surface-200">
                <p className="text-sm font-semibold text-slateText-900">
                  {user?.firstName} {user?.lastName}
                </p>
                <p className="text-xs text-slateText-400 truncate mt-0.5">{user?.email}</p>
              </div>
              <button
                onClick={() => { setDropdownOpen(false); navigate('/profile'); }}
                className="w-full text-left px-4 py-2 text-sm text-slateText-600 hover:bg-surface-50 flex items-center gap-2.5"
              >
                <UserIcon className="w-4 h-4 text-slateText-400" />
                My Profile
              </button>
              <div className="border-t border-surface-200 mt-1 pt-1">
                <button
                  onClick={handleLogout}
                  className="w-full text-left px-4 py-2 text-sm text-red-500 hover:bg-red-50 flex items-center gap-2.5"
                >
                  <LogOut className="w-4 h-4" />
                  Sign Out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
