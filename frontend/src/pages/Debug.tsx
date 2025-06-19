import React from 'react';
import {
  Box,
  Typography,
  Container,
  Card,
  CardContent,
  Button,
  Grid,
  Chip,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  BugReport,
  SportsFootball,
  Group,
  Dashboard as DashboardIcon,
  Login,
  ExitToApp,
  ContentCopy,
  OpenInNew,
  DataObject,
  Refresh,
  Settings,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../contexts/ToastContext';
import GradientButton from '../components/ui/GradientButton';
import GlassCard from '../components/ui/GlassCard';
import api from '../services/api';

const Debug: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, logout, isAuthenticated } = useAuth();
  const { showSuccess, showInfo, showError } = useToast();

  // Query environment info
  const { data: environmentData, isLoading: envLoading } = useQuery({
    queryKey: ['environment'],
    queryFn: () => api.get('/admin/environment'),
  });

  // Mutation for creating mock data
  const createMockDataMutation = useMutation({
    mutationFn: () => api.post('/admin/mock-data/create'),
    onSuccess: (data) => {
      showSuccess(`Mock data created successfully! Duration: ${data.data.duration}`);
      queryClient.invalidateQueries({ queryKey: ['environment'] });
    },
    onError: (error: any) => {
      showError(`Failed to create mock data: ${error.response?.data?.message || error.message}`);
    }
  });

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    showSuccess('Copied to clipboard!');
  };

  const debugRoutes = [
    {
      title: 'Team Selection Page',
      description: 'Test the team selection interface with search and filters',
      path: '/debug/team-selection',
      icon: <SportsFootball />,
      color: 'primary',
    },
    {
      title: 'Dashboard',
      description: 'Main dashboard page',
      path: '/',
      icon: <DashboardIcon />,
      color: 'secondary',
    },
  ];

  const apiEndpoints = [
    {
      title: 'Get All Teams',
      url: 'http://localhost:8080/api/v2/teams',
      method: 'GET',
      description: 'Retrieve all CFB teams with pagination',
    },
    {
      title: 'Search Teams',
      url: 'http://localhost:8080/api/v2/teams?search=Alabama&size=10',
      method: 'GET',
      description: 'Search teams by name',
    },
    {
      title: 'Get Conferences',
      url: 'http://localhost:8080/api/v2/teams/conferences',
      method: 'GET',
      description: 'Get all available conferences',
    },
    {
      title: 'Get All Users (Admin)',
      url: 'http://localhost:8080/api/v2/admin/users',
      method: 'GET',
      description: 'Admin endpoint to view all users',
    },
    {
      title: 'Get User by Username',
      url: 'http://localhost:8080/api/v2/admin/users/testuser',
      method: 'GET',
      description: 'Get specific user by username or ID',
    },
  ];

  const containerVariants = {
    initial: { opacity: 0 },
    animate: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1,
      },
    },
  };

  const itemVariants = {
    initial: { y: 20, opacity: 0 },
    animate: {
      y: 0,
      opacity: 1,
      transition: {
        type: 'spring',
        stiffness: 300,
        damping: 24,
      },
    },
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #0a0e27 0%, #1a1d35 50%, #242744 100%)',
        py: 4,
      }}
    >
      <Container maxWidth="lg">
        <motion.div
          variants={containerVariants}
          initial="initial"
          animate="animate"
        >
          {/* Header */}
          <motion.div variants={itemVariants}>
            <Box sx={{ textAlign: 'center', mb: 6 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 2 }}>
                <BugReport sx={{ fontSize: 48, color: 'primary.main', mr: 2 }} />
                <Typography
                  variant="h3"
                  sx={{
                    fontWeight: 700,
                    background: 'linear-gradient(135deg, #42a5f5, #66bb6a)',
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                  }}
                >
                  Debug Console
                </Typography>
              </Box>
              <Typography variant="h6" color="text.secondary">
                Development tools and testing endpoints
              </Typography>
              {isAuthenticated && (
                <Chip
                  label={`Logged in as: ${user?.username}`}
                  color="success"
                  sx={{ mt: 2 }}
                />
              )}
            </Box>
          </motion.div>

          <Grid container spacing={4}>
            {/* Environment Status */}
            <Grid item xs={12}>
              <motion.div variants={itemVariants}>
                <GlassCard>
                  <CardContent sx={{ p: 3 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <Settings sx={{ color: 'primary.main', mr: 1, fontSize: 28 }} />
                      <Typography variant="h5" sx={{ fontWeight: 600, flexGrow: 1 }}>
                        Environment Status
                      </Typography>
                      {envLoading && <CircularProgress size={20} />}
                    </Box>
                    
                    {environmentData?.data ? (
                      <Grid container spacing={3}>
                        <Grid item xs={12} sm={6} md={3}>
                          <Box>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                              Environment
                            </Typography>
                            <Chip
                              label={environmentData.data.environment}
                              color={environmentData.data.environment === 'testing' ? 'success' : 'warning'}
                              sx={{ fontWeight: 600 }}
                            />
                          </Box>
                        </Grid>
                        <Grid item xs={12} sm={6} md={3}>
                          <Box>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                              Mock Data
                            </Typography>
                            <Chip
                              label={environmentData.data.mockDataEnabled ? 'Enabled' : 'Disabled'}
                              color={environmentData.data.mockDataEnabled ? 'success' : 'error'}
                              sx={{ fontWeight: 600 }}
                            />
                          </Box>
                        </Grid>
                        <Grid item xs={12} sm={6} md={3}>
                          <Box>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                              Testing Mode
                            </Typography>
                            <Chip
                              label={environmentData.data.isTestingEnvironment ? 'Active' : 'Inactive'}
                              color={environmentData.data.isTestingEnvironment ? 'info' : 'default'}
                              sx={{ fontWeight: 600 }}
                            />
                          </Box>
                        </Grid>
                        <Grid item xs={12} sm={6} md={3}>
                          <Box>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                              Mock Data Control
                            </Typography>
                            <Button
                              variant="contained"
                              size="small"
                              startIcon={createMockDataMutation.isPending ? <CircularProgress size={16} /> : <Refresh />}
                              disabled={!environmentData.data.mockDataEnabled || createMockDataMutation.isPending}
                              onClick={() => createMockDataMutation.mutate()}
                              color="secondary"
                            >
                              {createMockDataMutation.isPending ? 'Creating...' : 'Create Mock Data'}
                            </Button>
                          </Box>
                        </Grid>
                      </Grid>
                    ) : envLoading ? (
                      <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                        <CircularProgress />
                      </Box>
                    ) : (
                      <Alert severity="warning">
                        Failed to load environment information. Check backend connection.
                      </Alert>
                    )}

                    {environmentData?.data?.mockDataEnabled && (
                      <Alert severity="info" sx={{ mt: 2 }}>
                        <Typography variant="body2">
                          <strong>Mock Data Active:</strong> The system will automatically generate realistic CFB games, 
                          standings, and championship data when you visit the dashboard.
                        </Typography>
                      </Alert>
                    )}
                  </CardContent>
                </GlassCard>
              </motion.div>
            </Grid>

            {/* Debug Routes */}
            <Grid item xs={12} md={6}>
              <motion.div variants={itemVariants}>
                <GlassCard>
                  <CardContent sx={{ p: 3 }}>
                    <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
                      🧭 Debug Routes
                    </Typography>
                    <Grid container spacing={2}>
                      {debugRoutes.map((route, index) => (
                        <Grid item xs={12} key={index}>
                          <Card
                            sx={{
                              background: 'linear-gradient(145deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                              backdropFilter: 'blur(10px)',
                              border: '1px solid rgba(255,255,255,0.1)',
                            }}
                          >
                            <CardContent sx={{ p: 2 }}>
                              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                {route.icon}
                                <Typography variant="h6" sx={{ ml: 1, fontWeight: 600 }}>
                                  {route.title}
                                </Typography>
                              </Box>
                              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                {route.description}
                              </Typography>
                              <GradientButton
                                size="small"
                                onClick={() => navigate(route.path)}
                                startIcon={<OpenInNew />}
                              >
                                Visit Page
                              </GradientButton>
                            </CardContent>
                          </Card>
                        </Grid>
                      ))}
                    </Grid>
                  </CardContent>
                </GlassCard>
              </motion.div>
            </Grid>

            {/* API Endpoints */}
            <Grid item xs={12} md={6}>
              <motion.div variants={itemVariants}>
                <GlassCard>
                  <CardContent sx={{ p: 3 }}>
                    <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
                      🔌 API Endpoints
                    </Typography>
                    <List>
                      {apiEndpoints.map((endpoint, index) => (
                        <React.Fragment key={index}>
                          <ListItem sx={{ px: 0 }}>
                            <ListItemIcon>
                              <Chip label={endpoint.method} size="small" color="primary" />
                            </ListItemIcon>
                            <ListItemText
                              primary={endpoint.title}
                              secondary={
                                <Box>
                                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                                    {endpoint.description}
                                  </Typography>
                                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Typography
                                      variant="body2"
                                      sx={{
                                        fontFamily: 'monospace',
                                        fontSize: '0.75rem',
                                        backgroundColor: 'rgba(255,255,255,0.1)',
                                        px: 1,
                                        py: 0.5,
                                        borderRadius: 1,
                                        wordBreak: 'break-all',
                                        flex: 1,
                                      }}
                                    >
                                      {endpoint.url}
                                    </Typography>
                                    <IconButton
                                      size="small"
                                      onClick={() => copyToClipboard(endpoint.url)}
                                      sx={{ color: 'text.secondary' }}
                                    >
                                      <ContentCopy fontSize="small" />
                                    </IconButton>
                                    <IconButton
                                      size="small"
                                      onClick={() => window.open(endpoint.url, '_blank')}
                                      sx={{ color: 'text.secondary' }}
                                    >
                                      <OpenInNew fontSize="small" />
                                    </IconButton>
                                  </Box>
                                </Box>
                              }
                            />
                          </ListItem>
                          {index < apiEndpoints.length - 1 && <Divider sx={{ my: 1, opacity: 0.3 }} />}
                        </React.Fragment>
                      ))}
                    </List>
                  </CardContent>
                </GlassCard>
              </motion.div>
            </Grid>

            {/* Quick Actions */}
            <Grid item xs={12}>
              <motion.div variants={itemVariants}>
                <GlassCard>
                  <CardContent sx={{ p: 3 }}>
                    <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
                      ⚡ Quick Actions
                    </Typography>
                    <Grid container spacing={2}>
                      <Grid item xs={12} sm={6} md={3}>
                        <Button
                          fullWidth
                          variant="outlined"
                          onClick={() => navigate('/debug/team-selection')}
                          startIcon={<SportsFootball />}
                          sx={{
                            borderColor: 'rgba(255,255,255,0.3)',
                            color: 'text.primary',
                            '&:hover': {
                              borderColor: 'primary.main',
                              backgroundColor: 'rgba(66, 165, 245, 0.1)',
                            },
                          }}
                        >
                          Test Team Selection
                        </Button>
                      </Grid>
                      <Grid item xs={12} sm={6} md={3}>
                        <Button
                          fullWidth
                          variant="outlined"
                          onClick={() => window.open('http://localhost:8080/api/v2/teams', '_blank')}
                          startIcon={<Group />}
                          sx={{
                            borderColor: 'rgba(255,255,255,0.3)',
                            color: 'text.primary',
                            '&:hover': {
                              borderColor: 'secondary.main',
                              backgroundColor: 'rgba(102, 187, 106, 0.1)',
                            },
                          }}
                        >
                          View All Teams
                        </Button>
                      </Grid>
                      {isAuthenticated && (
                        <Grid item xs={12} sm={6} md={3}>
                          <Button
                            fullWidth
                            variant="outlined"
                            onClick={() => {
                              logout();
                              showInfo('Logged out successfully');
                            }}
                            startIcon={<ExitToApp />}
                            sx={{
                              borderColor: 'rgba(255,255,255,0.3)',
                              color: 'text.primary',
                              '&:hover': {
                                borderColor: 'error.main',
                                backgroundColor: 'rgba(244, 67, 54, 0.1)',
                              },
                            }}
                          >
                            Logout
                          </Button>
                        </Grid>
                      )}
                      <Grid item xs={12} sm={6} md={3}>
                        <Button
                          fullWidth
                          variant="outlined"
                          onClick={() => navigate('/')}
                          startIcon={<DashboardIcon />}
                          sx={{
                            borderColor: 'rgba(255,255,255,0.3)',
                            color: 'text.primary',
                            '&:hover': {
                              borderColor: 'info.main',
                              backgroundColor: 'rgba(33, 150, 243, 0.1)',
                            },
                          }}
                        >
                          Go to Dashboard
                        </Button>
                      </Grid>
                    </Grid>
                  </CardContent>
                </GlassCard>
              </motion.div>
            </Grid>
          </Grid>
        </motion.div>
      </Container>
    </Box>
  );
};

export default Debug;