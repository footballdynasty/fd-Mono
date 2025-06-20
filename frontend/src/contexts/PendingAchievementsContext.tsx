import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

interface PendingAchievementsContextType {
  pendingRequests: Set<string>;
  addPendingRequest: (achievementId: string, requestId: string) => void;
  removePendingRequest: (achievementId: string) => void;
  isPending: (achievementId: string) => boolean;
  clearPendingRequests: () => void;
}

const PendingAchievementsContext = createContext<PendingAchievementsContextType | undefined>(undefined);

interface PendingAchievementsProviderProps {
  children: ReactNode;
}

const STORAGE_KEY = 'football-dynasty-pending-achievements';

export const PendingAchievementsProvider: React.FC<PendingAchievementsProviderProps> = ({ children }) => {
  const [pendingRequests, setPendingRequests] = useState<Set<string>>(new Set());

  // Load pending requests from localStorage on mount
  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const pendingArray = JSON.parse(stored);
        setPendingRequests(new Set(pendingArray));
      }
    } catch (error) {
      console.warn('Failed to load pending achievements from storage:', error);
    }
  }, []);

  // Save pending requests to localStorage whenever they change
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(Array.from(pendingRequests)));
    } catch (error) {
      console.warn('Failed to save pending achievements to storage:', error);
    }
  }, [pendingRequests]);

  const addPendingRequest = (achievementId: string, requestId: string) => {
    setPendingRequests(prev => new Set(prev).add(achievementId));
  };

  const removePendingRequest = (achievementId: string) => {
    setPendingRequests(prev => {
      const newSet = new Set(prev);
      newSet.delete(achievementId);
      return newSet;
    });
  };

  const isPending = (achievementId: string) => {
    return pendingRequests.has(achievementId);
  };

  const clearPendingRequests = () => {
    setPendingRequests(new Set());
  };

  const value = {
    pendingRequests,
    addPendingRequest,
    removePendingRequest,
    isPending,
    clearPendingRequests,
  };

  return (
    <PendingAchievementsContext.Provider value={value}>
      {children}
    </PendingAchievementsContext.Provider>
  );
};

export const usePendingAchievements = () => {
  const context = useContext(PendingAchievementsContext);
  if (context === undefined) {
    throw new Error('usePendingAchievements must be used within a PendingAchievementsProvider');
  }
  return context;
};