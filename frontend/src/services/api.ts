import axios from 'axios';
import * as Sentry from '@sentry/react';
import type { PaginatedResponse, Team, Game, Standing, Achievement, AchievementReward, AchievementCompletionResponse, LoginRequest, RegisterRequest, AuthResponse, StandingCreateRequest, StandingUpdateRequest, Notification, NotificationStats, AchievementRequest } from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v2';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    // Add auth token to requests
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle 401 errors by clearing auth data
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user');
      localStorage.removeItem('selected_team');
      window.location.reload();
    }

    // Report errors to Sentry for monitoring
    if (error.response?.status && error.response.status >= 500) {
      Sentry.captureException(error, {
        tags: {
          section: 'api',
          status: error.response.status,
        },
        extra: {
          url: error.config?.url,
          method: error.config?.method,
          response: error.response?.data,
        },
      });
    }

    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export const teamApi = {
  getAll: (params?: { search?: string; page?: number; size?: number }) =>
    api.get<PaginatedResponse<Team>>('/teams', { params }),
  
  getById: (id: string) =>
    api.get<Team>(`/teams/${id}`),
  
  getByConference: (conference: string) =>
    api.get<Team[]>(`/teams/conference/${conference}`),
  
  getConferences: () =>
    api.get<string[]>('/teams/conferences'),
  
  getHumanTeams: () =>
    api.get<Team[]>('/teams/human'),
  
  create: (team: Omit<Team, 'id'>) =>
    api.post<Team>('/teams', team),
  
  update: (id: string, team: Partial<Team>) =>
    api.put<Team>(`/teams/${id}`, team),
  
  delete: (id: string) =>
    api.delete(`/teams/${id}`),
};

export const gameApi = {
  getAll: (params?: { page?: number; size?: number; year?: number }) =>
    api.get<PaginatedResponse<Game>>('/games', { params }),
  
  getById: (id: string) =>
    api.get<Game>(`/games/${id}`),
  
  getByTeam: (teamId: string, year?: number) =>
    api.get<Game[]>(`/games/team/${teamId}`, { params: { year } }),
  
  getByWeek: (weekId: string) =>
    api.get<Game[]>(`/games/week/${weekId}`),
  
  getUpcoming: (teamId?: string) =>
    api.get<Game[]>('/games/upcoming', { params: { teamId } }),
  
  getRecent: (teamId?: string, limit?: number) =>
    api.get<Game[]>('/games/recent', { params: { teamId, limit } }),
  
  create: (game: Omit<Game, 'id'>) =>
    api.post<Game>('/games', game),
  
  update: (id: string, game: Partial<Game>) =>
    api.put<Game>(`/games/${id}`, game),
  
  updateScore: (id: string, homeScore: number, awayScore: number) =>
    api.patch<Game>(`/games/${id}/score`, { homeScore, awayScore }),
  
  delete: (id: string) =>
    api.delete(`/games/${id}`),
};

export const standingsApi = {
  // Get standings with flexible filtering options
  getStandings: (params?: { year?: number; conference?: string; page?: number; size?: number }) =>
    api.get<PaginatedResponse<Standing>>('/standings', { params }),
  
  // Get standing by ID
  getById: (id: string) =>
    api.get<Standing>(`/standings/${id}`),
  
  // Get team-specific standings
  getTeamStanding: (teamId: string, year?: number) => {
    if (year) {
      return api.get<Standing>(`/standings/team/${teamId}/year/${year}`);
    }
    return api.get<PaginatedResponse<Standing>>(`/standings/team/${teamId}`);
  },
  
  // Get conference standings for a specific year
  getByConference: (conference: string, year: number) =>
    api.get<Standing[]>(`/standings/conference/${conference}/year/${year}`),
  
  // Get top ranked teams
  getRanked: (year: number, limit?: number) =>
    api.get<Standing[]>(`/standings/ranked/year/${year}`, { params: { limit } }),
  
  // Get teams receiving votes
  getReceivingVotes: (year: number) =>
    api.get<Standing[]>(`/standings/votes/year/${year}`),
  
  // Calculate standings for a year
  calculateStandings: (year: number) =>
    api.post<{ message: string }>(`/standings/calculate/${year}`),
  
  // Calculate conference standings
  calculateConferenceStandings: (conference: string, year: number) =>
    api.post<{ message: string }>(`/standings/calculate/conference/${conference}/year/${year}`),
  
  // CRUD operations
  create: (standing: StandingCreateRequest) =>
    api.post<Standing>('/standings', standing),
  
  update: (id: string, standing: StandingUpdateRequest) =>
    api.put<Standing>(`/standings/${id}`, standing),
  
  delete: (id: string) =>
    api.delete(`/standings/${id}`),
};

export const conferenceStandingsApi = {
  getByConference: (conference: string, year: number) => 
    api.get(`/conference-standings/${conference}/${year}`),
  getAll: (year: number) => 
    api.get(`/conference-standings/all/${year}`),
  calculate: (year: number) => 
    api.post(`/conference-standings/calculate/${year}`),
};

export const conferenceChampionshipApi = {
  getChampionshipBid: (teamId: string, year: number) => 
    api.get(`/conference-championship/bid/${teamId}/${year}`),
};

export const achievementApi = {
  getAll: (params?: { page?: number; size?: number; type?: string; completed?: boolean }) =>
    api.get<PaginatedResponse<Achievement>>('/achievements', { params }),
  
  getById: (id: string) =>
    api.get<Achievement>(`/achievements/${id}`),
  
  getByType: (type: string) =>
    api.get<Achievement[]>(`/achievements/type/${type}`),
  
  create: (achievement: Omit<Achievement, 'id'>) =>
    api.post<Achievement>('/achievements', achievement),
  
  update: (id: string, achievement: Partial<Achievement>) =>
    api.put<Achievement>(`/achievements/${id}`, achievement),
  
  complete: (id: string, userContext?: { userId?: string; userDisplayName?: string; teamId?: string; teamName?: string; isAdmin?: boolean; requestReason?: string }) =>
    api.patch<AchievementCompletionResponse>(`/achievements/${id}/complete`, {
      userId: userContext?.userId || 'guest-user',
      userDisplayName: userContext?.userDisplayName || 'Guest User',
      teamId: userContext?.teamId || '',
      teamName: userContext?.teamName || '',
      isAdmin: userContext?.isAdmin || false,
      requestReason: userContext?.requestReason || 'Achievement completed'
    }),

  submitRequest: (achievementId: string, userContext?: { userId?: string; userDisplayName?: string; teamId?: string; teamName?: string; requestReason?: string }) =>
    api.post<AchievementCompletionResponse>('/achievements/submit-request', {
      achievementId,
      userId: userContext?.userId || 'guest-user',
      userDisplayName: userContext?.userDisplayName || 'Guest User',
      teamId: userContext?.teamId || '',
      teamName: userContext?.teamName || '',
      requestReason: userContext?.requestReason || 'Achievement completed'
    }),
  
  delete: (id: string) =>
    api.delete(`/achievements/${id}`),
};

export const achievementRewardApi = {
  getByAchievementId: (achievementId: string) =>
    api.get<{ rewards: AchievementReward[]; count: number; achievementId: string }>(`/admin/achievements/${achievementId}/rewards`),
  
  getStatistics: () =>
    api.get<{ statistics: any }>('/admin/rewards/statistics'),
  
  getTraitOptions: () =>
    api.get<{ traitOptions: any }>('/admin/rewards/trait-options'),
  
  initialize: () =>
    api.post<{ message: string }>('/admin/rewards/initialize'),
  
  create: (achievementId: string, reward: Omit<AchievementReward, 'id' | 'createdAt' | 'updatedAt'>) =>
    api.post<{ reward: AchievementReward; message: string }>(`/admin/achievements/${achievementId}/rewards`, reward),
  
  update: (rewardId: string, reward: Partial<AchievementReward>) =>
    api.put<{ reward: AchievementReward; message: string }>(`/admin/rewards/${rewardId}`, reward),
  
  delete: (rewardId: string) =>
    api.delete<{ message: string }>(`/admin/rewards/${rewardId}`),
};

export const weekApi = {
  getCurrent: () =>
    api.get<{
      year: number;
      currentWeek: number;
      totalWeeks: number;
      seasonProgress: number;
      weekId: string | null;
    }>('/weeks/current'),
  
  getByYear: (year: number) =>
    api.get<{
      year: number;
      weeks: any[];
      totalWeeks: number;
    }>(`/weeks/${year}`),
  
  getWeek: (year: number, weekNumber: number) =>
    api.get<{
      week: any;
      year: number;
      weekNumber: number;
    }>(`/weeks/${year}/${weekNumber}`),
};

export const authApi = {
  login: (credentials: LoginRequest) =>
    api.post<AuthResponse>('/auth/login', credentials),
  
  register: (userData: RegisterRequest) =>
    api.post<AuthResponse>('/auth/register', userData),
  
  logout: () =>
    api.post('/auth/logout'),
  
  getCurrentUser: () =>
    api.get('/auth/me'),
  
  refreshToken: () =>
    api.post('/auth/refresh'),
};

// Inbox API for admin achievement request management
export const inboxApi = {
  // Get pending achievement requests (admin only)
  getPendingRequests: () =>
    api.get<{
      requests: AchievementRequest[];
      count: number;
      timestamp: number;
    }>('/admin/inbox/requests'),
  
  // Approve achievement request (admin only)
  approveRequest: (requestId: string, adminNotes?: string) =>
    api.post<{ message: string; request: AchievementRequest }>(`/admin/inbox/requests/${requestId}/approve`, {
      adminNotes: adminNotes || ''
    }),
  
  // Reject achievement request (admin only)
  rejectRequest: (requestId: string, adminNotes?: string) =>
    api.post<{ message: string; request: AchievementRequest }>(`/admin/inbox/requests/${requestId}/reject`, {
      adminNotes: adminNotes || ''
    }),
  
  // Get inbox statistics (admin only)
  getStatistics: () =>
    api.get<{
      pendingRequests: number;
      approvedRequests: number;
      rejectedRequests: number;
      totalRequests: number;
      recentRequests: number;
    }>('/admin/inbox/statistics'),
};

// Notification API for real-time notifications
export const notificationApi = {
  // Get user notifications
  getAll: (params?: { unreadOnly?: boolean; limit?: number; page?: number }) =>
    api.get<{ notifications: Notification[]; stats: NotificationStats }>('/notifications', { params }),
  
  // Get notification statistics
  getStats: () =>
    api.get<NotificationStats>('/notifications/stats'),
  
  // Mark notification as read
  markAsRead: (notificationId: string) =>
    api.patch<{ message: string }>(`/notifications/${notificationId}/read`),
  
  // Mark all notifications as read
  markAllAsRead: () =>
    api.patch<{ message: string; count: number }>('/notifications/read-all'),
  
  // Delete notification
  delete: (notificationId: string) =>
    api.delete<{ message: string }>(`/notifications/${notificationId}`),
  
  // Get inbox count for navbar badge (combines notifications and admin requests)
  getInboxCount: () =>
    api.get<{ 
      notifications: number; 
      achievementRequests: number; 
      total: number; 
      isAdmin: boolean;
    }>('/notifications/inbox-count'),
};

export default api;