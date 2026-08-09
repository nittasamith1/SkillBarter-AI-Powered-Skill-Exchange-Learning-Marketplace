import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, User, Settings, Sparkles, Award, Compass, Target,
  Calendar, ArrowLeftRight, Coins, Star, ShieldAlert, Users2,
} from 'lucide-react';
import { clsx } from 'clsx';

const navGroups = [
  {
    label: 'Workspace',
    items: [
      { name: 'Dashboard',     path: '/dashboard',      icon: LayoutDashboard },
      { name: 'My Skills',     path: '/skills',         icon: Award },
      { name: 'Explore',       path: '/skills/explore', icon: Compass },
      { name: 'Learning Goals',path: '/learning-goals', icon: Target },
    ],
  },
  {
    label: 'Exchange Engine',
    items: [
      { name: 'Matches',      path: '/matches',      icon: Users2 },
      { name: 'Availability', path: '/availability', icon: Calendar },
      { name: 'Sessions',     path: '/sessions',     icon: ArrowLeftRight },
      { name: 'Credits',      path: '/credits',      icon: Coins },
      { name: 'Reputation',   path: '/reputation',   icon: Star },
      { name: 'Disputes',     path: '/disputes',     icon: ShieldAlert },
    ],
  },
  {
    label: 'Account',
    items: [
      { name: 'Profile',  path: '/profile',  icon: User },
      { name: 'Settings', path: '/settings', icon: Settings, disabled: true, tag: 'Soon' },
    ],
  },
];

export const Sidebar: React.FC = () => {
  return (
    <aside className="w-64 bg-brand-950 text-white flex flex-col h-screen sticky top-0 shrink-0">
      {/* Brand Header */}
      <div className="px-6 py-5 border-b border-white/5">
        <div className="flex items-center gap-3">
          {/* Logo mark: two overlapping arrows = exchange */}
          <div className="w-8 h-8 rounded-lg bg-teal-500 flex items-center justify-center shrink-0">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none" className="text-white">
              <path d="M3 6l4-3 4 3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M7 3v10" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
              <path d="M15 12l-4 3-4-3" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M11 15V5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
            </svg>
          </div>
          <div>
            <span className="font-bold text-[15px] tracking-tight leading-none">
              Skill<span className="text-teal-400">Barter</span>
            </span>
            <span className="block text-[10px] text-white/30 font-medium tracking-widest uppercase mt-0.5">AI Platform</span>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-6 overflow-y-auto">
        {navGroups.map((group) => (
          <div key={group.label}>
            <p className="text-[10px] font-semibold text-white/25 uppercase tracking-widest px-3 mb-1.5">
              {group.label}
            </p>
            <div className="space-y-0.5">
              {group.items.map((item) => {
                const Icon = item.icon;
                if (item.disabled) {
                  return (
                    <div
                      key={item.name}
                      className="flex items-center justify-between px-3 py-2 rounded-btn text-white/25 cursor-not-allowed text-sm"
                    >
                      <div className="flex items-center gap-3">
                        <Icon className="w-4 h-4" />
                        <span>{item.name}</span>
                      </div>
                      {item.tag && (
                        <span className="text-[9px] bg-white/5 text-white/30 px-1.5 py-0.5 rounded font-mono uppercase tracking-wider">
                          {item.tag}
                        </span>
                      )}
                    </div>
                  );
                }

                return (
                  <NavLink
                    key={item.name}
                    to={item.path}
                    className={({ isActive }) =>
                      clsx(
                        'flex items-center gap-3 px-3 py-2 rounded-btn text-sm font-medium transition-colors duration-150',
                        isActive
                          ? 'bg-teal-500/12 text-white border-l-2 border-teal-400 pl-[10px]'
                          : 'text-white/50 hover:text-white hover:bg-white/5'
                      )
                    }
                  >
                    {({ isActive }) => (
                      <>
                        <Icon className={clsx('w-4 h-4 shrink-0', isActive ? 'text-teal-400' : 'text-white/40')} />
                        <span>{item.name}</span>
                      </>
                    )}
                  </NavLink>
                );
              })}
            </div>
          </div>
        ))}
      </nav>

      {/* AI Phase Indicator */}
      <div className="px-4 py-4 border-t border-white/5">
        <div className="rounded-btn p-3 bg-teal-500/8 border border-teal-500/15">
          <div className="flex items-center gap-1.5 mb-1">
            <Sparkles className="w-3.5 h-3.5 text-teal-400" />
            <span className="text-[11px] font-semibold text-teal-300">Phase 3 · Active</span>
          </div>
          <p className="text-[11px] text-white/30 leading-relaxed">
            Exchange Engine · Sessions · Credits · Reputation
          </p>
        </div>
      </div>
    </aside>
  );
};
