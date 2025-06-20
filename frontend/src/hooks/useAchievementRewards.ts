import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { achievementRewardApi } from '../services/api';
import { AchievementReward } from '../types';

export const useAchievementRewards = (achievementId: string | null) => {
  return useQuery({
    queryKey: ['achievement-rewards', achievementId],
    queryFn: () => achievementRewardApi.getByAchievementId(achievementId!),
    enabled: !!achievementId,
    select: (data) => data.data,
  });
};

export const useRewardStatistics = () => {
  return useQuery({
    queryKey: ['reward-statistics'],
    queryFn: () => achievementRewardApi.getStatistics(),
    select: (data) => data.data.statistics,
  });
};

export const useTraitOptions = () => {
  return useQuery({
    queryKey: ['trait-options'],
    queryFn: () => achievementRewardApi.getTraitOptions(),
    select: (data) => data.data.traitOptions,
  });
};

export const useInitializeRewards = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: () => achievementRewardApi.initialize(),
    onSuccess: () => {
      // Invalidate all achievement rewards queries
      queryClient.invalidateQueries({ queryKey: ['achievement-rewards'] });
      queryClient.invalidateQueries({ queryKey: ['reward-statistics'] });
    },
  });
};

export const useCreateReward = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ achievementId, reward }: { 
      achievementId: string; 
      reward: Omit<AchievementReward, 'id' | 'createdAt' | 'updatedAt'> 
    }) => 
      achievementRewardApi.create(achievementId, reward),
    onSuccess: (data, variables) => {
      // Invalidate specific achievement rewards
      queryClient.invalidateQueries({ 
        queryKey: ['achievement-rewards', variables.achievementId] 
      });
      queryClient.invalidateQueries({ queryKey: ['reward-statistics'] });
    },
  });
};

export const useUpdateReward = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ rewardId, reward }: { 
      rewardId: string; 
      reward: Partial<AchievementReward> 
    }) => 
      achievementRewardApi.update(rewardId, reward),
    onSuccess: () => {
      // Invalidate all achievement rewards queries since we don't know which achievement
      queryClient.invalidateQueries({ queryKey: ['achievement-rewards'] });
      queryClient.invalidateQueries({ queryKey: ['reward-statistics'] });
    },
  });
};

export const useDeleteReward = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (rewardId: string) => achievementRewardApi.delete(rewardId),
    onSuccess: () => {
      // Invalidate all achievement rewards queries since we don't know which achievement
      queryClient.invalidateQueries({ queryKey: ['achievement-rewards'] });
      queryClient.invalidateQueries({ queryKey: ['reward-statistics'] });
    },
  });
};