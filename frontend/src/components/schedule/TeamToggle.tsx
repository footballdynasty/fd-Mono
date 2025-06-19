import React from 'react';
import {
  Box,
  Typography,
  ToggleButton,
  ToggleButtonGroup,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import {
  Groups as AllGamesIcon,
  Person as MyTeamIcon,
} from '@mui/icons-material';

interface TeamToggleProps {
  value: 'all' | 'selected';
  onChange: (value: 'all' | 'selected') => void;
  selectedTeamName?: string;
  loading?: boolean;
}

const TeamToggle: React.FC<TeamToggleProps> = ({
  value,
  onChange,
  selectedTeamName,
  loading = false,
}) => {
  const theme = useTheme();
  const isSmallMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  const handleChange = (
    _event: React.MouseEvent<HTMLElement>,
    newValue: 'all' | 'selected'
  ) => {
    if (newValue !== null) {
      onChange(newValue);
    }
  };

  return (
    <Box sx={{ flex: isMobile ? 1 : 'none' }}>
      <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
        View
      </Typography>
      <ToggleButtonGroup
        value={value}
        exclusive
        onChange={handleChange}
        size="small"
        disabled={loading}
        sx={{
          '& .MuiToggleButton-root': {
            background: 'rgba(255,255,255,0.1)',
            color: 'text.primary',
            border: '1px solid rgba(255,255,255,0.3)',
            '&.Mui-selected': {
              background: 'linear-gradient(135deg, #1e88e5, #42a5f5)',
              color: 'white',
            },
            '&:hover': {
              background: 'rgba(255,255,255,0.2)',
            },
          },
        }}
      >
        <ToggleButton value="all">
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <AllGamesIcon sx={{ fontSize: 16 }} />
            {!isSmallMobile && 'All Games'}
          </Box>
        </ToggleButton>
        <ToggleButton 
          value="selected" 
          disabled={!selectedTeamName}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <MyTeamIcon sx={{ fontSize: 16 }} />
            {!isSmallMobile && (selectedTeamName ? 'My Team' : 'No Team')}
          </Box>
        </ToggleButton>
      </ToggleButtonGroup>
    </Box>
  );
};

export default TeamToggle;