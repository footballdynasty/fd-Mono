import React from 'react';
import { Chip, Box, Typography } from '@mui/material';
import { 
  CheckCircle as CompletedIcon,
  RadioButtonChecked as LiveIcon,
  Schedule as ScheduledIcon,
  Cancel as CancelledIcon,
} from '@mui/icons-material';
import { GameStatus as GameStatusEnum } from '../../types';

interface GameStatusProps {
  status: GameStatusEnum;
  size?: 'small' | 'medium';
  showIcon?: boolean;
  customLabel?: string;
}

const GameStatus: React.FC<GameStatusProps> = ({
  status,
  size = 'small',
  showIcon = false,
  customLabel,
}) => {
  const getStatusConfig = (status: GameStatusEnum) => {
    switch (status) {
      case GameStatusEnum.COMPLETED:
        return {
          color: '#4caf50',
          backgroundColor: 'rgba(76, 175, 80, 0.1)',
          label: 'Final',
          icon: <CompletedIcon sx={{ fontSize: 16 }} />,
        };
      case GameStatusEnum.IN_PROGRESS:
        return {
          color: '#ff9800',
          backgroundColor: 'rgba(255, 152, 0, 0.1)',
          label: 'Live',
          icon: <LiveIcon sx={{ fontSize: 16 }} />,
          pulse: true,
        };
      case GameStatusEnum.CANCELLED:
        return {
          color: '#f44336',
          backgroundColor: 'rgba(244, 67, 54, 0.1)',
          label: 'Cancelled',
          icon: <CancelledIcon sx={{ fontSize: 16 }} />,
        };
      default: // SCHEDULED
        return {
          color: '#2196f3',
          backgroundColor: 'rgba(33, 150, 243, 0.1)',
          label: 'Scheduled',
          icon: <ScheduledIcon sx={{ fontSize: 16 }} />,
        };
    }
  };

  const config = getStatusConfig(status);
  const displayLabel = customLabel || config.label;

  return (
    <Chip
      label={
        showIcon ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            {config.icon}
            <Typography 
              variant="caption" 
              sx={{ fontWeight: 600, fontSize: size === 'medium' ? '0.85rem' : '0.75rem' }}
            >
              {displayLabel}
            </Typography>
          </Box>
        ) : (
          displayLabel
        )
      }
      size={size}
      sx={{
        background: config.backgroundColor,
        color: config.color,
        fontWeight: 600,
        fontSize: size === 'medium' ? '0.85rem' : '0.75rem',
        ...(config.pulse && {
          animation: 'pulse 2s infinite',
          '@keyframes pulse': {
            '0%, 100%': { opacity: 1 },
            '50%': { opacity: 0.7 },
          },
        }),
      }}
    />
  );
};

export default GameStatus;