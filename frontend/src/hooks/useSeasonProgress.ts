import { useQuery } from '@tanstack/react-query';
import { weekApi } from '../services/api';

export const useSeasonProgress = () => {
  return useQuery({
    queryKey: ['season-progress'],
    queryFn: async () => {
      const response = await weekApi.getCurrent();
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // Consider data fresh for 5 minutes
    gcTime: 10 * 60 * 1000, // Keep in cache for 10 minutes
    retry: 3,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });
};