import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { teamApi } from '../services/api';
import type { Team } from '../types';

// Query Keys
export const teamsQueryKeys = {
  all: ['teams'] as const,
  lists: () => [...teamsQueryKeys.all, 'list'] as const,
  list: (filters: { search?: string; page?: number; size?: number }) => 
    [...teamsQueryKeys.lists(), filters] as const,
  detail: (id: string) => [...teamsQueryKeys.all, 'detail', id] as const,
  conference: (conference: string) => 
    [...teamsQueryKeys.all, 'conference', conference] as const,
  conferences: () => [...teamsQueryKeys.all, 'conferences'] as const,
  human: () => [...teamsQueryKeys.all, 'human'] as const,
};

// Query Hooks

/**
 * Hook for fetching teams with filtering and pagination
 */
export const useTeams = (params?: { 
  search?: string; 
  page?: number; 
  size?: number;
  conference?: string;
  humanOnly?: boolean;
}) => {
  return useQuery({
    queryKey: teamsQueryKeys.list(params || {}),
    queryFn: async () => {
      const startTime = performance.now();
      console.log('🚀 Starting teams API call with params:', params);
      
      let response;
      
      // Use specific endpoints for better filtering
      if (params?.humanOnly && params?.conference) {
        // First get human teams, then filter by conference client-side
        const humanResponse = await teamApi.getHumanTeams();
        const filteredTeams = humanResponse.data.filter(team => 
          team.conference === params.conference
        );
        // Create paginated response structure
        const start = (params.page || 0) * (params.size || 10);
        const end = start + (params.size || 10);
        const paginatedTeams = filteredTeams.slice(start, end);
        
        response = {
          data: {
            content: paginatedTeams,
            totalElements: filteredTeams.length,
            totalPages: Math.ceil(filteredTeams.length / (params.size || 10)),
            size: params.size || 10,
            number: params.page || 0,
          }
        };
      } else if (params?.humanOnly) {
        // Use human teams endpoint
        const humanResponse = await teamApi.getHumanTeams();
        // Apply search filter if provided
        let teams = humanResponse.data;
        if (params.search) {
          const searchTerm = params.search.toLowerCase();
          teams = teams.filter(team =>
            team.name.toLowerCase().includes(searchTerm) ||
            team.coach?.toLowerCase().includes(searchTerm) ||
            team.conference?.toLowerCase().includes(searchTerm)
          );
        }
        // Apply pagination
        const start = (params.page || 0) * (params.size || 10);
        const end = start + (params.size || 10);
        const paginatedTeams = teams.slice(start, end);
        
        response = {
          data: {
            content: paginatedTeams,
            totalElements: teams.length,
            totalPages: Math.ceil(teams.length / (params.size || 10)),
            size: params.size || 10,
            number: params.page || 0,
          }
        };
      } else if (params?.conference) {
        // Use conference endpoint
        const conferenceResponse = await teamApi.getByConference(params.conference);
        // Apply search filter if provided
        let teams = conferenceResponse.data;
        if (params.search) {
          const searchTerm = params.search.toLowerCase();
          teams = teams.filter(team =>
            team.name.toLowerCase().includes(searchTerm) ||
            team.coach?.toLowerCase().includes(searchTerm)
          );
        }
        // Apply pagination
        const start = (params.page || 0) * (params.size || 10);
        const end = start + (params.size || 10);
        const paginatedTeams = teams.slice(start, end);
        
        response = {
          data: {
            content: paginatedTeams,
            totalElements: teams.length,
            totalPages: Math.ceil(teams.length / (params.size || 10)),
            size: params.size || 10,
            number: params.page || 0,
          }
        };
      } else {
        // Use regular getAll endpoint
        response = await teamApi.getAll({
          search: params?.search,
          page: params?.page,
          size: params?.size,
        });
      }
      
      const endTime = performance.now();
      const duration = endTime - startTime;
      console.log(`⏱️ Teams API call completed in ${duration.toFixed(2)}ms`);
      console.log('📊 Response data:', {
        totalElements: response.data.totalElements,
        size: response.data.size,
        number: response.data.number,
        contentLength: response.data.content?.length
      });
      
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes
  });
};

/**
 * Hook for fetching a specific team by ID
 */
export const useTeam = (id: string) => {
  return useQuery({
    queryKey: teamsQueryKeys.detail(id),
    queryFn: async () => {
      const response = await teamApi.getById(id);
      return response.data;
    },
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for fetching teams by conference
 */
export const useTeamsByConference = (conference: string) => {
  return useQuery({
    queryKey: teamsQueryKeys.conference(conference),
    queryFn: async () => {
      const response = await teamApi.getByConference(conference);
      return response.data;
    },
    enabled: !!conference,
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

/**
 * Hook for fetching all conferences
 */
export const useConferences = () => {
  return useQuery({
    queryKey: teamsQueryKeys.conferences(),
    queryFn: async () => {
      const response = await teamApi.getConferences();
      return response.data;
    },
    staleTime: 10 * 60 * 1000, // 10 minutes - conferences don't change often
    gcTime: 30 * 60 * 1000, // 30 minutes
  });
};

/**
 * Hook for fetching human-controlled teams
 */
export const useHumanTeams = () => {
  return useQuery({
    queryKey: teamsQueryKeys.human(),
    queryFn: async () => {
      const response = await teamApi.getHumanTeams();
      return response.data;
    },
    staleTime: 5 * 60 * 1000,
    gcTime: 10 * 60 * 1000,
  });
};

// Mutation Hooks

/**
 * Hook for creating a new team
 */
export const useCreateTeam = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: Omit<Team, 'id'>) => {
      const response = await teamApi.create(data);
      return response.data;
    },
    onSuccess: (newTeam) => {
      // Invalidate and refetch teams queries
      queryClient.invalidateQueries({ queryKey: teamsQueryKeys.all });
      
      // Invalidate conference-specific queries
      if (newTeam?.conference) {
        queryClient.invalidateQueries({ 
          queryKey: teamsQueryKeys.conference(newTeam.conference) 
        });
      }
      
      // Invalidate conferences list if new conference
      queryClient.invalidateQueries({ 
        queryKey: teamsQueryKeys.conferences() 
      });
      
      // Invalidate human teams if applicable
      if (newTeam?.isHuman) {
        queryClient.invalidateQueries({ 
          queryKey: teamsQueryKeys.human() 
        });
      }
    },
    onError: (error) => {
      console.error('Failed to create team:', error);
    },
  });
};

/**
 * Hook for updating an existing team
 */
export const useUpdateTeam = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: { id: string; data: Partial<Team> }) => {
      const response = await teamApi.update(id, data);
      return response.data;
    },
    onSuccess: (updatedTeam, { id }) => {
      // Update the specific team in cache
      queryClient.setQueryData(teamsQueryKeys.detail(id), updatedTeam);
      
      // Invalidate related queries
      queryClient.invalidateQueries({ queryKey: teamsQueryKeys.lists() });
      
      if (updatedTeam?.conference) {
        queryClient.invalidateQueries({ 
          queryKey: teamsQueryKeys.conference(updatedTeam.conference) 
        });
      }
      
      // Invalidate conferences list in case conference changed
      queryClient.invalidateQueries({ 
        queryKey: teamsQueryKeys.conferences() 
      });
      
      // Invalidate human teams if isHuman status changed
      queryClient.invalidateQueries({ 
        queryKey: teamsQueryKeys.human() 
      });
    },
    onError: (error) => {
      console.error('Failed to update team:', error);
    },
  });
};

/**
 * Hook for deleting a team
 */
export const useDeleteTeam = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await teamApi.delete(id);
      return id;
    },
    onSuccess: (deletedId) => {
      // Remove from cache and invalidate queries
      queryClient.removeQueries({ queryKey: teamsQueryKeys.detail(deletedId) });
      queryClient.invalidateQueries({ queryKey: teamsQueryKeys.all });
    },
    onError: (error) => {
      console.error('Failed to delete team:', error);
    },
  });
};