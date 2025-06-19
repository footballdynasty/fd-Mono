import React from 'react';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Box,
  Avatar,
  alpha,
  Skeleton,
} from '@mui/material';
import {
  SportsFootball,
  EmojiEvents,
  Leaderboard,
  Schedule,
  Dashboard,
  Settings,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import GlassCard from '../ui/GlassCard';
import { useSeasonProgress } from '../../hooks/useSeasonProgress';

const DRAWER_WIDTH = 280;

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ open, onClose }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { data: seasonProgress, isLoading: seasonLoading, error: seasonError } = useSeasonProgress();

  const menuItems = [
    { icon: Dashboard, label: 'Dashboard', path: '/' },
    { icon: Leaderboard, label: 'Standings', path: '/standings' },
    { icon: Schedule, label: 'Schedule', path: '/schedule' },
    { icon: EmojiEvents, label: 'Achievements', path: '/achievements' },
    { icon: SportsFootball, label: 'Teams', path: '/teams' },
    { icon: Settings, label: 'Settings', path: '/settings' },
  ];

  const handleNavigation = (path: string) => {
    navigate(path);
    onClose();
  };

  const listItemVariants = {
    initial: { x: -20, opacity: 0 },
    animate: { x: 0, opacity: 1 },
    hover: { x: 8, transition: { type: 'spring', stiffness: 300 } }
  };

  return (
    <Drawer
      variant="temporary"
      anchor="left"
      open={open}
      onClose={onClose}
      sx={{
        width: DRAWER_WIDTH,
        '& .MuiDrawer-paper': {
          width: DRAWER_WIDTH,
          background: 'linear-gradient(180deg, rgba(10,14,39,0.95) 0%, rgba(26,29,53,0.95) 100%)',
          backdropFilter: 'blur(20px)',
          border: 'none',
          borderRight: `1px solid ${alpha('#ffffff', 0.1)}`,
        },
      }}
    >
      <Box sx={{ p: 3, pt: 15 }}>
        {/* Logo/Header */}
        <Box sx={{ mb: 4, textAlign: 'center' }}>
          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ delay: 0.1 }}
          >
            <Avatar
              sx={{
                width: 60,
                height: 60,
                mx: 'auto',
                mb: 2,
                background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
              }}
            >
              <SportsFootball fontSize="large" />
            </Avatar>
            <Typography
              variant="h5"
              sx={{
                fontWeight: 700,
                background: 'linear-gradient(135deg, #42a5f5, #66bb6a)',
                backgroundClip: 'text',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
              }}
            >
              Football Dynasty
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Management Dashboard
            </Typography>
          </motion.div>
        </Box>

        {/* Navigation Menu */}
        <List sx={{ px: 0 }}>
          {menuItems.map((item, index) => {
            const isActive = location.pathname === item.path;
            const Icon = item.icon;

            return (
              <motion.div
                key={item.path}
                variants={listItemVariants}
                initial="initial"
                animate="animate"
                whileHover="hover"
                transition={{ delay: index * 0.1 }}
              >
                <ListItem disablePadding sx={{ mb: 1 }}>
                  <ListItemButton
                    onClick={() => handleNavigation(item.path)}
                    sx={{
                      borderRadius: '12px',
                      background: isActive 
                        ? 'linear-gradient(135deg, rgba(30,136,229,0.2) 0%, rgba(66,165,245,0.1) 100%)'
                        : 'transparent',
                      border: isActive 
                        ? `1px solid ${alpha('#42a5f5', 0.3)}`
                        : '1px solid transparent',
                      mb: 0.5,
                      '&:hover': {
                        background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
                      },
                    }}
                  >
                    <ListItemIcon
                      sx={{
                        color: isActive ? '#42a5f5' : 'text.secondary',
                        minWidth: 40,
                      }}
                    >
                      <Icon />
                    </ListItemIcon>
                    <ListItemText
                      primary={item.label}
                      sx={{
                        '& .MuiListItemText-primary': {
                          fontWeight: isActive ? 600 : 400,
                          color: isActive ? '#42a5f5' : 'text.primary',
                        },
                      }}
                    />
                  </ListItemButton>
                </ListItem>
              </motion.div>
            );
          })}
        </List>

        {/* Quick Stats Card */}
        <Box sx={{ mt: 4 }}>
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.6 }}
          >
            <GlassCard hover={false} sx={{ p: 2 }}>
              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                Season Progress
              </Typography>
              {seasonLoading ? (
                <>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Skeleton variant="text" width={60} height={28} />
                    <Skeleton variant="text" width={40} height={20} />
                  </Box>
                  <Skeleton variant="rectangular" width="100%" height={4} sx={{ borderRadius: 2 }} />
                </>
              ) : seasonError || !seasonProgress ? (
                <>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="h6" color="error">
                      --
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {new Date().getFullYear()}
                    </Typography>
                  </Box>
                  <Box
                    sx={{
                      height: 4,
                      background: alpha('#ffffff', 0.1),
                      borderRadius: 2,
                      overflow: 'hidden',
                    }}
                  >
                    <Box
                      sx={{
                        height: '100%',
                        width: '0%',
                        background: 'linear-gradient(90deg, #f44336, #ff5252)',
                        borderRadius: 2,
                      }}
                    />
                  </Box>
                </>
              ) : (
                <>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="h6" color="primary">
                      Week {seasonProgress.currentWeek}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {seasonProgress.year}
                    </Typography>
                  </Box>
                  <Box
                    sx={{
                      height: 4,
                      background: alpha('#ffffff', 0.1),
                      borderRadius: 2,
                      overflow: 'hidden',
                    }}
                  >
                    <Box
                      sx={{
                        height: '100%',
                        width: `${Math.round(seasonProgress.seasonProgress * 100)}%`,
                        background: 'linear-gradient(90deg, #1e88e5, #42a5f5)',
                        borderRadius: 2,
                      }}
                    />
                  </Box>
                </>
              )}
            </GlassCard>
          </motion.div>
        </Box>
      </Box>
    </Drawer>
  );
};

export default Sidebar;