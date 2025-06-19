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
  Divider,
} from '@mui/material';
import {
  SportsFootball,
  Person,
  Lock,
  Email,
  Visibility,
  VisibilityOff,
  ArrowBack,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useAuth } from '../../hooks/useAuth';
import GradientButton from '../../components/ui/GradientButton';
import GlassCard from '../../components/ui/GlassCard';

interface RegisterProps {
  onBackToLogin: () => void;
}

const Register: React.FC<RegisterProps> = ({ onBackToLogin }) => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register } = useAuth();

  const handleInputChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [field]: e.target.value
    }));
  };

  const validateForm = () => {
    if (!formData.username || !formData.password || !formData.confirmPassword) {
      setError('Username, password, and confirm password are required');
      return false;
    }

    if (formData.username.length < 3) {
      setError('Username must be at least 3 characters long');
      return false;
    }

    if (formData.email && !/\S+@\S+\.\S+/.test(formData.email)) {
      setError('Please enter a valid email address');
      return false;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long');
      return false;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      await register(formData.username, formData.email, formData.password);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const containerVariants = {
    initial: { opacity: 0, x: 50 },
    animate: { 
      opacity: 1, 
      x: 0,
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

  return (
    <Container maxWidth="sm">
      <motion.div
        variants={containerVariants}
        initial="initial"
        animate="animate"
      >
        <GlassCard hover={false}>
          <Box sx={{ p: 4 }}>
            {/* Back Button */}
            <Box sx={{ mb: 2 }}>
              <motion.div
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                <IconButton
                  onClick={onBackToLogin}
                  sx={{
                    background: 'rgba(255,255,255,0.1)',
                    '&:hover': {
                      background: 'rgba(255,255,255,0.2)',
                    },
                  }}
                >
                  <ArrowBack />
                </IconButton>
              </motion.div>
            </Box>

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
                  Join Football Dynasty
                </Typography>
                <Typography variant="body1" color="text.secondary">
                  Create your account to start managing teams
                </Typography>
              </motion.div>
            </Box>

            {/* Register Form */}
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
                  value={formData.username}
                  onChange={handleInputChange('username')}
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
                  value={formData.password}
                  onChange={handleInputChange('password')}
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

                <TextField
                  fullWidth
                  label="Confirm Password"
                  type={showConfirmPassword ? 'text' : 'password'}
                  value={formData.confirmPassword}
                  onChange={handleInputChange('confirmPassword')}
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
                          onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                          edge="end"
                        >
                          {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                />

                {/* Optional Section Divider */}
                <Box sx={{ mt: 3, mb: 2 }}>
                  <Divider sx={{ 
                    borderColor: 'rgba(255,255,255,0.1)',
                    '&::before, &::after': {
                      borderColor: 'rgba(255,255,255,0.1)',
                    }
                  }}>
                    <Typography variant="body2" color="text.secondary" sx={{ 
                      fontSize: '0.75rem',
                      opacity: 0.7,
                      px: 2
                    }}>
                      Optional
                    </Typography>
                  </Divider>
                </Box>

                <TextField
                  fullWidth
                  label="Email Address"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange('email')}
                  margin="normal"
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <Email color="action" />
                      </InputAdornment>
                    ),
                  }}
                  helperText="Recommended for account recovery and notifications"
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      '& fieldset': {
                        borderColor: 'rgba(255,255,255,0.15)',
                      },
                      '&:hover fieldset': {
                        borderColor: 'rgba(255,255,255,0.25)',
                      },
                      '&.Mui-focused fieldset': {
                        borderColor: 'rgba(66, 165, 245, 0.5)',
                      },
                    },
                    '& .MuiInputLabel-root': {
                      color: 'rgba(255,255,255,0.7)',
                    },
                    '& .MuiFormHelperText-root': {
                      color: 'rgba(255,255,255,0.5)',
                      fontSize: '0.75rem',
                    },
                  }}
                />

                <Box sx={{ mt: 4 }}>
                  <GradientButton
                    type="submit"
                    fullWidth
                    size="large"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? 'Creating Account...' : 'Create Account'}
                  </GradientButton>
                </Box>
              </Box>
            </motion.div>
          </Box>
        </GlassCard>
      </motion.div>
    </Container>
  );
};

export default Register;