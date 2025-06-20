import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { achievementApi } from '../services/api';
import { Achievement, AchievementType, AchievementRarity, PaginatedResponse, AchievementCompletionResponse } from '../types';
import { useAuth } from './useAuth';

interface UseAchievementsParams {
  page?: number;
  size?: number;
  type?: AchievementType;
  rarity?: AchievementRarity;
  completed?: boolean;
}

interface UseAchievementsReturn {
  data: PaginatedResponse<Achievement> | undefined;
  achievements: Achievement[];
  isLoading: boolean;
  isError: boolean;
  error: Error | null;
  refetch: () => void;
}

/**
 * Hook for fetching paginated achievements with filters
 */
export const useAchievements = (params: UseAchievementsParams = {}): UseAchievementsReturn => {
  const {
    data,
    isLoading,
    isError,
    error,
    refetch,
  } = useQuery({
    queryKey: ['achievements', params],
    queryFn: async () => {
      const response = await achievementApi.getAll(params);
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  return {
    data,
    achievements: data?.content || [],
    isLoading,
    isError,
    error,
    refetch,
  };
};

/**
 * Hook for fetching a single achievement by ID
 */
export const useAchievement = (id: string) => {
  return useQuery({
    queryKey: ['achievement', id],
    queryFn: async () => {
      const response = await achievementApi.getById(id);
      return response.data;
    },
    enabled: !!id,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};

/**
 * Hook for fetching achievements by type
 */
export const useAchievementsByType = (type: AchievementType) => {
  return useQuery({
    queryKey: ['achievements', 'type', type],
    queryFn: async () => {
      const response = await achievementApi.getByType(type);
      return response.data;
    },
    enabled: !!type,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Hook for fetching achievement statistics
 */
export const useAchievementStats = () => {
  return useQuery({
    queryKey: ['achievements', 'stats'],
    queryFn: async () => {
      const response = await achievementApi.getAll({ page: 0, size: 1000 }); // Get all for stats
      const achievements = response.data.content;
      
      const totalAchievements = achievements.length;
      const completedAchievements = achievements.filter(a => a.isCompleted).length;
      const completionPercentage = totalAchievements > 0 ? (completedAchievements / totalAchievements) * 100 : 0;
      
      // Count by type
      const countByType: Record<string, number> = {};
      Object.values(AchievementType).forEach(type => {
        countByType[type] = achievements.filter(a => a.type === type).length;
      });
      
      // Count by rarity
      const countByRarity: Record<string, number> = {};
      Object.values(AchievementRarity).forEach(rarity => {
        countByRarity[rarity] = achievements.filter(a => a.rarity === rarity).length;
      });
      
      // Recent achievements (completed in last 30 days)
      const thirtyDaysAgo = Date.now() - (30 * 24 * 60 * 60 * 1000);
      const recentAchievements = achievements.filter(a => 
        a.isCompleted && a.dateCompleted && a.dateCompleted > thirtyDaysAgo
      );
      
      return {
        totalAchievements,
        completedAchievements,
        completionPercentage,
        countByType,
        countByRarity,
        recentAchievements,
        achievements,
      };
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

/**
 * Hook for completing an achievement
 */
export const useCompleteAchievement = () => {
  const queryClient = useQueryClient();
  const { user, selectedTeam, isCommissioner } = useAuth();
  
  return useMutation<AchievementCompletionResponse, Error, string>({
    mutationFn: async (achievementId: string) => {
      const userContext = {
        userId: user?.id || 'guest-user',
        userDisplayName: user?.username || 'Guest User',
        teamId: selectedTeam?.id || '',
        teamName: selectedTeam?.name || '',
        isAdmin: isCommissioner, // Use real commissioner permission
        requestReason: 'Achievement completed'
      };
      
      const response = await achievementApi.complete(achievementId, userContext);
      return response.data;
    },
    onSuccess: (responseData) => {
      // Check if we have an achievement object (completed) or just response data (pending)
      if (responseData.achievement) {
        // Update the achievement in all relevant queries for completed achievements
        queryClient.setQueryData(['achievement', responseData.achievement.id], responseData.achievement);
      }
      
      // Invalidate and refetch achievements list regardless of status
      queryClient.invalidateQueries({ queryKey: ['achievements'] });
      
      // Return the full response so the component can handle success messaging properly
    },
    onError: (error) => {
      console.error('Failed to complete achievement:', error);
    },
  });
};

/**
 * Hook for creating a new achievement (admin)
 */
export const useCreateAchievement = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async (achievementData: Omit<Achievement, 'id'>) => {
      const response = await achievementApi.create(achievementData);
      return response.data;
    },
    onSuccess: () => {
      // Invalidate and refetch achievements list
      queryClient.invalidateQueries({ queryKey: ['achievements'] });
    },
    onError: (error) => {
      console.error('Failed to create achievement:', error);
    },
  });
};

/**
 * Hook for updating an achievement (admin)
 */
export const useUpdateAchievement = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async ({ id, data }: { id: string; data: Partial<Achievement> }) => {
      const response = await achievementApi.update(id, data);
      return response.data;
    },
    onSuccess: (updatedAchievement) => {
      // Update the achievement in the cache
      queryClient.setQueryData(['achievement', updatedAchievement.id], updatedAchievement);
      
      // Invalidate and refetch achievements list
      queryClient.invalidateQueries({ queryKey: ['achievements'] });
    },
    onError: (error) => {
      console.error('Failed to update achievement:', error);
    },
  });
};

/**
 * Hook for deleting an achievement (admin)
 */
export const useDeleteAchievement = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async (achievementId: string) => {
      await achievementApi.delete(achievementId);
      return achievementId;
    },
    onSuccess: (deletedId) => {
      // Remove the achievement from the cache
      queryClient.removeQueries({ queryKey: ['achievement', deletedId] });
      
      // Invalidate and refetch achievements list
      queryClient.invalidateQueries({ queryKey: ['achievements'] });
    },
    onError: (error) => {
      console.error('Failed to delete achievement:', error);
    },
  });
};