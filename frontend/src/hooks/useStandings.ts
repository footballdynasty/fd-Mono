import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { standingsApi } from '../services/api';
import type { StandingCreateRequest, StandingUpdateRequest } from '../types';

// Query Keys
export const standingsQueryKeys = {
  all: ['standings'] as const,
  lists: () => [...standingsQueryKeys.all, 'list'] as const,
  list: (filters: { year?: number; conference?: string; page?: number; size?: number }) => 
    [...standingsQueryKeys.lists(), filters] as const,
  detail: (id: string) => [...standingsQueryKeys.all, 'detail', id] as const,
  team: (teamId: string, year?: number) => 
    [...standingsQueryKeys.all, 'team', teamId, ...(year ? [year] : [])] as const,
  conference: (conference: string, year: number) => 
    [...standingsQueryKeys.all, 'conference', conference, year] as const,
  ranked: (year: number, limit?: number) => 
    [...standingsQueryKeys.all, 'ranked', year, ...(limit ? [limit] : [])] as const,
  votes: (year: number) => [...standingsQueryKeys.all, 'votes', year] as const,
};

// Query Hooks

/**
 * Hook for fetching standings with filtering and pagination
 */
export const useStandings = (params?: { 
  year?: number; 
  conference?: string; 
  page?: number; 
  size?: number 
}) => {
  return useQuery({
    queryKey: standingsQueryKeys.list(params || {}),
    queryFn: async () => {
      const response = await standingsApi.getStandings(params);
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
};

/**
 * Hook for fetching a specific standing by ID
 */
export const useStanding = (id: string) => {
  return useQuery({
    queryKey: standingsQueryKeys.detail(id),
    queryFn: async () => {
      const response = await standingsApi.getById(id);
      return response.data;
    },
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for fetching team-specific standings
 */
export const useTeamStanding = (teamId: string, year?: number) => {
  return useQuery({
    queryKey: standingsQueryKeys.team(teamId, year),
    queryFn: async () => {
      const response = await standingsApi.getTeamStanding(teamId, year);
      return response.data;
    },
    enabled: !!teamId,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for fetching conference standings
 */
export const useConferenceStandings = (conference: string, year: number) => {
  return useQuery({
    queryKey: standingsQueryKeys.conference(conference, year),
    queryFn: async () => {
      const response = await standingsApi.getByConference(conference, year);
      return response.data;
    },
    enabled: !!conference && !!year,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for fetching ranked teams
 */
export const useRankedStandings = (year: number, limit?: number) => {
  return useQuery({
    queryKey: standingsQueryKeys.ranked(year, limit),
    queryFn: async () => {
      const response = await standingsApi.getRanked(year, limit);
      return response.data;
    },
    enabled: !!year,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for fetching teams receiving votes
 */
export const useReceivingVotesStandings = (year: number) => {
  return useQuery({
    queryKey: standingsQueryKeys.votes(year),
    queryFn: async () => {
      const response = await standingsApi.getReceivingVotes(year);
      return response.data;
    },
    enabled: !!year,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

// Mutation Hooks

/**
 * Hook for creating a new standing
 */
export const useCreateStanding = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: StandingCreateRequest) => {
      const response = await standingsApi.create(data);
      return response.data;
    },
    onSuccess: (newStanding) => {
      // Invalidate and refetch standings queries
      queryClient.invalidateQueries({ queryKey: standingsQueryKeys.all });
      
      // Optimistically update team-specific queries
      if (newStanding?.team?.id) {
        queryClient.invalidateQueries({ 
          queryKey: standingsQueryKeys.team(newStanding.team.id) 
        });
      }
      
      // Invalidate conference standings
      if (newStanding?.team?.conference && newStanding?.year) {
        queryClient.invalidateQueries({ 
          queryKey: standingsQueryKeys.conference(newStanding.team.conference, newStanding.year) 
        });
      }
    },
    onError: (error) => {
      console.error('Failed to create standing:', error);
    },
  });
};

/**
 * Hook for updating an existing standing
 */
export const useUpdateStanding = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: { id: string; data: StandingUpdateRequest }) => {
      const response = await standingsApi.update(id, data);
      return response.data;
    },
    onSuccess: (updatedStanding, { id }) => {
      // Update the specific standing in cache
      queryClient.setQueryData(standingsQueryKeys.detail(id), updatedStanding);
      
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: standingsQueryKeys.lists() });
      
      if (updatedStanding?.team?.id) {
        queryClient.invalidateQueries({ 
          queryKey: standingsQueryKeys.team(updatedStanding.team.id) 
        });
      }
      
      if (updatedStanding?.team?.conference && updatedStanding?.year) {
        queryClient.invalidateQueries({ 
          queryKey: standingsQueryKeys.conference(updatedStanding.team.conference, updatedStanding.year) 
        });
      }
    },
    onError: (error) => {
      console.error('Failed to update standing:', error);
    },
  });
};

/**
 * Hook for deleting a standing
 */
export const useDeleteStanding = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await standingsApi.delete(id);
      return id;
    },
    onSuccess: (deletedId) => {
      // Remove from cache and invalidate queries
      queryClient.removeQueries({ queryKey: standingsQueryKeys.detail(deletedId) });
      queryClient.invalidateQueries({ queryKey: standingsQueryKeys.all });
    },
    onError: (error) => {
      console.error('Failed to delete standing:', error);
    },
  });
};

/**
 * Hook for calculating standings for a year
 */
export const useCalculateStandings = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (year: number) => {
      const response = await standingsApi.calculateStandings(year);
      return response.data;
    },
    onSuccess: (_, year) => {
      // Invalidate all standings queries for the calculated year
      queryClient.invalidateQueries({ 
        queryKey: standingsQueryKeys.all,
        predicate: (query) => {
          const key = query.queryKey;
          return key.includes(year) || key.includes('list');
        }
      });
    },
    onError: (error) => {
      console.error('Failed to calculate standings:', error);
    },
  });
};

/**
 * Hook for calculating conference standings
 */
export const useCalculateConferenceStandings = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ conference, year }: { conference: string; year: number }) => {
      const response = await standingsApi.calculateConferenceStandings(conference, year);
      return response.data;
    },
    onSuccess: (_, { conference, year }) => {
      // Invalidate specific conference standings
      queryClient.invalidateQueries({ 
        queryKey: standingsQueryKeys.conference(conference, year) 
      });
      
      // Invalidate general standings queries that might include this conference
      queryClient.invalidateQueries({ 
        queryKey: standingsQueryKeys.lists() 
      });
    },
    onError: (error) => {
      console.error('Failed to calculate conference standings:', error);
    },
  });
};