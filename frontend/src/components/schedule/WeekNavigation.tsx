import React from 'react';
import {
  Box,
  IconButton,
  FormControl,
  Select,
  MenuItem,
  Typography,
  SelectChangeEvent,
} from '@mui/material';
import {
  NavigateBefore as PrevIcon,
  NavigateNext as NextIcon,
  CalendarToday as CalendarIcon,
} from '@mui/icons-material';

interface WeekNavigationProps {
  currentWeek: number | null;
  totalWeeks: number;
  canGoPrevious: boolean;
  canGoNext: boolean;
  weekOptions: Array<{
    label: string;
    value: string;
    weekNumber: number;
    isCurrentWeek: boolean;
  }>;
  onPrevious: () => void;
  onNext: () => void;
  onWeekSelect: (week: number | null) => void;
  loading?: boolean;
}

const WeekNavigation: React.FC<WeekNavigationProps> = ({
  currentWeek,
  totalWeeks,
  canGoPrevious,
  canGoNext,
  weekOptions,
  onPrevious,
  onNext,
  onWeekSelect,
  loading = false,
}) => {
  const handleWeekSelect = (event: SelectChangeEvent<string>) => {
    const selectedValue = event.target.value;
    if (selectedValue === 'current') {
      onWeekSelect(null); // null means current week
    } else {
      const weekOption = weekOptions.find(w => w.value === selectedValue);
      if (weekOption) {
        onWeekSelect(weekOption.weekNumber);
      }
    }
  };

  const selectedWeekOption = weekOptions.find(w => w.weekNumber === currentWeek);

  return (
    <Box sx={{ flex: 1, minWidth: 200 }}>
      <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
        Week Navigation
      </Typography>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <IconButton
          onClick={onPrevious}
          disabled={loading}
          sx={{
            background: 'rgba(255,255,255,0.1)',
            '&:hover': { background: 'rgba(255,255,255,0.2)' },
            '&.Mui-disabled': { opacity: 0.5 },
          }}
        >
          <PrevIcon />
        </IconButton>
        
        <FormControl sx={{ minWidth: 120, flex: 1 }}>
          <Select
            value={currentWeek === null ? 'current' : selectedWeekOption?.value || 'current'}
            onChange={handleWeekSelect}
            size="small"
            disabled={loading}
            sx={{
              background: 'rgba(255,255,255,0.1)',
              '& .MuiOutlinedInput-notchedOutline': {
                borderColor: 'rgba(255,255,255,0.3)',
              },
              '&:hover .MuiOutlinedInput-notchedOutline': {
                borderColor: 'primary.main',
              },
            }}
          >
            <MenuItem value="current">
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CalendarIcon sx={{ fontSize: 16 }} />
                Current Week
              </Box>
            </MenuItem>
            {weekOptions.map((week) => (
              <MenuItem key={week.value} value={week.value}>
                {week.label}
                {week.isCurrentWeek && ' (Current)'}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        
        <IconButton
          onClick={onNext}
          disabled={loading}
          sx={{
            background: 'rgba(255,255,255,0.1)',
            '&:hover': { background: 'rgba(255,255,255,0.2)' },
            '&.Mui-disabled': { opacity: 0.5 },
          }}
        >
          <NextIcon />
        </IconButton>
      </Box>
    </Box>
  );
};

export default WeekNavigation;