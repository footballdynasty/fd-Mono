import React, { useState, useCallback, useEffect } from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Autocomplete,
  Button,
  Grid,
  Typography,
  Chip,
  useTheme,
  // useMediaQuery, // Future use for responsive features
  alpha,
  InputAdornment,
  IconButton,
} from '@mui/material';
import {
  Search as SearchIcon,
  Clear as ClearIcon,
  FilterList as FilterIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useSearchParams } from 'react-router-dom';
import { useDebounce } from '../../hooks/useDebounce';
import GlassCard from '../ui/GlassCard';

export interface StandingsFiltersState {
  year: number | null;
  conference: string | null;
  search: string;
}

interface StandingsFiltersProps {
  filters: StandingsFiltersState;
  onFiltersChange: (filters: StandingsFiltersState) => void;
  loading?: boolean;
  availableYears?: number[];
  availableConferences?: string[];
}

// Default conferences - can be overridden by props
const DEFAULT_CONFERENCES = [
  'ACC',
  'Big 10',
  'Big 12',
  'Pac-12',
  'SEC',
  'AAC',
  'C-USA',
  'MAC',
  'Mountain West',
  'Sun Belt',
  'FBS Independents',
];

// Generate available years (current year back to 2000)
const generateAvailableYears = (): number[] => {
  const currentYear = new Date().getFullYear();
  const years: number[] = [];
  for (let year = currentYear; year >= 2000; year--) {
    years.push(year);
  }
  return years;
};

const StandingsFilters: React.FC<StandingsFiltersProps> = ({
  filters,
  onFiltersChange,
  loading = false,
  availableYears = generateAvailableYears(),
  availableConferences = DEFAULT_CONFERENCES,
}) => {
  const theme = useTheme();
  // const isMobile = useMediaQuery(theme.breakpoints.down('md')); // Future use for mobile-specific features
  const [searchParams, setSearchParams] = useSearchParams();

  // Local state for search input to handle debouncing
  const [localSearch, setLocalSearch] = useState(filters.search);
  const debouncedSearch = useDebounce(localSearch, 300);

  // Update filters when debounced search changes
  useEffect(() => {
    if (debouncedSearch !== filters.search) {
      onFiltersChange({
        ...filters,
        search: debouncedSearch,
      });
    }
  }, [debouncedSearch, filters, onFiltersChange]);

  // Sync filters with URL parameters
  useEffect(() => {
    const params = new URLSearchParams();
    
    if (filters.year) {
      params.set('year', filters.year.toString());
    }
    if (filters.conference) {
      params.set('conference', filters.conference);
    }
    if (filters.search) {
      params.set('search', filters.search);
    }

    // Only update URL if it's different
    const currentParams = searchParams.toString();
    const newParams = params.toString();
    if (currentParams !== newParams) {
      setSearchParams(params, { replace: true });
    }
  }, [filters, searchParams, setSearchParams]);

  // Initialize filters from URL on mount
  useEffect(() => {
    const yearParam = searchParams.get('year');
    const conferenceParam = searchParams.get('conference');
    const searchParam = searchParams.get('search');

    const initialFilters: StandingsFiltersState = {
      year: yearParam ? parseInt(yearParam, 10) : new Date().getFullYear(),
      conference: conferenceParam || null,
      search: searchParam || '',
    };

    // Only update if different from current filters
    if (
      initialFilters.year !== filters.year ||
      initialFilters.conference !== filters.conference ||
      initialFilters.search !== filters.search
    ) {
      onFiltersChange(initialFilters);
      setLocalSearch(initialFilters.search);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only run on mount - dependencies intentionally omitted

  const handleYearChange = useCallback((year: number | null) => {
    onFiltersChange({
      ...filters,
      year,
    });
  }, [filters, onFiltersChange]);

  const handleConferenceChange = useCallback((conference: string | null) => {
    onFiltersChange({
      ...filters,
      conference,
    });
  }, [filters, onFiltersChange]);

  const handleSearchChange = useCallback((search: string) => {
    setLocalSearch(search);
  }, []);

  const handleClearFilters = useCallback(() => {
    const clearedFilters: StandingsFiltersState = {
      year: new Date().getFullYear(),
      conference: null,
      search: '',
    };
    onFiltersChange(clearedFilters);
    setLocalSearch('');
  }, [onFiltersChange]);

  const handleRefresh = useCallback(() => {
    // Trigger a refresh by calling onFiltersChange with current filters
    onFiltersChange({ ...filters });
  }, [filters, onFiltersChange]);

  // Count active filters
  const activeFiltersCount = [
    filters.conference,
    filters.search,
  ].filter(Boolean).length;

  const containerVariants = {
    initial: { opacity: 0, y: -20 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { 
        duration: 0.4,
        staggerChildren: 0.1
      }
    },
  };

  const itemVariants = {
    initial: { opacity: 0, x: -20 },
    animate: { opacity: 1, x: 0 },
  };

  return (
    <motion.div
      variants={containerVariants}
      initial="initial"
      animate="animate"
    >
      <GlassCard sx={{ mb: 3 }}>
      <Box sx={{ p: 3 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <FilterIcon sx={{ color: 'primary.main' }} />
            <Typography variant="h6" fontWeight={600}>
              Filter Standings
            </Typography>
            {activeFiltersCount > 0 && (
              <Chip
                label={`${activeFiltersCount} active`}
                size="small"
                color="primary"
                sx={{ 
                  background: theme.palette.gradient.primary,
                  color: '#ffffff',
                  fontWeight: 600,
                }}
              />
            )}
          </Box>

          <Box sx={{ display: 'flex', gap: 1 }}>
            <IconButton
              onClick={handleRefresh}
              disabled={loading}
              sx={{
                color: 'primary.main',
                '&:hover': {
                  backgroundColor: alpha(theme.palette.primary.main, 0.1),
                },
              }}
            >
              <RefreshIcon />
            </IconButton>
            {activeFiltersCount > 0 && (
              <Button
                variant="outlined"
                size="small"
                onClick={handleClearFilters}
                startIcon={<ClearIcon />}
                sx={{
                  borderColor: alpha('#ffffff', 0.3),
                  color: 'text.secondary',
                  '&:hover': {
                    borderColor: alpha('#ffffff', 0.5),
                    backgroundColor: alpha('#ffffff', 0.05),
                  },
                }}
              >
                Clear All
              </Button>
            )}
          </Box>
        </Box>

        {/* Filter Controls */}
        <Grid container spacing={3}>
          {/* Year Filter */}
          <Grid item xs={12} sm={6} md={3}>
            <motion.div variants={itemVariants}>
              <FormControl fullWidth>
                <InputLabel sx={{ color: 'text.secondary' }}>Year</InputLabel>
                <Select
                  value={filters.year || ''}
                  label="Year"
                  onChange={(e) => handleYearChange(e.target.value as number)}
                  disabled={loading}
                  sx={{
                    '& .MuiSelect-select': {
                      color: 'text.primary',
                    },
                    '& .MuiOutlinedInput-notchedOutline': {
                      borderColor: alpha('#ffffff', 0.2),
                    },
                    '&:hover .MuiOutlinedInput-notchedOutline': {
                      borderColor: alpha('#ffffff', 0.3),
                    },
                    '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                      borderColor: 'primary.main',
                    },
                  }}
                >
                  {availableYears.map((year) => (
                    <MenuItem key={year} value={year}>
                      {year}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </motion.div>
          </Grid>

          {/* Conference Filter */}
          <Grid item xs={12} sm={6} md={4}>
            <motion.div variants={itemVariants}>
              <Autocomplete
                value={filters.conference}
                onChange={(_, newValue) => handleConferenceChange(newValue)}
                options={['All Conferences', ...availableConferences]}
                getOptionLabel={(option) => option}
                disabled={loading}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Conference"
                    placeholder="Select conference..."
                    sx={{
                      '& .MuiInputLabel-root': {
                        color: 'text.secondary',
                      },
                      '& .MuiOutlinedInput-root': {
                        '& fieldset': {
                          borderColor: alpha('#ffffff', 0.2),
                        },
                        '&:hover fieldset': {
                          borderColor: alpha('#ffffff', 0.3),
                        },
                        '&.Mui-focused fieldset': {
                          borderColor: 'primary.main',
                        },
                      },
                    }}
                  />
                )}
                renderOption={(props, option) => (
                  <Box
                    component="li"
                    {...props}
                    sx={{
                      backgroundColor: 'background.paper',
                      '&:hover': {
                        backgroundColor: alpha(theme.palette.primary.main, 0.1),
                      },
                      '&[aria-selected="true"]': {
                        backgroundColor: alpha(theme.palette.primary.main, 0.2),
                      },
                    }}
                  >
                    {option === 'All Conferences' ? (
                      <Typography sx={{ fontStyle: 'italic', color: 'text.secondary' }}>
                        {option}
                      </Typography>
                    ) : (
                      option
                    )}
                  </Box>
                )}
              />
            </motion.div>
          </Grid>

          {/* Search Filter */}
          <Grid item xs={12} sm={12} md={5}>
            <motion.div variants={itemVariants}>
              <TextField
                fullWidth
                label="Search Teams"
                placeholder="Search by team name..."
                value={localSearch}
                onChange={(e) => handleSearchChange(e.target.value)}
                disabled={loading}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: 'text.secondary' }} />
                    </InputAdornment>
                  ),
                  endAdornment: localSearch && (
                    <InputAdornment position="end">
                      <IconButton
                        size="small"
                        onClick={() => handleSearchChange('')}
                        sx={{ color: 'text.secondary' }}
                      >
                        <ClearIcon />
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
                sx={{
                  '& .MuiInputLabel-root': {
                    color: 'text.secondary',
                  },
                  '& .MuiOutlinedInput-root': {
                    '& fieldset': {
                      borderColor: alpha('#ffffff', 0.2),
                    },
                    '&:hover fieldset': {
                      borderColor: alpha('#ffffff', 0.3),
                    },
                    '&.Mui-focused fieldset': {
                      borderColor: 'primary.main',
                    },
                  },
                }}
              />
            </motion.div>
          </Grid>
        </Grid>

        {/* Active Filters Display */}
        {(filters.conference || filters.search) && (
          <Box sx={{ mt: 2, pt: 2, borderTop: `1px solid ${alpha('#ffffff', 0.1)}` }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              Active Filters:
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {filters.conference && filters.conference !== 'All Conferences' && (
                <Chip
                  label={`Conference: ${filters.conference}`}
                  onDelete={() => handleConferenceChange(null)}
                  size="small"
                  sx={{
                    background: alpha(theme.palette.secondary.main, 0.2),
                    border: `1px solid ${alpha(theme.palette.secondary.main, 0.3)}`,
                    color: 'text.primary',
                  }}
                />
              )}
              {filters.search && (
                <Chip
                  label={`Search: "${filters.search}"`}
                  onDelete={() => handleSearchChange('')}
                  size="small"
                  sx={{
                    background: alpha(theme.palette.info.main, 0.2),
                    border: `1px solid ${alpha(theme.palette.info.main, 0.3)}`,
                    color: 'text.primary',
                  }}
                />
              )}
            </Box>
          </Box>
        )}
      </Box>
      </GlassCard>
    </motion.div>
  );
};

export default StandingsFilters;