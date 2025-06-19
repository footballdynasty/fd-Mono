import React from 'react';
import { Box } from '@mui/material';
import { motion, PanInfo } from 'framer-motion';

interface SwipeNavigationProps {
  onSwipeLeft: () => void;
  onSwipeRight: () => void;
  children: React.ReactNode;
  disabled?: boolean;
  swipeThreshold?: number;
}

const SwipeNavigation: React.FC<SwipeNavigationProps> = ({
  onSwipeLeft,
  onSwipeRight,
  children,
  disabled = false,
  swipeThreshold = 100,
}) => {
  const handlePanEnd = (event: any, info: PanInfo) => {
    if (disabled) return;
    
    const { offset, velocity } = info;
    
    // Check if swipe was significant enough (either distance or velocity)
    const significantSwipe = Math.abs(offset.x) > swipeThreshold || Math.abs(velocity.x) > 500;
    
    if (significantSwipe) {
      if (offset.x > 0) {
        // Swiped right (previous week)
        onSwipeRight();
      } else {
        // Swiped left (next week)
        onSwipeLeft();
      }
    }
  };
  
  return (
    <motion.div
      drag="x"
      dragConstraints={{ left: 0, right: 0 }}
      dragElastic={0.2}
      onPanEnd={handlePanEnd}
      dragMomentum={false}
      style={{
        width: '100%',
        cursor: disabled ? 'default' : 'grab',
      }}
      whileDrag={{ cursor: 'grabbing' }}
    >
      <Box sx={{ width: '100%', userSelect: 'none' }}>
        {children}
      </Box>
    </motion.div>
  );
};

export default SwipeNavigation;