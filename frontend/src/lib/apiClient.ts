import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ApiResponse, AuthResponse } from '../types';

const API_BASE_URL = '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

// Request interceptor: attach Access Token
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('accessToken');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: handle 401 & token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/register')) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        isRefreshing = false;
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        const { data } = await axios.post<ApiResponse<AuthResponse>>(
          `${API_BASE_URL}/auth/refresh`,
          { refreshToken }
        );

        const newAccessToken = data.data.accessToken;
        const newRefreshToken = data.data.refreshToken;

        localStorage.setItem('accessToken', newAccessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        }

        processQueue(null, newAccessToken);
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

// ── Phase 2 API Functions ─────────────────────────────────────

// Skill Categories
export const getSkillCategories = async () => {
  const { data } = await apiClient.get('/skill-categories');
  return data.data;
};

// Skills
export const searchSkills = async (search?: string, categoryId?: string) => {
  const params: Record<string, string> = {};
  if (search) params.search = search;
  if (categoryId) params.categoryId = categoryId;
  const { data } = await apiClient.get('/skills', { params });
  return data.data;
};

export const exploreSkills = async (search?: string, categoryId?: string) => {
  const params: Record<string, string> = {};
  if (search) params.search = search;
  if (categoryId) params.categoryId = categoryId;
  const { data } = await apiClient.get('/skills/explore', { params });
  return data.data;
};

export const createSkill = async (req: { name: string; description?: string; categoryId: string; tags?: string[] }) => {
  const { data } = await apiClient.post('/skills', req);
  return data.data;
};

// User Skills
export const getMySkills = async () => {
  const { data } = await apiClient.get('/users/me/skills');
  return data.data;
};

export const addMySkill = async (req: { skillId: string; level: string; canTeach: boolean; wantToLearn: boolean; yearsExperience?: number }) => {
  const { data } = await apiClient.post('/users/me/skills', req);
  return data.data;
};

export const removeMySkill = async (skillId: string) => {
  const { data } = await apiClient.delete(`/users/me/skills/${skillId}`);
  return data.data;
};

// Learning Goals
export const getMyLearningGoals = async () => {
  const { data } = await apiClient.get('/users/me/learning-goals');
  return data.data;
};

export const createLearningGoal = async (req: { targetSkillId: string; goalText?: string; currentLevel?: string; targetLevel?: string; deadline?: string; learningPreferences?: string }) => {
  const { data } = await apiClient.post('/users/me/learning-goals', req);
  return data.data;
};

export const deleteLearningGoal = async (goalId: string) => {
  const { data } = await apiClient.delete(`/users/me/learning-goals/${goalId}`);
  return data.data;
};

// Marketplace & Users
export const searchMarketplaceUsers = async (query?: string, skillId?: string) => {
  const params: Record<string, string> = {};
  if (query) params.query = query;
  if (skillId) params.skillId = skillId;
  const { data } = await apiClient.get('/marketplace/users', { params });
  return data.data;
};

export const getPublicProfile = async (userId: string) => {
  const { data } = await apiClient.get(`/marketplace/users/${userId}`);
  return data.data;
};

// Exchange Requests
export const sendExchangeRequest = async (req: { receiverId: string; offeredSkillId: string; wantedSkillId: string; message?: string }) => {
  const { data } = await apiClient.post('/exchange-requests', req);
  return data.data;
};

export const getMyExchangeRequests = async () => {
  const { data } = await apiClient.get('/exchange-requests');
  return data.data;
};

export const respondToExchangeRequest = async (requestId: string, status: 'ACCEPTED' | 'REJECTED') => {
  const { data } = await apiClient.put(`/exchange-requests/${requestId}/respond`, { status });
  return data.data;
};

// Dashboard
export const getDashboardData = async () => {
  const { data } = await apiClient.get('/dashboard');
  return data.data;
};

