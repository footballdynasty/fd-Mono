export interface Team {
  id: string;
  name: string;
  coach?: string;
  username?: string;
  conference?: string;
  isHuman?: boolean;
  imageUrl?: string;
  createdAt?: string;
  updatedAt?: string;
  currentWins?: number;
  currentLosses?: number;
  winPercentage?: number;
  currentRank?: number;
  totalGames?: number;
}

export interface Game {
  id: string;
  gameId: string;
  homeTeamId: string;
  homeTeamName: string;
  homeTeamImageUrl?: string;
  awayTeamId: string;
  awayTeamName: string;
  awayTeamImageUrl?: string;
  homeScore: number;
  awayScore: number;
  date: string;
  weekId?: string;
  weekNumber?: number;
  year?: number;
  homeTeamRank?: number;
  awayTeamRank?: number;
  status: GameStatus;
  createdAt?: string;
  updatedAt?: string;
  statusDisplay?: string;
  scoreDisplay?: string;
  isCompleted?: boolean;
  winnerName?: string;
}

export enum GameStatus {
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS', 
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface Standing {
  id: string;
  team: Team;
  year: number;
  wins: number;
  losses: number;
  conference_wins: number;
  conference_losses: number;
  rank?: number;
  conference_rank?: number;
  receiving_votes: number;
  created_at: string;
  updated_at: string;
  // Calculated fields
  win_percentage: number;
  total_games: number;
  conference_win_percentage: number;
  total_conference_games: number;
}

export interface StandingCreateRequest {
  team_id: string;
  year: number;
  wins?: number;
  losses?: number;
  conference_wins?: number;
  conference_losses?: number;
  rank?: number;
  conference_rank?: number;
  receiving_votes?: number;
}

export interface StandingUpdateRequest {
  wins?: number;
  losses?: number;
  conference_wins?: number;
  conference_losses?: number;
  rank?: number;
  conference_rank?: number;
  receiving_votes?: number;
}

export interface AchievementReward {
  id: string;
  type: RewardType;
  traitName?: string;
  boostAmount: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
  displayName?: string;
  description?: string;
  category?: string;
}

export enum RewardType {
  TRAIT_BOOST = 'TRAIT_BOOST',
  GAME_RESTART = 'GAME_RESTART',
}

export interface Achievement {
  id: string;
  description: string;
  reward: string;
  dateCompleted?: number;
  type: AchievementType;
  rarity: AchievementRarity;
  icon?: string;
  color?: string;
  isCompleted: boolean;
  isPending?: boolean;
  pendingRequestId?: string;
  createdAt?: string;
  updatedAt?: string;
  rewards?: AchievementReward[];
}

export interface AchievementRequest {
  id: string;
  achievementId: string;
  achievementDescription: string;
  userId: string;
  userDisplayName: string;
  teamId?: string;
  teamName?: string;
  requestReason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  reviewedAt?: string;
  reviewedBy?: string;
}

export interface AchievementCompletionResponse {
  achievement?: Achievement;
  requestId?: string;
  status: 'completed' | 'pending';
  message: string;
  timestamp: number;
}

export enum AchievementType {
  WINS = 'WINS',
  SEASON = 'SEASON', 
  CHAMPIONSHIP = 'CHAMPIONSHIP',
  STATISTICS = 'STATISTICS',
  GENERAL = 'GENERAL',
}

export enum AchievementRarity {
  COMMON = 'COMMON',
  UNCOMMON = 'UNCOMMON',
  RARE = 'RARE',
  EPIC = 'EPIC',
  LEGENDARY = 'LEGENDARY',
}

export interface Week {
  id: string;
  year: number;
  weekNumber: number;
  games?: Game[];
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export enum Role {
  USER = 'USER',
  COMMISSIONER = 'COMMISSIONER'
}

export interface User {
  id: string;
  username: string;
  email?: string;
  selectedTeamId?: string;
  selectedTeam?: Team;
  roles?: Role[];
  isActive?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthContextType {
  user: User | null;
  selectedTeam: Team | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isCommissioner: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string, email?: string) => Promise<void>;
  logout: () => void;
  selectTeam: (team: Team) => Promise<void>;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email?: string;
  password: string;
}

export interface AuthResponse {
  user: User;
  token: string;
  selectedTeam?: Team;
}

// Notification System Types
export enum NotificationType {
  ACHIEVEMENT_REQUEST = 'ACHIEVEMENT_REQUEST',
  ACHIEVEMENT_COMPLETED = 'ACHIEVEMENT_COMPLETED', 
  ACHIEVEMENT_APPROVED = 'ACHIEVEMENT_APPROVED',
  ACHIEVEMENT_REJECTED = 'ACHIEVEMENT_REJECTED',
  GENERAL = 'GENERAL',
}

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  data?: {
    achievementId?: string;
    achievementName?: string;
    requestId?: string;
    userId?: string;
    userName?: string;
    teamName?: string;
    action?: string;
    url?: string;
  };
}

export interface NotificationStats {
  total: number;
  unread: number;
  byType: Record<NotificationType, number>;
}