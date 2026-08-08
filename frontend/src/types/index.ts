export interface Tenant {
  id: string;
  name: string;
  slug: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';
  createdAt: string;
}

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  bio?: string;
  location?: string;
  preferredLanguage: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'PENDING_VERIFICATION';
  roles: string[];
  tenant: Tenant;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
  user: User;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  path: string;
  errors?: Record<string, string>;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  tenantSlug: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  bio?: string;
  location?: string;
  preferredLanguage?: string;
}

// ── Phase 2 Types ──────────────────────────────────────────────

export type SkillLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
export type GoalStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
export type ExchangeStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';

export interface SkillCategory {
  id: string;
  name: string;
  description?: string;
  parentId?: string;
  children?: SkillCategory[];
}

export interface Skill {
  id: string;
  name: string;
  description?: string;
  categoryId: string;
  categoryName: string;
  isGlobal: boolean;
  tags?: string;
  prerequisites?: Skill[];
}

export interface ExploreSkill {
  id: string;
  name: string;
  description?: string;
  categoryId: string;
  categoryName: string;
  teacherCount: number;
  learnerCount: number;
}

export interface UserSkill {
  id: string;
  userId: string;
  skillId: string;
  skillName: string;
  categoryName: string;
  level: SkillLevel;
  canTeach: boolean;
  wantToLearn: boolean;
  yearsExperience?: number;
}

export interface AddUserSkillRequest {
  skillId: string;
  level: SkillLevel;
  canTeach: boolean;
  wantToLearn: boolean;
  yearsExperience?: number;
}

export interface LearningGoal {
  id: string;
  userId: string;
  targetSkillId: string;
  targetSkillName: string;
  goalText?: string;
  currentLevel?: SkillLevel;
  targetLevel?: SkillLevel;
  deadline?: string;
  learningPreferences?: string;
  status: GoalStatus;
}

export interface CreateLearningGoalRequest {
  targetSkillId: string;
  goalText?: string;
  currentLevel?: SkillLevel;
  targetLevel?: SkillLevel;
  deadline?: string;
  learningPreferences?: string;
}

export interface ExchangeRequest {
  id: string;
  requesterId: string;
  requesterName: string;
  receiverId: string;
  receiverName: string;
  offeredSkillId: string;
  offeredSkillName: string;
  wantedSkillId: string;
  wantedSkillName: string;
  message?: string;
  status: ExchangeStatus;
  createdAt: string;
}

export interface CreateExchangeRequest {
  receiverId: string;
  offeredSkillId: string;
  wantedSkillId: string;
  message?: string;
}

export interface PublicUserProfile {
  user: User;
  skillsTeaching: UserSkill[];
  skillsLearning: UserSkill[];
  learningGoals: LearningGoal[];
}

export interface RecommendedMatch {
  matchedUser: PublicUserProfile;
  matchedSkillName: string;
  matchReason: string;
}

export interface DashboardSummary {
  mySkills: UserSkill[];
  myLearningGoals: LearningGoal[];
  recommendedMatches: RecommendedMatch[];
  pendingRequests: ExchangeRequest[];
}
