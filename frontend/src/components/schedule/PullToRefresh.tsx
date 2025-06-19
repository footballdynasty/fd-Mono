import React, { useState, useRef, useEffect } from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';
import { motion, useMotionValue, useTransform, PanInfo } from 'framer-motion';

interface PullToRefreshProps {
  onRefresh: () => Promise<void>;
  children: React.ReactNode;
  disabled?: boolean;
}

const PullToRefresh: React.FC<PullToRefreshProps> = ({
  onRefresh,
  children,
  disabled = false,
}) => {
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isPulling, setIsPulling] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const y = useMotionValue(0);
  
  const pullThreshold = 80;
  const maxPull = 120;
  
  const rotate = useTransform(y, [0, pullThreshold], [0, 180]);
  const opacity = useTransform(y, [0, pullThreshold / 2, pullThreshold], [0, 0.5, 1]);
  
  const handlePanStart = () => {
    if (!disabled && !isRefreshing) {
      setIsPulling(true);
    }
  };
  
  const handlePanEnd = async (event: any, info: PanInfo) => {
    if (disabled || isRefreshing) return;
    
    const currentY = y.get();
    
    if (currentY >= pullThreshold) {
      setIsRefreshing(true);
      try {
        await onRefresh();
      } catch (error) {
        console.error('Refresh failed:', error);
      } finally {
        setIsRefreshing(false);
      }
    }
    
    setIsPulling(false);
    y.set(0);
  };
  
  const handlePan = (event: any, info: PanInfo) => {
    if (disabled || isRefreshing) return;
    
    // Only allow pulling down when at the top of the page
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    if (scrollTop > 10) return;
    
    // Only handle downward pulls
    if (info.delta.y < 0) return;
    
    const newY = Math.min(info.offset.y, maxPull);
    if (newY > 0) {
      y.set(newY);
    }
  };
  
  useEffect(() => {
    if (isRefreshing) {
      y.set(pullThreshold);
    }
  }, [isRefreshing, y]);
  
  return (
    <motion.div
      ref={containerRef}
      drag="y"
      dragConstraints={{ top: 0, bottom: maxPull }}
      dragElastic={{ top: 0.2, bottom: 0 }}
      onPanStart={handlePanStart}
      onPan={handlePan}
      onPanEnd={handlePanEnd}
      style={{ y }}
      dragMomentum={false}
    >
      {/* Pull to refresh indicator */}
      <motion.div
        style={{
          height: y,
          overflow: 'hidden',
        }}
      >
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            height: pullThreshold,
            background: 'linear-gradient(135deg, rgba(30, 136, 229, 0.1), rgba(66, 165, 245, 0.1))',
            backdropFilter: 'blur(10px)',
          }}
        >
          {isRefreshing ? (
            <>
              <CircularProgress 
                size={24} 
                sx={{ color: 'primary.main', mb: 1 }} 
              />
              <Typography variant="caption" color="text.secondary">
                Refreshing...
              </Typography>
            </>
          ) : (
            <>
              <motion.div
                style={{ rotate, opacity }}
                transition={{ duration: 0.2 }}
              >
                <Box
                  sx={{
                    width: 24,
                    height: 24,
                    borderRadius: '50%',
                    border: '2px solid',
                    borderColor: 'primary.main',
                    borderTopColor: 'transparent',
                    mb: 1,
                  }}
                />
              </motion.div>
              <motion.div style={{ opacity }}>
                <Typography variant="caption" color="text.secondary">
                  {isPulling && y.get() >= pullThreshold ? 'Release to refresh' : 'Pull to refresh'}
                </Typography>
              </motion.div>
            </>
          )}
        </Box>
      </motion.div>
      
      {children}
    </motion.div>
  );
};

export default PullToRefresh;