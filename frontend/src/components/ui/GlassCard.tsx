import React from 'react';
import { Card, CardProps, alpha, SxProps, Theme } from '@mui/material';
import { motion } from 'framer-motion';

interface GlassCardProps extends CardProps {
  children: React.ReactNode;
  hover?: boolean;
  gradient?: boolean;
}

const GlassCard: React.FC<GlassCardProps> = ({ 
  children, 
  hover = true, 
  gradient = false,
  sx,
  ...props 
}) => {
  const cardVariants = {
    initial: { scale: 1, y: 0 },
    hover: { 
      scale: 1.02, 
      y: -4,
      transition: { 
        type: "spring", 
        stiffness: 300, 
        damping: 20 
      }
    }
  };

  const baseStyles: SxProps<Theme> = {
    background: gradient 
      ? 'linear-gradient(145deg, rgba(30,136,229,0.15) 0%, rgba(66,165,245,0.1) 50%, rgba(255,255,255,0.05) 100%)'
      : 'linear-gradient(145deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%)',
    backdropFilter: 'blur(20px)',
    border: `1px solid ${alpha('#ffffff', 0.15)}`,
    borderRadius: '20px',
    boxShadow: '0 8px 32px rgba(0,0,0,0.3)',
    position: 'relative',
    overflow: 'hidden',
    '&::before': {
      content: '""',
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,
      height: '1px',
      background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent)',
    },
    ...sx,
  };

  if (!hover) {
    return (
      <Card sx={baseStyles} {...props}>
        {children}
      </Card>
    );
  }

  return (
    <motion.div
      variants={cardVariants}
      initial="initial"
      whileHover="hover"
      style={{ display: 'inline-block', width: '100%' }}
    >
      <Card sx={baseStyles} {...props}>
        {children}
      </Card>
    </motion.div>
  );
};

export default GlassCard;