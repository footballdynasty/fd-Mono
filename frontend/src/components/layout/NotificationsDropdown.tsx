import React, { useState } from 'react';
import {
  IconButton,
  Badge,
  Menu,
  MenuItem,
  Typography,
  Box,
  Button,
  Divider,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  alpha,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Notifications,
  NotificationsNone,
  EmojiEvents,
  AdminPanelSettings,
  CheckCircle,
  Cancel,
  Delete,
  MarkEmailRead,
  Inbox,
  Visibility,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useNotifications } from '../../contexts/NotificationsContext';
import { useAuth } from '../../hooks/useAuth';
import { NotificationType } from '../../types';
import { formatDistanceToNow } from 'date-fns';
import { useNavigate } from 'react-router-dom';

const NotificationsDropdown: React.FC = () => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { isCommissioner } = useAuth();
  const navigate = useNavigate();
  const {
    notifications,
    inboxCount,
    isLoading,
    error,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    approveAchievementRequest,
    rejectAchievementRequest,
  } = useNotifications();

  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case NotificationType.ACHIEVEMENT_REQUEST:
        return <AdminPanelSettings sx={{ color: '#ff9800' }} />;
      case NotificationType.ACHIEVEMENT_COMPLETED:
        return <EmojiEvents sx={{ color: '#4caf50' }} />;
      case NotificationType.ACHIEVEMENT_APPROVED:
        return <CheckCircle sx={{ color: '#4caf50' }} />;
      case NotificationType.ACHIEVEMENT_REJECTED:
        return <Cancel sx={{ color: '#f44336' }} />;
      default:
        return <Notifications sx={{ color: '#2196f3' }} />;
    }
  };

  const getNotificationColor = (type: NotificationType) => {
    switch (type) {
      case NotificationType.ACHIEVEMENT_REQUEST:
        return '#ff9800';
      case NotificationType.ACHIEVEMENT_COMPLETED:
        return '#4caf50';
      case NotificationType.ACHIEVEMENT_APPROVED:
        return '#4caf50';
      case NotificationType.ACHIEVEMENT_REJECTED:
        return '#f44336';
      default:
        return '#2196f3';
    }
  };

  const handleNotificationClick = async (notification: any) => {
    // Mark as read if unread
    if (!notification.isRead) {
      await markAsRead(notification.id);
    }

    // Handle navigation based on notification type
    if (notification.data?.url) {
      navigate(notification.data.url);
    } else if (notification.type === NotificationType.ACHIEVEMENT_REQUEST && isCommissioner) {
      navigate('/admin/inbox');
    } else if (
      notification.type === NotificationType.ACHIEVEMENT_COMPLETED ||
      notification.type === NotificationType.ACHIEVEMENT_APPROVED ||
      notification.type === NotificationType.ACHIEVEMENT_REJECTED
    ) {
      navigate('/achievements');
    }

    handleClose();
  };

  const handleApproveRequest = async (requestId: string, event: React.MouseEvent) => {
    event.stopPropagation();
    try {
      await approveAchievementRequest(requestId, 'Approved via notification');
    } catch (error) {
      console.error('Failed to approve request:', error);
    }
  };

  const handleRejectRequest = async (requestId: string, event: React.MouseEvent) => {
    event.stopPropagation();
    try {
      await rejectAchievementRequest(requestId, 'Rejected via notification');
    } catch (error) {
      console.error('Failed to reject request:', error);
    }
  };

  const handleDeleteNotification = async (notificationId: string, event: React.MouseEvent) => {
    event.stopPropagation();
    try {
      await deleteNotification(notificationId);
    } catch (error) {
      console.error('Failed to delete notification:', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
    } catch (error) {
      console.error('Failed to mark all as read:', error);
    }
  };

  const formatTimeAgo = (dateString: string) => {
    try {
      return formatDistanceToNow(new Date(dateString), { addSuffix: true });
    } catch {
      return 'Unknown time';
    }
  };

  return (
    <>
      <motion.div
        whileHover={{ scale: 1.05 }}
        whileTap={{ scale: 0.95 }}
      >
        <IconButton
          color="inherit"
          onClick={handleClick}
          sx={{
            mr: 2,
            background: alpha('#ffffff', 0.1),
            '&:hover': {
              background: alpha('#ffffff', 0.2),
            },
          }}
        >
          <Badge 
            badgeContent={inboxCount > 0 ? inboxCount : undefined} 
            color="error"
            max={99}
          >
            {inboxCount > 0 ? <Notifications /> : <NotificationsNone />}
          </Badge>
        </IconButton>
      </motion.div>

      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        PaperProps={{
          sx: {
            width: 400,
            maxHeight: 600,
            background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
            backdropFilter: 'blur(20px)',
            border: `1px solid ${alpha('#ffffff', 0.2)}`,
            borderRadius: 3,
            mt: 1,
          },
        }}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        {/* Header */}
        <Box sx={{ p: 2, borderBottom: `1px solid ${alpha('#ffffff', 0.1)}` }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
            <Typography variant="h6" sx={{ fontWeight: 600, color: 'text.primary' }}>
              <Inbox sx={{ mr: 1, verticalAlign: 'middle' }} />
              Notifications
            </Typography>
            {notifications.length > 0 && (
              <Button
                size="small"
                startIcon={<MarkEmailRead />}
                onClick={handleMarkAllAsRead}
                sx={{
                  color: 'primary.main',
                  textTransform: 'none',
                  fontSize: '0.875rem',
                }}
              >
                Mark All Read
              </Button>
            )}
          </Box>
          {inboxCount > 0 && (
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              {inboxCount} new notification{inboxCount !== 1 ? 's' : ''}
            </Typography>
          )}
        </Box>

        {/* Loading State */}
        {isLoading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <CircularProgress size={24} />
          </Box>
        )}

        {/* Error State */}
        {error && (
          <Box sx={{ p: 2 }}>
            <Alert severity="error" sx={{ fontSize: '0.875rem' }}>
              {error}
            </Alert>
          </Box>
        )}

        {/* No Notifications */}
        {!isLoading && !error && notifications.length === 0 && (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <NotificationsNone sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>
              No notifications yet
            </Typography>
          </Box>
        )}

        {/* Notifications List */}
        {!isLoading && !error && notifications.length > 0 && (
          <Box sx={{ maxHeight: 400, overflow: 'auto' }}>
            {notifications.slice(0, 10).map((notification, index) => (
              <motion.div
                key={notification.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.2, delay: index * 0.05 }}
              >
                <ListItem
                    button
                    onClick={() => handleNotificationClick(notification)}
                    sx={{
                      borderBottom: `1px solid ${alpha('#ffffff', 0.05)}`,
                      '&:hover': {
                        background: alpha('#ffffff', 0.1),
                      },
                      opacity: notification.isRead ? 0.7 : 1,
                    }}
                  >
                    <ListItemIcon>
                      {getNotificationIcon(notification.type)}
                    </ListItemIcon>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                            {notification.title}
                          </Typography>
                          {!notification.isRead && (
                            <Chip
                              label="New"
                              size="small"
                              sx={{
                                height: 20,
                                fontSize: '0.75rem',
                                backgroundColor: getNotificationColor(notification.type),
                                color: 'white',
                              }}
                            />
                          )}
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Typography variant="body2" sx={{ color: 'text.secondary', mb: 0.5 }}>
                            {notification.message}
                          </Typography>
                          <Typography variant="caption" sx={{ color: 'text.disabled' }}>
                            {formatTimeAgo(notification.createdAt)}
                          </Typography>
                        </Box>
                      }
                    />
                    
                    {/* Action Buttons for Achievement Requests (Admin only) */}
                    {notification.type === NotificationType.ACHIEVEMENT_REQUEST && 
                     isCommissioner && 
                     notification.data?.requestId && (
                      <Box sx={{ display: 'flex', gap: 0.5, ml: 1 }}>
                        <IconButton
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate('/admin/inbox');
                            handleClose();
                          }}
                          sx={{
                            color: '#2196f3',
                            '&:hover': { backgroundColor: alpha('#2196f3', 0.1) },
                          }}
                          title="Review Request"
                        >
                          <Visibility fontSize="small" />
                        </IconButton>
                        <IconButton
                          size="small"
                          onClick={(e) => handleApproveRequest(notification.data?.requestId || '', e)}
                          sx={{
                            color: '#4caf50',
                            '&:hover': { backgroundColor: alpha('#4caf50', 0.1) },
                          }}
                          title="Approve Request"
                        >
                          <CheckCircle fontSize="small" />
                        </IconButton>
                        <IconButton
                          size="small"
                          onClick={(e) => handleRejectRequest(notification.data?.requestId || '', e)}
                          sx={{
                            color: '#f44336',
                            '&:hover': { backgroundColor: alpha('#f44336', 0.1) },
                          }}
                          title="Reject Request"
                        >
                          <Cancel fontSize="small" />
                        </IconButton>
                      </Box>
                    )}
                    
                    {/* Delete Button */}
                    <IconButton
                      size="small"
                      onClick={(e) => handleDeleteNotification(notification.id, e)}
                      sx={{
                        color: 'text.disabled',
                        '&:hover': { 
                          color: '#f44336',
                          backgroundColor: alpha('#f44336', 0.1),
                        },
                      }}
                    >
                      <Delete fontSize="small" />
                    </IconButton>
                  </ListItem>
                </motion.div>
              ))}
          </Box>
        )}

        {/* Footer Actions */}
        {notifications.length > 0 && (
          <>
            <Divider sx={{ borderColor: alpha('#ffffff', 0.1) }} />
            <Box sx={{ p: 2, textAlign: 'center' }}>
              <Button
                variant="text"
                onClick={() => {
                  navigate('/notifications');
                  handleClose();
                }}
                sx={{
                  color: 'primary.main',
                  textTransform: 'none',
                }}
              >
                View All Notifications
              </Button>
              {isCommissioner && (
                <Button
                  variant="text"
                  onClick={() => {
                    navigate('/admin/inbox');
                    handleClose();
                  }}
                  sx={{
                    ml: 2,
                    color: 'secondary.main',
                    textTransform: 'none',
                  }}
                >
                  Admin Inbox
                </Button>
              )}
            </Box>
          </>
        )}
      </Menu>
    </>
  );
};

export default NotificationsDropdown;