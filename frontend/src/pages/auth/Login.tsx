import React, { useState } from 'react';
import {
  Box,
  TextField,
  Typography,
  Alert,
  Container,
  Avatar,
  InputAdornment,
  IconButton,
  Button,
  Divider,
} from '@mui/material';
import {
  SportsFootball,
  Person,
  Lock,
  Visibility,
  VisibilityOff,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../contexts/ToastContext';
import GradientButton from '../../components/ui/GradientButton';
import GlassCard from '../../components/ui/GlassCard';
import Register from './Register';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const { login } = useAuth();
  const { showSuccess, showError } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      await login(username, password);
      showSuccess('Successfully signed in! Welcome back.');
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 'Login failed. Please check your credentials.';
      setError(errorMessage);
      showError(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const containerVariants = {
    initial: { opacity: 0, y: 50 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { 
        duration: 0.6,
        ease: "easeOut"
      }
    }
  };

  const formVariants = {
    initial: { opacity: 0 },
    animate: { 
      opacity: 1,
      transition: { 
        delay: 0.3,
        duration: 0.5
      }
    }
  };

  if (showRegister) {
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
        <Register onBackToLogin={() => setShowRegister(false)} />
      </Box>
    );
  }

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
      <Container maxWidth="sm">
        <motion.div
          variants={containerVariants}
          initial="initial"
          animate="animate"
        >
          <GlassCard hover={false}>
            <Box sx={{ p: 4 }}>
              {/* Logo/Header */}
              <Box sx={{ textAlign: 'center', mb: 4 }}>
                <motion.div
                  initial={{ scale: 0.5, opacity: 0 }}
                  animate={{ scale: 1, opacity: 1 }}
                  transition={{ delay: 0.1, duration: 0.5 }}
                >
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
                    variant="h4"
                    sx={{
                      fontWeight: 700,
                      mb: 1,
                      background: 'linear-gradient(135deg, #42a5f5, #66bb6a)',
                      backgroundClip: 'text',
                      WebkitBackgroundClip: 'text',
                      WebkitTextFillColor: 'transparent',
                    }}
                  >
                    Football Dynasty
                  </Typography>
                  <Typography variant="body1" color="text.secondary">
                    Sign in to manage your team
                  </Typography>
                </motion.div>
              </Box>

              {/* Login Form */}
              <motion.div
                variants={formVariants}
                initial="initial"
                animate="animate"
              >
                <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3 }}>
                  {error && (
                    <Alert severity="error" sx={{ mb: 3 }}>
                      {error}
                    </Alert>
                  )}

                  <TextField
                    fullWidth
                    label="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    margin="normal"
                    required
                    autoFocus
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <Person color="action" />
                        </InputAdornment>
                      ),
                    }}
                  />

                  <TextField
                    fullWidth
                    label="Password"
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    margin="normal"
                    required
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <Lock color="action" />
                        </InputAdornment>
                      ),
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton
                            onClick={() => setShowPassword(!showPassword)}
                            edge="end"
                          >
                            {showPassword ? <VisibilityOff /> : <Visibility />}
                          </IconButton>
                        </InputAdornment>
                      ),
                    }}
                  />

                  <Box sx={{ mt: 4 }}>
                    <GradientButton
                      type="submit"
                      fullWidth
                      size="large"
                      disabled={isSubmitting || !username || !password}
                    >
                      {isSubmitting ? 'Signing In...' : 'Sign In'}
                    </GradientButton>
                  </Box>

                  {/* Divider */}
                  <Box sx={{ mt: 3, mb: 3 }}>
                    <Divider sx={{ 
                      borderColor: 'rgba(255,255,255,0.2)',
                      '&::before, &::after': {
                        borderColor: 'rgba(255,255,255,0.2)',
                      }
                    }}>
                      <Typography variant="body2" color="text.secondary">
                        or
                      </Typography>
                    </Divider>
                  </Box>

                  {/* Sign Up Button */}
                  <Box>
                    <Button
                      fullWidth
                      size="large"
                      onClick={() => setShowRegister(true)}
                      sx={{
                        borderRadius: '12px',
                        textTransform: 'none',
                        fontWeight: 600,
                        padding: '12px 24px',
                        border: '2px solid rgba(255,255,255,0.2)',
                        color: 'text.primary',
                        background: 'rgba(255,255,255,0.05)',
                        '&:hover': {
                          background: 'rgba(255,255,255,0.1)',
                          border: '2px solid rgba(255,255,255,0.3)',
                        },
                      }}
                    >
                      Create New Account
                    </Button>
                  </Box>
                </Box>
              </motion.div>
            </Box>
          </GlassCard>
        </motion.div>
      </Container>
    </Box>
  );
};

export default Login;