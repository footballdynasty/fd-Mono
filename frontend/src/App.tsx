import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import theme from './theme/theme';
import { AuthProvider } from './hooks/useAuth';
import { ToastProvider } from './contexts/ToastContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/layout/Layout';
import Dashboard from './pages/Dashboard';
import StandingsPage from './pages/StandingsPage';
import SchedulePage from './pages/SchedulePage';
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
        <ToastProvider>
          <AuthProvider>
            <Router>
              <ProtectedRoute>
                <Layout>
                  <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/standings" element={<StandingsPage />} />
                    <Route path="/schedule" element={<SchedulePage />} />
                    <Route path="/achievements" element={<div>Achievements Page</div>} />
                    <Route path="/teams" element={<div>Teams Page</div>} />
                    <Route path="/settings" element={<div>Settings Page</div>} />
                    {/* Debug Routes */}
                    <Route path="/debug" element={<Debug />} />
                    <Route path="/debug/team-selection" element={<TeamSelection />} />
                  </Routes>
                </Layout>
              </ProtectedRoute>
            </Router>
          </AuthProvider>
        </ToastProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
};

export default App;