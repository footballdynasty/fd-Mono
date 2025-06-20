import React, { useState } from 'react';
import {
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Box,
  Avatar,
  alpha,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Search,
  Logout,
  SportsFootball,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useAuth } from '../../hooks/useAuth';
import NotificationsDropdown from './NotificationsDropdown';

interface HeaderProps {
  onMenuClick: () => void;
}

const Header: React.FC<HeaderProps> = ({ onMenuClick }) => {
  const { user, selectedTeam, logout } = useAuth();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const handleMenuClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    handleMenuClose();
  };

  return (
    <AppBar
      position="fixed"
      sx={{
        background: 'linear-gradient(135deg, rgba(10,14,39,0.95) 0%, rgba(26,29,53,0.95) 50%, rgba(36,39,68,0.95) 100%)',
        backdropFilter: 'blur(20px)',
        borderBottom: `1px solid ${alpha('#ffffff', 0.1)}`,
        boxShadow: '0 8px 32px rgba(0,0,0,0.3)',
        zIndex: (theme) => theme.zIndex.drawer + 1,
      }}
    >
      <Toolbar sx={{ px: { xs: 2, sm: 3 } }}>
        {/* Menu Button */}
        <motion.div
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
        >
          <IconButton
            edge="start"
            color="inherit"
            aria-label="menu"
            onClick={onMenuClick}
            sx={{
              mr: 2,
              background: alpha('#ffffff', 0.1),
              '&:hover': {
                background: alpha('#ffffff', 0.2),
              },
            }}
          >
            <MenuIcon />
          </IconButton>
        </motion.div>

        {/* Title and Team Info */}
        <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center' }}>
          <Typography
            variant="h6"
            component="div"
            sx={{
              fontWeight: 600,
              background: 'linear-gradient(135deg, #ffffff, #e3f2fd)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              mr: 2,
            }}
          >
            Football Dynasty
          </Typography>
          {selectedTeam && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <SportsFootball sx={{ mr: 1, color: '#42a5f5' }} />
              <Typography variant="body1" sx={{ fontWeight: 500 }}>
                {selectedTeam.name}
              </Typography>
            </Box>
          )}
        </Box>

        {/* Search */}
        <motion.div
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
        >
          <IconButton
            color="inherit"
            sx={{
              mr: 1,
              background: alpha('#ffffff', 0.1),
              '&:hover': {
                background: alpha('#ffffff', 0.2),
              },
            }}
          >
            <Search />
          </IconButton>
        </motion.div>

        {/* Notifications */}
        <NotificationsDropdown />

        {/* User Avatar */}
        <motion.div
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
        >
          <Avatar
            onClick={handleMenuClick}
            sx={{
              width: 40,
              height: 40,
              background: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
              border: `2px solid ${alpha('#ffffff', 0.2)}`,
              cursor: 'pointer',
            }}
          >
            {user?.username?.substring(0, 2).toUpperCase() || 'FD'}
          </Avatar>
        </motion.div>

        {/* User Menu */}
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
          PaperProps={{
            sx: {
              background: 'linear-gradient(145deg, rgba(26,29,53,0.95) 0%, rgba(36,39,68,0.95) 100%)',
              backdropFilter: 'blur(20px)',
              border: '1px solid rgba(255,255,255,0.1)',
              borderRadius: '12px',
              mt: 1,
            },
          }}
        >
          <MenuItem onClick={handleMenuClose}>
            <ListItemIcon>
              <SportsFootball fontSize="small" />
            </ListItemIcon>
            <ListItemText 
              primary={user?.username}
              secondary={selectedTeam?.name}
            />
          </MenuItem>
          <MenuItem onClick={handleLogout}>
            <ListItemIcon>
              <Logout fontSize="small" />
            </ListItemIcon>
            <ListItemText primary="Logout" />
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
};

export default Header;