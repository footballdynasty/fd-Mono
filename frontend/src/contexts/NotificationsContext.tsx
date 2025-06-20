import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationApi, inboxApi } from '../services/api';
import { Notification, NotificationStats, NotificationType } from '../types';
import { useAuth } from '../hooks/useAuth';
import * as Sentry from '@sentry/react';

interface NotificationsContextType {
  // Data
  notifications: Notification[];
  stats: NotificationStats | null;
  inboxCount: number;
  isLoading: boolean;
  error: string | null;
  
  // Actions
  markAsRead: (notificationId: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  deleteNotification: (notificationId: string) => Promise<void>;
  refreshNotifications: () => void;
  refreshInboxCount: () => void;
  
  // Admin actions
  approveAchievementRequest: (requestId: string, adminNotes?: string) => Promise<void>;
  rejectAchievementRequest: (requestId: string, adminNotes?: string) => Promise<void>;
}

const NotificationsContext = createContext<NotificationsContextType | undefined>(undefined);

interface NotificationsProviderProps {
  children: ReactNode;
}

export const NotificationsProvider: React.FC<NotificationsProviderProps> = ({ children }) => {
  const { isAuthenticated, user, isCommissioner } = useAuth();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  // Get user notifications
  const {
    data: notificationsData,
    isLoading: notificationsLoading,
    refetch: refetchNotifications,
  } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationApi.getAll({ unreadOnly: false, limit: 50 }),
    enabled: isAuthenticated,
    refetchInterval: 30000, // Refetch every 30 seconds
    select: (data) => data.data,
  });

  // Get achievement requests for admin users
  const {
    data: achievementRequestsData,
    isLoading: requestsLoading,
    refetch: refetchRequests,
  } = useQuery({
    queryKey: ['admin-achievement-requests'],
    queryFn: () => inboxApi.getPendingRequests(),
    enabled: isAuthenticated && isCommissioner,
    refetchInterval: 30000, // Refetch every 30 seconds
    select: (data) => data.data,
  });

  // Get inbox count for navbar badge
  const {
    data: inboxCountData,
    isLoading: inboxCountLoading,
    refetch: refetchInboxCount,
  } = useQuery({
    queryKey: ['inbox-count'],
    queryFn: () => notificationApi.getInboxCount(),
    enabled: isAuthenticated,
    refetchInterval: 15000, // Refetch every 15 seconds
    select: (data) => data.data,
  });

  // Mark notification as read mutation
  const markAsReadMutation = useMutation({
    mutationFn: (notificationId: string) => notificationApi.markAsRead(notificationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['inbox-count'] });
    },
    onError: (error) => {
      console.error('Failed to mark notification as read:', error);
      setError('Failed to mark notification as read');
      Sentry.captureException(error, { tags: { section: 'notifications' } });
    },
  });

  // Mark all notifications as read mutation
  const markAllAsReadMutation = useMutation({
    mutationFn: () => notificationApi.markAllAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['inbox-count'] });
    },
    onError: (error) => {
      console.error('Failed to mark all notifications as read:', error);
      setError('Failed to mark all notifications as read');
      Sentry.captureException(error, { tags: { section: 'notifications' } });
    },
  });

  // Delete notification mutation
  const deleteNotificationMutation = useMutation({
    mutationFn: (notificationId: string) => notificationApi.delete(notificationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['inbox-count'] });
    },
    onError: (error) => {
      console.error('Failed to delete notification:', error);
      setError('Failed to delete notification');
      Sentry.captureException(error, { tags: { section: 'notifications' } });
    },
  });

  // Approve achievement request mutation (admin only)
  const approveRequestMutation = useMutation({
    mutationFn: ({ requestId, adminNotes }: { requestId: string; adminNotes?: string }) =>
      inboxApi.approveRequest(requestId, adminNotes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['inbox-count'] });
      queryClient.invalidateQueries({ queryKey: ['admin-achievement-requests'] });
      queryClient.invalidateQueries({ queryKey: ['achievements'] });
    },
    onError: (error) => {
      console.error('Failed to approve achievement request:', error);
      setError('Failed to approve achievement request');
      Sentry.captureException(error, { tags: { section: 'admin-inbox' } });
    },
  });

  // Reject achievement request mutation (admin only)
  const rejectRequestMutation = useMutation({
    mutationFn: ({ requestId, adminNotes }: { requestId: string; adminNotes?: string }) =>
      inboxApi.rejectRequest(requestId, adminNotes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['inbox-count'] });
      queryClient.invalidateQueries({ queryKey: ['admin-achievement-requests'] });
    },
    onError: (error) => {
      console.error('Failed to reject achievement request:', error);
      setError('Failed to reject achievement request');
      Sentry.captureException(error, { tags: { section: 'admin-inbox' } });
    },
  });

  // Clear error after 5 seconds
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [error]);

  // Combine notifications and achievement requests for admin users
  const combinedNotifications = React.useMemo(() => {
    const baseNotifications = notificationsData?.notifications || [];
    
    if (!isCommissioner || !achievementRequestsData?.requests) {
      return baseNotifications;
    }

    // Convert achievement requests to notification format
    const requestNotifications = achievementRequestsData.requests.map((request: any) => ({
      id: request.id,
      type: NotificationType.ACHIEVEMENT_REQUEST,
      title: 'Achievement Request',
      message: `${request.userDisplayName} requested completion of "${request.achievementDescription}"`,
      isRead: false,
      createdAt: request.createdAt,
      data: {
        requestId: request.id,
        achievementId: request.achievementId,
        achievementName: request.achievementDescription,
        userId: request.userId,
        userName: request.userDisplayName,
        teamName: request.teamName,
        url: '/admin/inbox'
      }
    }));

    // Combine and sort by creation date
    return [...baseNotifications, ...requestNotifications].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }, [notificationsData?.notifications, achievementRequestsData?.requests, isCommissioner]);

  const contextValue: NotificationsContextType = {
    // Data
    notifications: combinedNotifications,
    stats: notificationsData?.stats || null,
    inboxCount: inboxCountData?.total || 0,
    isLoading: notificationsLoading || inboxCountLoading || requestsLoading,
    error,
    
    // Actions
    markAsRead: async (notificationId: string) => {
      await markAsReadMutation.mutateAsync(notificationId);
    },
    markAllAsRead: async () => {
      await markAllAsReadMutation.mutateAsync();
    },
    deleteNotification: async (notificationId: string) => {
      await deleteNotificationMutation.mutateAsync(notificationId);
    },
    refreshNotifications: () => {
      refetchNotifications();
      if (isCommissioner) {
        refetchRequests();
      }
    },
    refreshInboxCount: () => {
      refetchInboxCount();
    },
    
    // Admin actions
    approveAchievementRequest: async (requestId: string, adminNotes?: string) => {
      if (!isCommissioner) {
        throw new Error('Only administrators can approve achievement requests');
      }
      await approveRequestMutation.mutateAsync({ requestId, adminNotes });
    },
    rejectAchievementRequest: async (requestId: string, adminNotes?: string) => {
      if (!isCommissioner) {
        throw new Error('Only administrators can reject achievement requests');
      }
      await rejectRequestMutation.mutateAsync({ requestId, adminNotes });
    },
  };

  return (
    <NotificationsContext.Provider value={contextValue}>
      {children}
    </NotificationsContext.Provider>
  );
};

export const useNotifications = (): NotificationsContextType => {
  const context = useContext(NotificationsContext);
  if (context === undefined) {
    throw new Error('useNotifications must be used within a NotificationsProvider');
  }
  return context;
};