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
          {
            path: '/dashboard',
            element: <DashboardPage />,
          },
          {
            path: '/skills',
            element: <SkillsPage />,
          },
          {
            path: '/skills/explore',
            element: <ExploreSkillsPage />,
          },
          {
            path: '/learning-goals',
            element: <LearningGoalsPage />,
          },
          {
            path: '/users/:id',
            element: <UserProfilePage />,
          },
          {
            path: '/profile',
            element: <ProfilePage />,
          },
          {
            path: '/settings',
            element: <ProfilePage />,
          },
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
