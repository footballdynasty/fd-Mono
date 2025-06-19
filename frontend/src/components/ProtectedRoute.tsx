import React from 'react';
import { useAuth } from '../hooks/useAuth';
import Login from '../pages/auth/Login';
import TeamSelection from '../pages/auth/TeamSelection';
import { Box, CircularProgress } from '@mui/material';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, selectedTeam, isLoading } = useAuth();

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

  if (!isAuthenticated) {
    return <Login />;
  }

  if (!selectedTeam) {
    return <TeamSelection />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;