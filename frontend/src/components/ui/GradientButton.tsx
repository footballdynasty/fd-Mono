import React from 'react';
import { Button, ButtonProps, alpha } from '@mui/material';
import { motion } from 'framer-motion';

interface GradientButtonProps extends ButtonProps {
  gradient?: 'primary' | 'secondary' | 'success' | 'warning' | 'error';
  glow?: boolean;
}

const GradientButton: React.FC<GradientButtonProps> = ({ 
  children,
  gradient = 'primary',
  glow = true,
  sx,
  ...props 
}) => {
  const gradients = {
    primary: 'linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%)',
    secondary: 'linear-gradient(135deg, #26a69a 0%, #4db6ac 100%)',
    success: 'linear-gradient(135deg, #4caf50 0%, #66bb6a 100%)',
    warning: 'linear-gradient(135deg, #ff9800 0%, #ffb74d 100%)',
    error: 'linear-gradient(135deg, #f44336 0%, #e57373 100%)',
  };

  const hoverGradients = {
    primary: 'linear-gradient(135deg, #1565c0 0%, #1e88e5 100%)',
    secondary: 'linear-gradient(135deg, #00695c 0%, #26a69a 100%)',
    success: 'linear-gradient(135deg, #388e3c 0%, #4caf50 100%)',
    warning: 'linear-gradient(135deg, #f57c00 0%, #ff9800 100%)',
    error: 'linear-gradient(135deg, #d32f2f 0%, #f44336 100%)',
  };

  const glowColors = {
    primary: '#42a5f5',
    secondary: '#4db6ac',
    success: '#66bb6a',
    warning: '#ffb74d',
    error: '#e57373',
  };

  const buttonVariants = {
    initial: { scale: 1 },
    hover: { 
      scale: 1.05,
      transition: { 
        type: "spring", 
        stiffness: 400, 
        damping: 17 
      }
    },
    tap: { scale: 0.95 }
  };

  return (
    <motion.div
      variants={buttonVariants}
      initial="initial"
      whileHover="hover"
      whileTap="tap"
      style={{ display: 'inline-block' }}
    >
      <Button
        {...props}
        sx={{
          background: gradients[gradient],
          color: '#ffffff',
          borderRadius: '12px',
          textTransform: 'none',
          fontWeight: 600,
          padding: '12px 24px',
          border: 'none',
          boxShadow: glow 
            ? `0 8px 24px ${alpha(glowColors[gradient], 0.4)}`
            : '0 4px 12px rgba(0,0,0,0.2)',
          '&:hover': {
            background: hoverGradients[gradient],
            color: '#ffffff',
            boxShadow: glow 
              ? `0 12px 32px ${alpha(glowColors[gradient], 0.6)}`
              : '0 8px 24px rgba(0,0,0,0.3)',
          },
          '&:active': {
            boxShadow: glow 
              ? `0 4px 16px ${alpha(glowColors[gradient], 0.4)}`
              : '0 2px 8px rgba(0,0,0,0.2)',
          },
          '&:disabled': {
            background: alpha('#ffffff', 0.1),
            color: alpha('#ffffff', 0.3),
            boxShadow: 'none',
          },
          ...sx,
        }}
      >
        {children}
      </Button>
    </motion.div>
  );
};

export default GradientButton;