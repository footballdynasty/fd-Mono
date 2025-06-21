import React, { useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  Box,
  CardContent,
  Button,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  IconButton,
  Tooltip,
  Grid,
  CircularProgress,
} from '@mui/material';
import {
  CheckCircle,
  Cancel,
  Visibility,
  EmojiEvents,
  Person,
  Group,
  AccessTime,
  Close,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { formatDistanceToNow } from 'date-fns';
import { useQuery } from '@tanstack/react-query';
import { inboxApi } from '../services/api';
import { useNotifications } from '../contexts/NotificationsContext';
import { useAuth } from '../hooks/useAuth';
import { AchievementRequest } from '../types';
import GlassCard from '../components/ui/GlassCard';
import GradientButton from '../components/ui/GradientButton';

const AdminInboxPage: React.FC = () => {
  const { isCommissioner } = useAuth();
  const { approveAchievementRequest, rejectAchievementRequest } = useNotifications();
  const [selectedRequest, setSelectedRequest] = useState<AchievementRequest | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogType, setDialogType] = useState<'approve' | 'reject' | 'view'>('view');
  const [adminNotes, setAdminNotes] = useState('');
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Get pending achievement requests
  const {
    data: requestsData,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ['admin-inbox-requests'],
    queryFn: () => inboxApi.getPendingRequests(),
    enabled: isCommissioner,
    refetchInterval: 30000, // Refetch every 30 seconds
    select: (data) => data.data,
  });

  const requests = requestsData?.requests || [];

  const handleViewRequest = (request: AchievementRequest) => {
    setSelectedRequest(request);
    setDialogType('view');
    setDialogOpen(true);
    setAdminNotes('');
    setError(null);
  };

  const handleApproveRequest = (request: AchievementRequest) => {
    setSelectedRequest(request);
    setDialogType('approve');
    setDialogOpen(true);
    setAdminNotes('Approved via Admin Inbox');
    setError(null);
  };

  const handleRejectRequest = (request: AchievementRequest) => {
    setSelectedRequest(request);
    setDialogType('reject');
    setDialogOpen(true);
    setAdminNotes('');
    setError(null);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedRequest(null);
    setAdminNotes('');
    setError(null);
    setProcessing(false);
  };

  const handleConfirmAction = async () => {
    if (!selectedRequest) return;

    setProcessing(true);
    setError(null);

    try {
      if (dialogType === 'approve') {
        await approveAchievementRequest(selectedRequest.id, adminNotes);
      } else if (dialogType === 'reject') {
        await rejectAchievementRequest(selectedRequest.id, adminNotes);
      }
      
      // Refresh the requests list
      refetch();
      handleCloseDialog();
    } catch (err: any) {
      console.error('Failed to process request:', err);
      setError(err.message || 'Failed to process request');
    } finally {
      setProcessing(false);
    }
  };

  const formatTimeAgo = (dateString: string) => {
    try {
      return formatDistanceToNow(new Date(dateString), { addSuffix: true });
    } catch {
      return 'Unknown time';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return '#ff9800';
      case 'APPROVED':
        return '#4caf50';
      case 'REJECTED':
        return '#f44336';
      default:
        return '#2196f3';
    }
  };

  if (!isCommissioner) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">
          Access denied. This page is only available to administrators.
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <Box sx={{ mb: 4 }}>
          <Typography variant="h3" component="h1" sx={{ mb: 2, fontWeight: 700 }}>
            Admin Inbox
          </Typography>
          <Typography variant="h6" sx={{ color: 'text.secondary', mb: 3 }}>
            Review and manage achievement completion requests
          </Typography>

          {/* Summary Stats */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} md={4}>
              <GlassCard>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <EmojiEvents sx={{ fontSize: 40, color: '#ff9800' }} />
                    <Box>
                      <Typography variant="h4" sx={{ fontWeight: 600 }}>
                        {requests.length}
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                        Pending Requests
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </GlassCard>
            </Grid>
            <Grid item xs={12} md={4}>
              <GlassCard>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <AccessTime sx={{ fontSize: 40, color: '#2196f3' }} />
                    <Box>
                      <Typography variant="h4" sx={{ fontWeight: 600 }}>
                        {requestsData?.count || 0}
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                        Total Count
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </GlassCard>
            </Grid>
            <Grid item xs={12} md={4}>
              <GlassCard>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Group sx={{ fontSize: 40, color: '#4caf50' }} />
                    <Box>
                      <Typography variant="h4" sx={{ fontWeight: 600 }}>
                        {new Set(requests.map(r => r.userId)).size}
                      </Typography>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                        Unique Users
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </GlassCard>
            </Grid>
          </Grid>
        </Box>

        {/* Requests List */}
        {isLoading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : requests.length === 0 ? (
          <GlassCard>
            <CardContent sx={{ textAlign: 'center', py: 6 }}>
              <EmojiEvents sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" sx={{ mb: 1 }}>
                No pending requests
              </Typography>
              <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                All achievement requests have been processed.
              </Typography>
            </CardContent>
          </GlassCard>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {requests.map((request, index) => (
              <motion.div
                key={request.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, delay: index * 0.1 }}
              >
                <GlassCard>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <Box sx={{ flex: 1 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                          <EmojiEvents sx={{ color: '#ff9800' }} />
                          <Typography variant="h6" sx={{ fontWeight: 600 }}>
                            {request.achievementDescription}
                          </Typography>
                          <Chip
                            label={request.status}
                            size="small"
                            sx={{
                              backgroundColor: getStatusColor(request.status),
                              color: 'white',
                              fontWeight: 600,
                            }}
                          />
                        </Box>

                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, mb: 2 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Person sx={{ fontSize: 20, color: 'text.secondary' }} />
                            <Typography variant="body2">
                              <strong>{request.userDisplayName}</strong>
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Group sx={{ fontSize: 20, color: 'text.secondary' }} />
                            <Typography variant="body2">
                              {request.teamName}
                            </Typography>
                          </Box>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <AccessTime sx={{ fontSize: 20, color: 'text.secondary' }} />
                            <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                              {formatTimeAgo(request.createdAt)}
                            </Typography>
                          </Box>
                        </Box>

                        {request.requestReason && (
                          <Box sx={{ mt: 2 }}>
                            <Typography variant="body2" sx={{ color: 'text.secondary', mb: 1 }}>
                              <strong>Reason:</strong>
                            </Typography>
                            <Paper sx={{ p: 2, backgroundColor: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)' }}>
                              <Typography variant="body2">
                                {request.requestReason}
                              </Typography>
                            </Paper>
                          </Box>
                        )}
                      </Box>

                      <Box sx={{ display: 'flex', gap: 1, ml: 2 }}>
                        <Tooltip title="View Details">
                          <IconButton
                            onClick={() => handleViewRequest(request)}
                            sx={{ color: '#2196f3' }}
                          >
                            <Visibility />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Approve Request">
                          <IconButton
                            onClick={() => handleApproveRequest(request)}
                            sx={{ color: '#4caf50' }}
                          >
                            <CheckCircle />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Reject Request">
                          <IconButton
                            onClick={() => handleRejectRequest(request)}
                            sx={{ color: '#f44336' }}
                          >
                            <Cancel />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </Box>
                  </CardContent>
                </GlassCard>
              </motion.div>
            ))}
          </Box>
        )}

        {/* Action Dialog */}
        <Dialog 
          open={dialogOpen} 
          onClose={handleCloseDialog}
          maxWidth="md" 
          fullWidth
          PaperProps={{
            sx: {
              background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
              backdropFilter: 'blur(20px)',
              border: '1px solid rgba(255,255,255,0.2)',
            }
          }}
        >
          <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6">
              {dialogType === 'approve' ? 'Approve Request' : 
               dialogType === 'reject' ? 'Reject Request' : 'Request Details'}
            </Typography>
            <IconButton onClick={handleCloseDialog} size="small">
              <Close />
            </IconButton>
          </DialogTitle>
          
          <DialogContent>
            {selectedRequest && (
              <Box>
                <Box sx={{ mb: 3 }}>
                  <Typography variant="h6" sx={{ mb: 1 }}>
                    {selectedRequest.achievementDescription}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                    Requested by <strong>{selectedRequest.userDisplayName}</strong> ({selectedRequest.teamName})
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'text.disabled', display: 'block', mt: 1 }}>
                    Submitted {formatTimeAgo(selectedRequest.createdAt)}
                  </Typography>
                </Box>

                {selectedRequest.requestReason && (
                  <Box sx={{ mb: 3 }}>
                    <Typography variant="subtitle2" sx={{ mb: 1 }}>
                      Request Reason:
                    </Typography>
                    <Paper sx={{ p: 2, backgroundColor: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)' }}>
                      <Typography variant="body2">
                        {selectedRequest.requestReason}
                      </Typography>
                    </Paper>
                  </Box>
                )}

                {(dialogType === 'approve' || dialogType === 'reject') && (
                  <Box sx={{ mb: 2 }}>
                    <TextField
                      fullWidth
                      multiline
                      rows={3}
                      label="Admin Notes"
                      value={adminNotes}
                      onChange={(e) => setAdminNotes(e.target.value)}
                      placeholder={`Add notes about this ${dialogType} decision...`}
                      sx={{ mb: 2 }}
                    />
                    
                    {error && (
                      <Alert severity="error" sx={{ mb: 2 }}>
                        {error}
                      </Alert>
                    )}
                  </Box>
                )}
              </Box>
            )}
          </DialogContent>
          
          <DialogActions sx={{ p: 3, pt: 0 }}>
            <Button onClick={handleCloseDialog} variant="outlined">
              Cancel
            </Button>
            {dialogType !== 'view' && (
              <GradientButton
                onClick={handleConfirmAction}
                disabled={processing}
                variant="contained"
                sx={{
                  background: dialogType === 'approve' 
                    ? 'linear-gradient(45deg, #4caf50, #66bb6a)'
                    : 'linear-gradient(45deg, #f44336, #ef5350)',
                }}
              >
                {processing ? (
                  <CircularProgress size={20} />
                ) : (
                  `${dialogType === 'approve' ? 'Approve' : 'Reject'} Request`
                )}
              </GradientButton>
            )}
          </DialogActions>
        </Dialog>
      </motion.div>
    </Container>
  );
};

export default AdminInboxPage;