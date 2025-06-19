import React, { useState } from 'react';
import {
  Box,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  useTheme,
  useMediaQuery,
  SelectChangeEvent,
  Collapse,
  IconButton,
} from '@mui/material';
import {
  FilterList as FilterIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import GlassCard from '../ui/GlassCard';
import WeekNavigation from './WeekNavigation';
import TeamToggle from './TeamToggle';
import { useAuth } from '../../hooks/useAuth';

interface ScheduleFiltersState {
  year: number;
  week: number | null;
  teamView: 'all' | 'selected';
  conference: string | null;
}

interface WeekNavigationData {
  currentWeekNumber: number;
  totalWeeks: number;
  canGoPrevious: boolean;
  canGoNext: boolean;
  getWeekOptions: () => Array<{
    label: string;
    value: string;
    weekNumber: number;
    isCurrentWeek: boolean;
  }>;
  isLoading: boolean;
}

interface ScheduleFiltersProps {
  filters: ScheduleFiltersState;
  onFiltersChange: (filters: ScheduleFiltersState) => void;
  loading: boolean;
  weekNavigation: WeekNavigationData;
  currentWeek?: {
    year: number;
    currentWeek: number;
    totalWeeks: number;
    seasonProgress: number;
    weekId: string | null;
  };
}

const ScheduleFilters: React.FC<ScheduleFiltersProps> = ({
  filters,
  onFiltersChange,
  loading,
  weekNavigation,
  currentWeek,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const isSmallMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const { selectedTeam } = useAuth();
  
  // Collapsible filters for mobile
  const [filtersExpanded, setFiltersExpanded] = useState(!isMobile);

  // Week navigation helpers with proper cycling
  const handlePreviousWeek = () => {
    const currentWeekNumber = filters.week || weekNavigation.currentWeekNumber;
    let newWeek;
    
    if (currentWeekNumber <= 1) {
      // Cycle to last week
      newWeek = weekNavigation.totalWeeks;
    } else {
      newWeek = currentWeekNumber - 1;
    }
    
    onFiltersChange({ ...filters, week: newWeek });
  };

  const handleNextWeek = () => {
    const currentWeekNumber = filters.week || weekNavigation.currentWeekNumber;
    let newWeek;
    
    if (currentWeekNumber >= weekNavigation.totalWeeks) {
      // Cycle to first week
      newWeek = 1;
    } else {
      newWeek = currentWeekNumber + 1;
    }
    
    onFiltersChange({ ...filters, week: newWeek });
  };

  // Year selection
  const handleYearChange = (event: SelectChangeEvent<string>) => {
    const year = parseInt(event.target.value, 10);
    onFiltersChange({ ...filters, year });
  };

  // Conference selection
  const handleConferenceChange = (event: SelectChangeEvent<string>) => {
    const conference = event.target.value === 'all' ? null : event.target.value;
    onFiltersChange({ ...filters, conference });
  };

  // Get available years (current year and previous few years)
  const getAvailableYears = () => {
    const currentYear = new Date().getFullYear();
    return Array.from({ length: 5 }, (_, i) => currentYear - i);
  };

  // Get available conferences (mock data for now - could be fetched from API)
  const getAvailableConferences = () => [
    'ACC', 'Big 12', 'Big Ten', 'Pac-12', 'SEC', 'American', 'C-USA', 'MAC', 'Mountain West', 'Sun Belt'
  ];

  const weekOptions = weekNavigation.getWeekOptions();

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
    >
      <GlassCard>
        <Box sx={{ p: 3 }}>
          <Box sx={{ 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'space-between',
            mb: 3,
            flexWrap: 'wrap',
            gap: 2,
          }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <FilterIcon sx={{ color: 'primary.main' }} />
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Schedule Filters
              </Typography>
              {isMobile && (
                <IconButton
                  onClick={() => setFiltersExpanded(!filtersExpanded)}
                  sx={{
                    ml: 'auto',
                    background: 'rgba(255,255,255,0.1)',
                    '&:hover': { background: 'rgba(255,255,255,0.2)' },
                  }}
                >
                  {filtersExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                </IconButton>
              )}
            </Box>
            
            {/* Season Progress */}
            {currentWeek && (
              <Chip
                label={`Week ${Math.min(Math.max(filters.week || weekNavigation.currentWeekNumber, 1), weekNavigation.totalWeeks)} of ${weekNavigation.totalWeeks}`}
                size="small"
                sx={{
                  background: 'linear-gradient(135deg, #1e88e5, #42a5f5)',
                  color: 'white',
                  fontWeight: 600,
                }}
              />
            )}
          </Box>

          <Collapse in={filtersExpanded} timeout="auto" unmountOnExit>
            <Box sx={{ 
              display: 'flex', 
              flexDirection: isMobile ? 'column' : 'row',
              gap: 3,
              alignItems: isMobile ? 'stretch' : 'flex-end',
              mt: isMobile ? 2 : 0,
            }}>
            {/* Week Navigation */}
            <WeekNavigation
              currentWeek={filters.week}
              totalWeeks={weekNavigation.totalWeeks}
              canGoPrevious={weekNavigation.canGoPrevious}
              canGoNext={weekNavigation.canGoNext}
              weekOptions={weekOptions}
              onPrevious={handlePreviousWeek}
              onNext={handleNextWeek}
              onWeekSelect={(week) => onFiltersChange({ ...filters, week })}
              loading={loading || weekNavigation.isLoading}
            />

            {/* Team View Toggle */}
            <TeamToggle
              value={filters.teamView}
              onChange={(teamView) => onFiltersChange({ ...filters, teamView })}
              selectedTeamName={selectedTeam?.name}
              loading={loading}
            />

            {/* Year Selection */}
            <Box sx={{ flex: isMobile ? 1 : 'none', minWidth: 100 }}>
              <FormControl fullWidth>
                <InputLabel 
                  size="small"
                  sx={{ 
                    color: 'text.secondary',
                    '&.Mui-focused': { color: 'primary.main' },
                  }}
                >
                  Year
                </InputLabel>
                <Select
                  value={filters.year.toString()}
                  label="Year"
                  onChange={handleYearChange}
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
                  {getAvailableYears().map((year) => (
                    <MenuItem key={year} value={year.toString()}>
                      {year}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>

            {/* Conference Filter (only show when viewing all games) */}
            {filters.teamView === 'all' && (
              <Box sx={{ flex: isMobile ? 1 : 'none', minWidth: 120 }}>
                <FormControl fullWidth>
                  <InputLabel 
                    size="small"
                    sx={{ 
                      color: 'text.secondary',
                      '&.Mui-focused': { color: 'primary.main' },
                    }}
                  >
                    Conference
                  </InputLabel>
                  <Select
                    value={filters.conference || 'all'}
                    label="Conference"
                    onChange={handleConferenceChange}
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
                    <MenuItem value="all">All Conferences</MenuItem>
                    {getAvailableConferences().map((conf) => (
                      <MenuItem key={conf} value={conf}>
                        {conf}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Box>
            )}
            </Box>
          </Collapse>

          {/* Active Filters Summary */}
          <Box sx={{ mt: 3, pt: 2, borderTop: '1px solid rgba(255,255,255,0.1)' }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              Active Filters:
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              <Chip
                label={`${filters.year} Season`}
                size="small"
                variant="outlined"
                sx={{ borderColor: 'rgba(255,255,255,0.3)' }}
              />
              <Chip
                label={filters.week ? `Week ${filters.week}` : 'Current Week'}
                size="small"
                variant="outlined"
                sx={{ borderColor: 'rgba(255,255,255,0.3)' }}
              />
              <Chip
                label={filters.teamView === 'selected' 
                  ? `${selectedTeam?.name || 'Selected Team'}` 
                  : 'All Games'
                }
                size="small"
                variant="outlined"
                sx={{ borderColor: 'rgba(255,255,255,0.3)' }}
              />
              {filters.conference && (
                <Chip
                  label={filters.conference}
                  size="small"
                  variant="outlined"
                  sx={{ borderColor: 'rgba(255,255,255,0.3)' }}
                />
              )}
            </Box>
          </Box>
        </Box>
      </GlassCard>
    </motion.div>
  );
};

export default ScheduleFilters;