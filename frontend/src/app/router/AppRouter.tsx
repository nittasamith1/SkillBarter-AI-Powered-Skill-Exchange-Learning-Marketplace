import React from 'react';
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom';
import { ProtectedRoute } from '../../components/common/ProtectedRoute';
import { AppLayout } from '../../components/layout/AppLayout';
import { LoginPage } from '../../features/auth/pages/LoginPage';
import { RegisterPage } from '../../features/auth/pages/RegisterPage';
import { DashboardPage } from '../../features/dashboard/pages/DashboardPage';
import { ProfilePage } from '../../features/profile/pages/ProfilePage';
import { SkillsPage } from '../../features/skills/pages/SkillsPage';
import { ExploreSkillsPage } from '../../features/skills/pages/ExploreSkillsPage';
import { LearningGoalsPage } from '../../features/goals/pages/LearningGoalsPage';
import { UserProfilePage } from '../../features/marketplace/pages/UserProfilePage';
// Phase 3
import { MatchesPage } from '../../features/matches/pages/MatchesPage';
import { AvailabilityPage } from '../../features/availability/pages/AvailabilityPage';
import { SessionsPage } from '../../features/sessions/pages/SessionsPage';
import { SessionDetailPage } from '../../features/sessions/pages/SessionDetailPage';
import { CreditsPage } from '../../features/credits/pages/CreditsPage';
import { ReputationPage } from '../../features/reputation/pages/ReputationPage';
import { DisputesPage } from '../../features/disputes/pages/DisputesPage';

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          // Phase 1 & 2
          { path: '/dashboard',       element: <DashboardPage /> },
          { path: '/skills',          element: <SkillsPage /> },
          { path: '/skills/explore',  element: <ExploreSkillsPage /> },
          { path: '/learning-goals',  element: <LearningGoalsPage /> },
          { path: '/users/:id',       element: <UserProfilePage /> },
          { path: '/profile',         element: <ProfilePage /> },
          { path: '/settings',        element: <ProfilePage /> },
          // Phase 3 — Exchange Engine
          { path: '/matches',              element: <MatchesPage /> },
          { path: '/availability',         element: <AvailabilityPage /> },
          { path: '/sessions',             element: <SessionsPage /> },
          { path: '/sessions/:id',         element: <SessionDetailPage /> },
          { path: '/credits',              element: <CreditsPage /> },
          { path: '/reputation',           element: <ReputationPage /> },
          { path: '/disputes',             element: <DisputesPage /> },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/dashboard" replace />,
  },
]);

export const AppRouter: React.FC = () => {
  return <RouterProvider router={router} />;
};
