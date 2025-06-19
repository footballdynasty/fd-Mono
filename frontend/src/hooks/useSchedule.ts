import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { gameApi, weekApi } from '../services/api';
import type { Game } from '../types';

// Query Keys
export const scheduleQueryKeys = {
  all: ['schedule'] as const,
  games: () => [...scheduleQueryKeys.all, 'games'] as const,
  gamesList: (filters: { page?: number; size?: number; year?: number }) => 
    [...scheduleQueryKeys.games(), 'list', filters] as const,
  gamesByWeek: (weekId: string) => 
    [...scheduleQueryKeys.games(), 'week', weekId] as const,
  gamesByTeam: (teamId: string, year?: number) => 
    [...scheduleQueryKeys.games(), 'team', teamId, ...(year ? [year] : [])] as const,
  upcomingGames: (teamId?: string) => 
    [...scheduleQueryKeys.games(), 'upcoming', ...(teamId ? [teamId] : [])] as const,
  recentGames: (teamId?: string, limit?: number) => 
    [...scheduleQueryKeys.games(), 'recent', ...(teamId ? [teamId] : []), ...(limit ? [limit] : [])] as const,
  
  weeks: () => [...scheduleQueryKeys.all, 'weeks'] as const,
  currentWeek: () => [...scheduleQueryKeys.weeks(), 'current'] as const,
  weeksByYear: (year: number) => [...scheduleQueryKeys.weeks(), 'year', year] as const,
  specificWeek: (year: number, weekNumber: number) => 
    [...scheduleQueryKeys.weeks(), 'specific', year, weekNumber] as const,
};

// Game Query Hooks

/**
 * Hook for fetching games with filtering and pagination
 */
export const useGames = (params?: { 
  page?: number; 
  size?: number; 
  year?: number 
}) => {
  return useQuery({
    queryKey: scheduleQueryKeys.gamesList(params || {}),
    queryFn: async () => {
      const response = await gameApi.getAll(params);
      return response.data;
    },
    staleTime: 2 * 60 * 1000, // 2 minutes (shorter for live games)
    gcTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Hook for fetching games by week
 */
export const useGamesByWeek = (weekId: string) => {
  return useQuery({
    queryKey: scheduleQueryKeys.gamesByWeek(weekId),
    queryFn: async () => {
      const response = await gameApi.getByWeek(weekId);
      return response.data;
    },
    enabled: !!weekId,
    staleTime: 2 * 60 * 1000, // 2 minutes
    gcTime: 5 * 60 * 1000,
    refetchInterval: 30 * 1000, // 30 seconds - refetch for live updates
  });
};

/**
 * Hook for fetching games by team
 */
export const useGamesByTeam = (teamId: string, year?: number) => {
  return useQuery({
    queryKey: scheduleQueryKeys.gamesByTeam(teamId, year),
    queryFn: async () => {
      const response = await gameApi.getByTeam(teamId, year);
      return response.data;
    },
    enabled: !!teamId,
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for fetching upcoming games
 */
export const useUpcomingGames = (teamId?: string) => {
  return useQuery({
    queryKey: scheduleQueryKeys.upcomingGames(teamId),
    queryFn: async () => {
      const response = await gameApi.getUpcoming(teamId);
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for fetching recent games
 */
export const useRecentGames = (teamId?: string, limit?: number) => {
  return useQuery({
    queryKey: scheduleQueryKeys.recentGames(teamId, limit),
    queryFn: async () => {
      const response = await gameApi.getRecent(teamId, limit);
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000,
  });
};

// Week Query Hooks

/**
 * Hook for fetching current week information
 */
export const useCurrentWeek = () => {
  return useQuery({
    queryKey: scheduleQueryKeys.currentWeek(),
    queryFn: async () => {
      const response = await weekApi.getCurrent();
      return response.data;
    },
    staleTime: 30 * 60 * 1000, // 30 minutes (weeks don't change often)
    gcTime: 60 * 60 * 1000, // 1 hour
  });
};

/**
 * Hook for fetching all weeks for a year
 */
export const useWeeksByYear = (year: number) => {
  return useQuery({
    queryKey: scheduleQueryKeys.weeksByYear(year),
    queryFn: async () => {
      const response = await weekApi.getByYear(year);
      return response.data;
    },
    enabled: !!year,
    staleTime: 60 * 60 * 1000, // 1 hour
    gcTime: 2 * 60 * 60 * 1000, // 2 hours
  });
};

/**
 * Hook for fetching a specific week
 */
export const useSpecificWeek = (year: number, weekNumber: number) => {
  return useQuery({
    queryKey: scheduleQueryKeys.specificWeek(year, weekNumber),
    queryFn: async () => {
      const response = await weekApi.getWeek(year, weekNumber);
      return response.data;
    },
    enabled: !!year && !!weekNumber,
    staleTime: 30 * 60 * 1000, // 30 minutes
    gcTime: 60 * 60 * 1000, // 1 hour
  });
};

// Combined Hooks for Schedule Page

/**
 * Hook for schedule page data - combines week info and games
 */
export const useScheduleData = (params: {
  year: number;
  weekNumber?: number | null;
  teamId?: string;
  teamView: 'all' | 'selected';
}) => {
  const { year, weekNumber, teamId, teamView } = params;
  
  // Get current week if no specific week is provided
  const { data: currentWeekData } = useCurrentWeek();
  const { data: weeksData } = useWeeksByYear(year);
  
  // Determine the effective weekId based on weekNumber or current week
  const effectiveWeekId = (() => {
    if (weekNumber && weeksData?.weeks) {
      // Find the weekId for the specified week number
      const weekInfo = weeksData.weeks.find((w: any) => w.weekNumber === weekNumber);
      return weekInfo?.id;
    }
    // Use current week by default
    return currentWeekData?.weekId;
  })();
  
  // Call all hooks unconditionally (Rules of Hooks requirement)
  const teamGamesQuery = useGamesByTeam(teamId || '', year);
  const weekGamesQuery = useGamesByWeek(effectiveWeekId || '');
  const allGamesQuery = useGames({ year, size: 100 });
  
  // Select the appropriate query based on view type and apply additional filtering
  let selectedQuery;
  let filteredData;
  
  if (teamView === 'selected' && teamId) {
    // For team view, start with team games but filter by week if specified
    selectedQuery = teamGamesQuery;
    filteredData = selectedQuery.data;
    
    // If a specific week is selected, filter team games to only that week
    if (weekNumber && filteredData) {
      const games = Array.isArray(filteredData) ? filteredData : (filteredData as any).content;
      if (Array.isArray(games)) {
        filteredData = games.filter((game: any) => game.weekNumber === weekNumber);
      }
    }
  } else if (effectiveWeekId) {
    selectedQuery = weekGamesQuery;
    filteredData = selectedQuery.data;
  } else {
    selectedQuery = allGamesQuery;
    filteredData = selectedQuery.data;
  }
  
  // Debug logging
  if (process.env.NODE_ENV === 'development') {
    const getDataLength = (data: any) => {
      if (!data) return 0;
      if (Array.isArray(data)) return data.length;
      if (data.content && Array.isArray(data.content)) return data.content.length;
      return 0;
    };

    console.log('Schedule Data Debug:', {
      teamView,
      teamId,
      weekNumber,
      effectiveWeekId,
      hasCurrentWeek: !!currentWeekData,
      hasWeeksData: !!weeksData,
      originalDataLength: getDataLength(selectedQuery.data),
      filteredDataLength: getDataLength(filteredData),
      queryType: teamView === 'selected' && teamId ? 'team' : effectiveWeekId ? 'week' : 'all'
    });
  }

  // Extract games data - handle both Game[] and PaginatedResponse<Game>
  const extractGames = (data: any): Game[] | undefined => {
    if (!data) return undefined;
    if (Array.isArray(data)) return data;
    if (data.content && Array.isArray(data.content)) return data.content;
    return undefined;
  };

  return {
    data: extractGames(filteredData),
    isLoading: selectedQuery.isLoading,
    error: selectedQuery.error,
    currentWeek: currentWeekData,
    effectiveWeekId,
  };
};

/**
 * Hook for week navigation
 */
export const useWeekNavigation = (year: number) => {
  const { data: currentWeekData } = useCurrentWeek();
  const { data: weeksData } = useWeeksByYear(year);
  
  const currentWeekNumber = currentWeekData?.currentWeek || 1;
  const totalWeeks = currentWeekData?.totalWeeks || weeksData?.totalWeeks || 15;
  
  const navigationHelpers = {
    canGoPrevious: currentWeekNumber > 1,
    canGoNext: currentWeekNumber < totalWeeks,
    getCurrentWeekId: () => currentWeekData?.weekId,
    getWeekOptions: () => {
      if (!weeksData?.weeks) return [];
      return weeksData.weeks.map((week: any) => ({
        label: `Week ${week.weekNumber}`,
        value: week.id,
        weekNumber: week.weekNumber,
        isCurrentWeek: week.weekNumber === currentWeekNumber,
      }));
    },
  };
  
  return {
    currentWeekNumber,
    totalWeeks,
    currentWeekData,
    weeksData,
    ...navigationHelpers,
    isLoading: !currentWeekData || !weeksData,
  };
};

// Mutation Hooks

/**
 * Hook for updating game scores
 */
export const useUpdateGameScore = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ 
      gameId, 
      homeScore, 
      awayScore 
    }: { 
      gameId: string; 
      homeScore: number; 
      awayScore: number; 
    }) => {
      const response = await gameApi.updateScore(gameId, homeScore, awayScore);
      return response.data;
    },
    onSuccess: (updatedGame) => {
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: scheduleQueryKeys.games() });
      
      // If the game has team info, invalidate team-specific queries
      if (updatedGame.homeTeamId) {
        queryClient.invalidateQueries({ 
          queryKey: scheduleQueryKeys.gamesByTeam(updatedGame.homeTeamId) 
        });
      }
      if (updatedGame.awayTeamId) {
        queryClient.invalidateQueries({ 
          queryKey: scheduleQueryKeys.gamesByTeam(updatedGame.awayTeamId) 
        });
      }
      
      // Invalidate week-specific queries
      if (updatedGame.weekId) {
        queryClient.invalidateQueries({ 
          queryKey: scheduleQueryKeys.gamesByWeek(updatedGame.weekId) 
        });
      }
    },
    onError: (error) => {
      console.error('Failed to update game score:', error);
    },
  });
};