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
  alpha,
  InputAdornment,
  IconButton,
  Switch,
  FormControlLabel,
} from '@mui/material';
import {
  Search as SearchIcon,
  Clear as ClearIcon,
  FilterList as FilterIcon,
  Refresh as RefreshIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { useSearchParams } from 'react-router-dom';
import { useDebounce } from '../../hooks/useDebounce';
import { useConferences } from '../../hooks/useTeams';
import GlassCard from '../ui/GlassCard';

export interface TeamsFiltersState {
  conference: string | null;
  search: string;
  humanOnly: boolean;
  sortBy: 'name' | 'conference' | 'wins' | 'losses' | 'winPercentage';
  sortOrder: 'asc' | 'desc';
}

interface TeamsFiltersProps {
  filters: TeamsFiltersState;
  onFiltersChange: (filters: TeamsFiltersState) => void;
  loading?: boolean;
}

const TeamsFilters: React.FC<TeamsFiltersProps> = ({
  filters,
  onFiltersChange,
  loading = false,
}) => {
  const theme = useTheme();
  const [searchParams, setSearchParams] = useSearchParams();
  const { data: conferences = [], isLoading: conferencesLoading } = useConferences();

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
    
    if (filters.conference) {
      params.set('conference', filters.conference);
    }
    if (filters.search) {
      params.set('search', filters.search);
    }
    if (filters.humanOnly) {
      params.set('humanOnly', 'true');
    }
    if (filters.sortBy !== 'name') {
      params.set('sortBy', filters.sortBy);
    }
    if (filters.sortOrder !== 'asc') {
      params.set('sortOrder', filters.sortOrder);
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
    const conferenceParam = searchParams.get('conference');
    const searchParam = searchParams.get('search');
    const humanOnlyParam = searchParams.get('humanOnly') === 'true';
    const sortByParam = searchParams.get('sortBy') as TeamsFiltersState['sortBy'];
    const sortOrderParam = searchParams.get('sortOrder') as TeamsFiltersState['sortOrder'];

    const initialFilters: TeamsFiltersState = {
      conference: conferenceParam || null,
      search: searchParam || '',
      humanOnly: humanOnlyParam,
      sortBy: sortByParam || 'name',
      sortOrder: sortOrderParam || 'asc',
    };

    // Only update if different from current filters
    const hasChanges = Object.keys(initialFilters).some(
      key => initialFilters[key as keyof TeamsFiltersState] !== filters[key as keyof TeamsFiltersState]
    );

    if (hasChanges) {
      onFiltersChange(initialFilters);
      setLocalSearch(initialFilters.search);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Only run on mount

  const handleConferenceChange = useCallback((conference: string | null) => {
    onFiltersChange({
      ...filters,
      conference,
    });
  }, [filters, onFiltersChange]);

  const handleSearchChange = useCallback((search: string) => {
    setLocalSearch(search);
  }, []);

  const handleHumanOnlyChange = useCallback((humanOnly: boolean) => {
    onFiltersChange({
      ...filters,
      humanOnly,
    });
  }, [filters, onFiltersChange]);

  const handleSortChange = useCallback((sortBy: TeamsFiltersState['sortBy'], sortOrder?: TeamsFiltersState['sortOrder']) => {
    onFiltersChange({
      ...filters,
      sortBy,
      sortOrder: sortOrder || filters.sortOrder,
    });
  }, [filters, onFiltersChange]);

  const handleClearFilters = useCallback(() => {
    const clearedFilters: TeamsFiltersState = {
      conference: null,
      search: '',
      humanOnly: false,
      sortBy: 'name',
      sortOrder: 'asc',
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
    filters.humanOnly,
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

  const sortOptions = [
    { value: 'name', label: 'Team Name' },
    { value: 'conference', label: 'Conference' },
    { value: 'wins', label: 'Wins' },
    { value: 'losses', label: 'Losses' },
    { value: 'winPercentage', label: 'Win %' },
  ];

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
                Filter Teams
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
            {/* Conference Filter */}
            <Grid item xs={12} sm={6} md={3}>
              <motion.div variants={itemVariants}>
                <Autocomplete
                  value={filters.conference}
                  onChange={(_, newValue) => handleConferenceChange(newValue)}
                  options={['All Conferences', ...conferences]}
                  getOptionLabel={(option) => option}
                  disabled={loading || conferencesLoading}
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

            {/* Sort By */}
            <Grid item xs={12} sm={6} md={3}>
              <motion.div variants={itemVariants}>
                <FormControl fullWidth>
                  <InputLabel sx={{ color: 'text.secondary' }}>Sort By</InputLabel>
                  <Select
                    value={filters.sortBy}
                    label="Sort By"
                    onChange={(e) => handleSortChange(e.target.value as TeamsFiltersState['sortBy'])}
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
                    {sortOptions.map((option) => (
                      <MenuItem key={option.value} value={option.value}>
                        {option.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </motion.div>
            </Grid>

            {/* Sort Order */}
            <Grid item xs={12} sm={6} md={2}>
              <motion.div variants={itemVariants}>
                <FormControl fullWidth>
                  <InputLabel sx={{ color: 'text.secondary' }}>Order</InputLabel>
                  <Select
                    value={filters.sortOrder}
                    label="Order"
                    onChange={(e) => handleSortChange(filters.sortBy, e.target.value as TeamsFiltersState['sortOrder'])}
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
                    <MenuItem value="asc">Ascending</MenuItem>
                    <MenuItem value="desc">Descending</MenuItem>
                  </Select>
                </FormControl>
              </motion.div>
            </Grid>

            {/* Search Filter */}
            <Grid item xs={12} sm={6} md={4}>
              <motion.div variants={itemVariants}>
                <TextField
                  fullWidth
                  label="Search Teams"
                  placeholder="Search by team name or coach..."
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

          {/* Human Teams Toggle */}
          <Box sx={{ mt: 2, pt: 2, borderTop: `1px solid ${alpha('#ffffff', 0.1)}` }}>
            <motion.div variants={itemVariants}>
              <FormControlLabel
                control={
                  <Switch
                    checked={filters.humanOnly}
                    onChange={(e) => handleHumanOnlyChange(e.target.checked)}
                    disabled={loading}
                    sx={{
                      '& .MuiSwitch-switchBase.Mui-checked': {
                        color: theme.palette.primary.main,
                      },
                      '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
                        backgroundColor: theme.palette.primary.main,
                      },
                    }}
                  />
                }
                label={
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <PersonIcon sx={{ fontSize: '1rem', color: 'text.secondary' }} />
                    <Typography variant="body2" color="text.secondary">
                      Show only human-controlled teams
                    </Typography>
                  </Box>
                }
              />
            </motion.div>
          </Box>

          {/* Active Filters Display */}
          {(filters.conference || filters.search || filters.humanOnly) && (
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
                {filters.humanOnly && (
                  <Chip
                    icon={<PersonIcon />}
                    label="Human Teams Only"
                    onDelete={() => handleHumanOnlyChange(false)}
                    size="small"
                    sx={{
                      background: alpha(theme.palette.success.main, 0.2),
                      border: `1px solid ${alpha(theme.palette.success.main, 0.3)}`,
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

export default TeamsFilters;