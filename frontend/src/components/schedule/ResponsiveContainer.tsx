import React from 'react';
import { Box, useTheme, useMediaQuery } from '@mui/material';

interface ResponsiveContainerProps {
  children: React.ReactNode;
  mobileSpacing?: number;
  tabletSpacing?: number;
  desktopSpacing?: number;
  mobilePadding?: number;
  tabletPadding?: number;
  desktopPadding?: number;
}

const ResponsiveContainer: React.FC<ResponsiveContainerProps> = ({
  children,
  mobileSpacing = 1,
  tabletSpacing = 2,
  desktopSpacing = 3,
  mobilePadding = 1,
  tabletPadding = 2,
  desktopPadding = 3,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.between('sm', 'md'));
  
  const getSpacing = () => {
    if (isMobile) return mobileSpacing;
    if (isTablet) return tabletSpacing;
    return desktopSpacing;
  };
  
  const getPadding = () => {
    if (isMobile) return mobilePadding;
    if (isTablet) return tabletPadding;
    return desktopPadding;
  };
  
  return (
    <Box
      sx={{
        '& > *:not(:last-child)': {
          mb: getSpacing(),
        },
        p: getPadding(),
      }}
    >
      {children}
    </Box>
  );
};

export default ResponsiveContainer;