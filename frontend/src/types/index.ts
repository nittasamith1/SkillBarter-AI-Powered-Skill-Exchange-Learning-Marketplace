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

// ── Phase 3 Types ──────────────────────────────────────────────

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
export type SessionStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW' | 'DISPUTED';
export type DisputeStatus = 'OPEN' | 'UNDER_REVIEW' | 'RESOLVED' | 'REJECTED';
export type NotificationType =
  | 'EXCHANGE_REQUEST_RECEIVED' | 'EXCHANGE_REQUEST_ACCEPTED' | 'EXCHANGE_REQUEST_REJECTED'
  | 'SESSION_SCHEDULED' | 'SESSION_REMINDER' | 'SESSION_CANCELLED' | 'SESSION_COMPLETED'
  | 'CREDIT_EARNED' | 'CREDIT_SPENT' | 'REVIEW_REQUESTED' | 'REVIEW_RECEIVED' | 'DISPUTE_CREATED' | 'DISPUTE_RESOLVED';

export interface UserAvailability {
  id: string;
  userId: string;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  timezone: string;
  active: boolean;
}

export interface CreateAvailabilityRequest {
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  timezone?: string;
}

export interface OverlapSlot {
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  timezone: string;
}

export interface MatchScore {
  skillCompatibility: number;
  goalAlignment: number;
  availabilityOverlap: number;
  proficiencyBalance: number;
  trustScore: number;
}

export interface MatchCandidateSkill {
  skillId: string;
  skillName: string;
  level: SkillLevel;
}

export interface MatchCandidate {
  candidateUserId: string;
  firstName: string;
  lastName: string;
  email: string;
  trustScore: number;
  creditBalance: number;
  availabilitySlotsCount: number;
  totalSessionsCompleted: number;
  canTeachSkills: MatchCandidateSkill[];
  wantToLearnSkills: MatchCandidateSkill[];
  scores: MatchScore;
}

export interface Session {
  id: string;
  tenantId: string;
  exchangeRequestId: string;
  teacherId: string;
  teacherName: string;
  learnerId: string;
  learnerName: string;
  skillId: string;
  skillName: string;
  scheduledStart: string;
  scheduledEnd: string;
  scheduledAt: string;         // alias: same as scheduledStart
  durationMinutes?: number;
  startedAt?: string;
  completedAt?: string;
  timezone: string;
  status: SessionStatus;
  meetingLink?: string;
  cancellationReason?: string;
  creditsSettled: boolean;
  createdAt: string;
}

export interface CreateSessionRequest {
  exchangeRequestId: string;
  scheduledStart: string;
  scheduledEnd: string;
  timezone?: string;
  meetingLink?: string;
}

export interface CreditWallet {
  id: string;
  userId: string;
  balance: number;
  updatedAt: string;
}

export interface CreditTransaction {
  id: string;
  userId: string;
  amount: number;
  balanceAfter: number;
  type: 'EARN' | 'SPEND' | 'REFUND' | 'PENALTY' | 'ADJUSTMENT' | 'INITIAL';
  referenceType?: string;
  referenceId?: string;
  description?: string;
  createdAt: string;
}

export interface Review {
  id: string;
  sessionId: string;
  reviewerId: string;
  reviewerName: string;
  revieweeId: string;
  revieweeName: string;
  rating: number;
  comment?: string;
  createdAt: string;
}

export interface TrustScore {
  userId: string;
  overallScore: number;          // mapped from trustScore backend field
  trustScore: number;            // raw backend field
  ratingScore: number;
  completionScore: number;
  reliabilityScore: number;
  responseScore: number;
  cancellationScore: number;
  cancellationPenalty: number;   // alias for cancellationScore
  averageRating: number;
  totalReviews: number;
  completedSessions: number;
  cancelledSessions: number;
  updatedAt: string;
  calculatedAt: string;
}

export interface NotificationItem {
  id: string;
  userId: string;
  type: NotificationType | string;
  title: string;
  message: string;
  read: boolean;
  referenceType?: string;
  referenceId?: string;
  createdAt: string;
}

export interface Dispute {
  id: string;
  sessionId: string;
  raisedBy: string;
  raisedByName: string;
  reason: string;
  description?: string;
  status: DisputeStatus;
  resolution?: string;
  createdAt: string;
  resolvedAt?: string;
}
