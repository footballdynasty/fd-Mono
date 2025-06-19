import React, { useState, useMemo } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Avatar,
  Chip,
  Container,
  CircularProgress,
  Alert,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Stack,
  Divider,
} from '@mui/material';
import {
  SportsFootball,
  Star,
  Person,
  Search,
  FilterList,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../hooks/useAuth';
import { teamApi } from '../../services/api';
import { Team } from '../../types';
import GradientButton from '../../components/ui/GradientButton';
import GlassCard from '../../components/ui/GlassCard';

const TeamSelection: React.FC = () => {
  const { selectTeam, user } = useAuth();
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedConference, setSelectedConference] = useState('All');
  const [showHumanOnly, setShowHumanOnly] = useState(false);

  const { data: teamsData, isLoading, error } = useQuery({
    queryKey: ['teams'],
    queryFn: () => teamApi.getAll({ size: 1000 }), // Get all teams
  });

  const { data: conferencesData } = useQuery({
    queryKey: ['conferences'],
    queryFn: () => teamApi.getConferences(),
  });

  // Extract data for use in useMemo
  const allTeams = teamsData?.data?.content || [];
  const conferences = conferencesData?.data || [];

  // Filter and search teams - moved before early returns
  const filteredTeams = useMemo(() => {
    let filtered = allTeams;

    // Apply search filter
    if (searchTerm.trim()) {
      const search = searchTerm.toLowerCase();
      filtered = filtered.filter(team =>
        team.name.toLowerCase().includes(search) ||
        team.coach?.toLowerCase().includes(search) ||
        team.conference?.toLowerCase().includes(search)
      );
    }

    // Apply conference filter
    if (selectedConference !== 'All') {
      filtered = filtered.filter(team => team.conference === selectedConference);
    }

    // Apply human-only filter
    if (showHumanOnly) {
      filtered = filtered.filter(team => team.isHuman);
    }

    return filtered;
  }, [allTeams, searchTerm, selectedConference, showHumanOnly]);

  const handleTeamSelect = (team: Team) => {
    setSelectedTeam(team);
  };

  const handleConfirmSelection = async () => {
    if (!selectedTeam) return;
    
    setIsSubmitting(true);
    try {
      selectTeam(selectedTeam);
    } catch (error) {
      console.error('Error selecting team:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

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

  if (isLoading) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          background: 'linear-gradient(135deg, #0a0e27 0%, #1a1d35 50%, #242744 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <CircularProgress size={60} />
      </Box>
    );
  }

  if (error) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          background: 'linear-gradient(135deg, #0a0e27 0%, #1a1d35 50%, #242744 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: 2,
        }}
      >
        <Alert severity="error">
          Failed to load teams. Please try again.
        </Alert>
      </Box>
    );
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #0a0e27 0%, #1a1d35 50%, #242744 100%)',
        pt: 4,
        pb: selectedTeam ? 120 : 4, // Extra bottom padding when team is selected
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
              <Avatar
                sx={{
                  width: 80,
                  height: 80,
                  mx: 'auto',
                  mb: 2,
                  background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
                }}
              >
                <SportsFootball fontSize="large" />
              </Avatar>
              <Typography
                variant="h3"
                sx={{
                  fontWeight: 700,
                  mb: 1,
                  background: 'linear-gradient(135deg, #42a5f5, #66bb6a)',
                  backgroundClip: 'text',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                }}
              >
                Choose Your Team
              </Typography>
              <Typography variant="h6" color="text.secondary">
                Welcome, {user?.username}! Select the team you are managing in game.
              </Typography>
            </Box>
          </motion.div>

          {/* Search and Filter Section */}
          <motion.div variants={itemVariants}>
            <GlassCard sx={{ mb: 4, p: 3 }}>
              <Stack spacing={3}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <FilterList color="primary" />
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    Find Your Team
                  </Typography>
                  <Chip 
                    label={`${filteredTeams.length} teams`} 
                    size="small" 
                    color="primary" 
                    variant="outlined"
                  />
                </Box>
                
                <Divider sx={{ opacity: 0.3 }} />
                
                <Grid container spacing={3} sx={{ px: 2 }}>
                  {/* Search Field */}
                  <Grid item xs={12} md={5}>
                    <TextField
                      fullWidth
                      placeholder="Search teams, coaches, or conferences..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      InputProps={{
                        startAdornment: (
                          <InputAdornment position="start">
                            <Search color="action" />
                          </InputAdornment>
                        ),
                        sx: {
                          backgroundColor: 'rgba(255,255,255,0.05)',
                          '& .MuiOutlinedInput-notchedOutline': {
                            borderColor: 'rgba(255,255,255,0.2)',
                          },
                          '&:hover .MuiOutlinedInput-notchedOutline': {
                            borderColor: 'rgba(255,255,255,0.3)',
                          },
                        },
                      }}
                      sx={{
                        '& .MuiInputLabel-root': {
                          color: 'text.secondary',
                        },
                      }}
                    />
                  </Grid>

                  {/* Conference Filter */}
                  <Grid item xs={12} md={4}>
                    <FormControl fullWidth>
                      <InputLabel sx={{ color: 'text.secondary' }}>Conference</InputLabel>
                      <Select
                        value={selectedConference}
                        onChange={(e) => setSelectedConference(e.target.value)}
                        label="Conference"
                        sx={{
                          backgroundColor: 'rgba(255,255,255,0.05)',
                          '& .MuiOutlinedInput-notchedOutline': {
                            borderColor: 'rgba(255,255,255,0.2)',
                          },
                          '&:hover .MuiOutlinedInput-notchedOutline': {
                            borderColor: 'rgba(255,255,255,0.3)',
                          },
                        }}
                      >
                        <MenuItem value="All">All Conferences</MenuItem>
                        {conferences.map((conference) => (
                          <MenuItem key={conference} value={conference}>
                            {conference}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>

                  {/* Human Teams Filter */}
                  <Grid item xs={12} md={3}>
                    <FormControl fullWidth>
                      <InputLabel sx={{ color: 'text.secondary' }}>Type</InputLabel>
                      <Select
                        value={showHumanOnly ? 'human' : 'all'}
                        onChange={(e) => setShowHumanOnly(e.target.value === 'human')}
                        label="Type"
                        sx={{
                          backgroundColor: 'rgba(255,255,255,0.05)',
                          '& .MuiOutlinedInput-notchedOutline': {
                            borderColor: 'rgba(255,255,255,0.2)',
                          },
                          '&:hover .MuiOutlinedInput-notchedOutline': {
                            borderColor: 'rgba(255,255,255,0.3)',
                          },
                        }}
                      >
                        <MenuItem value="all">All Teams</MenuItem>
                        <MenuItem value="human">Human Only</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                </Grid>
              </Stack>
            </GlassCard>
          </motion.div>

          {/* Team Grid */}
          <Grid container spacing={3}>
            {filteredTeams.length === 0 ? (
              <Grid item xs={12}>
                <Box sx={{ textAlign: 'center', py: 8 }}>
                  <Typography variant="h6" color="text.secondary">
                    No teams found matching your search criteria
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    Try adjusting your search or filters
                  </Typography>
                </Box>
              </Grid>
            ) : (
              filteredTeams.map((team, index) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={team.id}>
                <motion.div
                  variants={itemVariants}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                >
                  <Card
                    onClick={() => handleTeamSelect(team)}
                    sx={{
                      cursor: 'pointer',
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      background: selectedTeam?.id === team.id
                        ? 'linear-gradient(145deg, rgba(30,136,229,0.2) 0%, rgba(66,165,245,0.1) 100%)'
                        : 'linear-gradient(145deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                      backdropFilter: 'blur(20px)',
                      border: selectedTeam?.id === team.id
                        ? '2px solid #42a5f5'
                        : '1px solid rgba(255,255,255,0.1)',
                      borderRadius: '20px',
                      boxShadow: selectedTeam?.id === team.id
                        ? '0 8px 32px rgba(66,165,245,0.3)'
                        : '0 8px 32px rgba(0,0,0,0.3)',
                      transition: 'all 0.3s ease',
                      '&:hover': {
                        transform: 'translateY(-4px)',
                        boxShadow: '0 12px 40px rgba(0,0,0,0.4)',
                      },
                    }}
                  >
                    <CardContent sx={{ p: 3, textAlign: 'center', flexGrow: 1, display: 'flex', flexDirection: 'column', minHeight: '280px' }}>
                      {/* Top Section */}
                      <Box sx={{ mb: 'auto' }}>
                        {/* Team Avatar */}
                        <Avatar
                          sx={{
                            width: 60,
                            height: 60,
                            mx: 'auto',
                            mb: 2,
                            background: 'linear-gradient(135deg, #ff9800 0%, #ffb74d 100%)',
                            fontSize: '1.5rem',
                            fontWeight: 700,
                          }}
                        >
                          {team.name.substring(0, 2).toUpperCase()}
                        </Avatar>

                        {/* Team Name */}
                        <Typography
                          variant="h6"
                          sx={{
                            fontWeight: 600,
                            mb: 1,
                            color: selectedTeam?.id === team.id ? '#42a5f5' : 'text.primary',
                            lineHeight: 1.2,
                            minHeight: '2.4em', // Reserve space for 2 lines
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          {team.name}
                        </Typography>

                        {/* Conference */}
                        <Box sx={{ minHeight: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 2 }}>
                          {team.conference ? (
                            <Chip
                              label={team.conference}
                              size="small"
                              sx={{
                                background: 'linear-gradient(135deg, rgba(255,255,255,0.2), rgba(255,255,255,0.1))',
                                color: 'text.secondary',
                              }}
                            />
                          ) : (
                            <Box sx={{ height: '24px' }} /> // Placeholder for consistent spacing
                          )}
                        </Box>
                      </Box>

                      {/* Bottom Section */}
                      <Box sx={{ mt: 'auto' }}>
                        {/* Team Stats */}
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                          <Box sx={{ textAlign: 'left' }}>
                            <Typography variant="body2" color="text.secondary">
                              Record
                            </Typography>
                            <Typography variant="body1" sx={{ fontWeight: 600 }}>
                              {team.currentWins || 0}-{team.currentLosses || 0}
                            </Typography>
                          </Box>
                          <Box sx={{ textAlign: 'right' }}>
                            <Typography variant="body2" color="text.secondary">
                              Rank
                            </Typography>
                            <Typography variant="body1" sx={{ fontWeight: 600 }}>
                              {team.currentRank ? `#${team.currentRank}` : 'NR'}
                            </Typography>
                          </Box>
                        </Box>

                        {/* Indicators Section */}
                        <Box sx={{ minHeight: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1 }}>
                          {/* Human Team Indicator */}
                          {team.isHuman && (
                            <Chip
                              icon={<Person />}
                              label="Human"
                              size="small"
                              color="primary"
                              sx={{
                                background: 'linear-gradient(135deg, #1e88e5, #42a5f5)',
                                color: 'white',
                              }}
                            />
                          )}

                          {/* Selection Indicator */}
                          {selectedTeam?.id === team.id && (
                            <Star color="primary" />
                          )}
                        </Box>
                      </Box>
                    </CardContent>
                  </Card>
                </motion.div>
              </Grid>
              ))
            )}
          </Grid>

          {/* Confirm Button - Sticky at bottom when team is selected */}
          {selectedTeam && (
            <motion.div
              variants={itemVariants}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
              style={{
                position: 'fixed',
                bottom: 0,
                left: 0,
                right: 0,
                zIndex: 1000,
              }}
            >
              <Box 
                sx={{ 
                  background: 'linear-gradient(135deg, rgba(10,14,39,0.7) 0%, rgba(26,29,53,0.75) 50%, rgba(36,39,68,0.8) 100%)',
                  backdropFilter: 'blur(25px)',
                  borderTop: '1px solid rgba(255,255,255,0.15)',
                  py: 3,
                  px: 2,
                  textAlign: 'center',
                  boxShadow: '0 -8px 32px rgba(0,0,0,0.3)',
                }}
              >
                <Container maxWidth="lg">
                  <GlassCard hover={false} sx={{ display: 'inline-block', p: 3 }}>
                    <Typography variant="h6" sx={{ mb: 2 }}>
                      Selected: {selectedTeam.name}
                    </Typography>
                    <GradientButton
                      size="large"
                      onClick={handleConfirmSelection}
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? 'Setting up...' : 'Start Managing Team'}
                    </GradientButton>
                  </GlassCard>
                </Container>
              </Box>
            </motion.div>
          )}
        </motion.div>
      </Container>
    </Box>
  );
};

export default TeamSelection;