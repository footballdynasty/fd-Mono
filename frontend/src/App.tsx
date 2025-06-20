import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline, GlobalStyles } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import theme from './theme/theme';
import { AuthProvider } from './hooks/useAuth';
import { ToastProvider } from './contexts/ToastContext';
import { PendingAchievementsProvider } from './contexts/PendingAchievementsContext';
import { NotificationsProvider } from './contexts/NotificationsContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/layout/Layout';
import Dashboard from './pages/Dashboard';
import StandingsPage from './pages/StandingsPage';
import SchedulePage from './pages/SchedulePage';
import AchievementsPage from './pages/AchievementsPage';
import AdminInboxPage from './pages/AdminInboxPage';
import TeamsPage from './pages/TeamsPage';
import TeamSelection from './pages/auth/TeamSelection';
import Debug from './pages/Debug';

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      refetchOnWindowFocus: false,
    },
  },
});

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <GlobalStyles
          styles={{
            '@keyframes pulse': {
              '0%': {
                opacity: 1,
              },
              '50%': {
                opacity: 0.6,
              },
              '100%': {
                opacity: 1,
              },
            },
          }}
        />
        <ToastProvider>
          <AuthProvider>
            <NotificationsProvider>
              <PendingAchievementsProvider>
                <Router>
                <ProtectedRoute>
                  <Layout>
                  <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/standings" element={<StandingsPage />} />
                    <Route path="/schedule" element={<SchedulePage />} />
                    <Route path="/achievements" element={<AchievementsPage />} />
                    <Route path="/admin/inbox" element={<AdminInboxPage />} />
                    <Route path="/teams" element={<TeamsPage />} />
                    <Route path="/settings" element={<div>Settings Page</div>} />
                    {/* Debug Routes */}
                    <Route path="/debug" element={<Debug />} />
                    <Route path="/debug/team-selection" element={<TeamSelection />} />
                  </Routes>
                  </Layout>
                </ProtectedRoute>
                </Router>
              </PendingAchievementsProvider>
            </NotificationsProvider>
          </AuthProvider>
        </ToastProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
};

export default App;