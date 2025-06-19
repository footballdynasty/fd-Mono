import React, { useState, useMemo } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  Typography,
  Box,
  Skeleton,
  Alert,
  Chip,
  Avatar,
  useTheme,
  useMediaQuery,
  alpha,
} from '@mui/material';
import { motion } from 'framer-motion';
import { Standing } from '../../types';
import GlassCard from '../ui/GlassCard';

type SortOrder = 'asc' | 'desc';
type SortableField = 'rank' | 'wins' | 'losses' | 'win_percentage' | 'conference_wins' | 'conference_losses' | 'conference_win_percentage';

interface StandingsTableProps {
  standings: Standing[];
  loading?: boolean;
  error?: string | null;
  showConferenceStats?: boolean;
  compact?: boolean;
}

interface TableColumn {
  id: SortableField | 'team' | 'conference_record';
  label: string;
  minWidth?: number;
  sortable?: boolean;
  align?: 'left' | 'center' | 'right';
  hideOnMobile?: boolean;
  format?: (value: any, row: Standing) => React.ReactNode;
}

const StandingsTable: React.FC<StandingsTableProps> = ({
  standings,
  loading = false,
  error = null,
  showConferenceStats = true,
  compact = false,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const isTablet = useMediaQuery(theme.breakpoints.down('lg'));

  const [sortBy, setSortBy] = useState<SortableField>('rank');
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc');

  // Define table columns
  const columns: TableColumn[] = useMemo(() => [
    {
      id: 'rank',
      label: 'Rank',
      minWidth: 60,
      sortable: true,
      align: 'center',
      format: (value: number | null, row: Standing) => (
        value ? (
          <Chip
            label={`#${value}`}
            size="small"
            sx={{
              background: value <= 25 
                ? 'linear-gradient(135deg, #4caf50 0%, #66bb6a 100%)'
                : 'linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0.1) 100%)',
              color: '#ffffff',
              fontWeight: 600,
              minWidth: '45px',
            }}
          />
        ) : (
          <Typography variant="body2" color="text.secondary">
            NR
          </Typography>
        )
      ),
    },
    {
      id: 'team',
      label: 'Team',
      minWidth: 200,
      sortable: false,
      align: 'left',
      format: (_, row: Standing) => (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Avatar
            src={row.team.imageUrl}
            alt={row.team.name}
            sx={{ 
              width: compact ? 24 : 32, 
              height: compact ? 24 : 32,
              border: `1px solid ${alpha('#ffffff', 0.2)}`,
            }}
          >
            {row.team.name.charAt(0)}
          </Avatar>
          <Box>
            <Typography 
              variant={compact ? 'body2' : 'body1'} 
              fontWeight={600}
              color="text.primary"
            >
              {row.team.name}
            </Typography>
            {!isMobile && (
              <Typography 
                variant="caption" 
                color="text.secondary"
                sx={{ display: 'block' }}
              >
                {row.team.conference}
              </Typography>
            )}
          </Box>
        </Box>
      ),
    },
    {
      id: 'wins',
      label: 'Wins',
      minWidth: 60,
      sortable: true,
      align: 'center',
      hideOnMobile: compact,
    },
    {
      id: 'losses',
      label: 'Losses',
      minWidth: 60,
      sortable: true,
      align: 'center',
      hideOnMobile: compact,
    },
    {
      id: 'win_percentage',
      label: 'Win %',
      minWidth: 80,
      sortable: true,
      align: 'center',
      format: (value: number) => (
        <Typography 
          variant={compact ? 'body2' : 'body1'} 
          fontWeight={600}
          color={value >= 0.75 ? 'success.main' : value >= 0.5 ? 'warning.main' : 'error.main'}
        >
          {(value * 100).toFixed(1)}%
        </Typography>
      ),
    },
    ...(showConferenceStats ? [
      {
        id: 'conference_record' as const,
        label: 'Conf W-L',
        minWidth: 90,
        sortable: false,
        align: 'center' as const,
        hideOnMobile: isTablet,
        format: (_: any, row: Standing) => (
          <Typography variant={compact ? 'body2' : 'body1'}>
            {row.conference_wins}-{row.conference_losses}
          </Typography>
        ),
      },
      {
        id: 'conference_win_percentage' as const,
        label: 'Conf %',
        minWidth: 80,
        sortable: true,
        align: 'center' as const,
        hideOnMobile: isTablet,
        format: (value: number) => (
          <Typography 
            variant={compact ? 'body2' : 'body1'} 
            fontWeight={600}
            color={value >= 0.75 ? 'success.main' : value >= 0.5 ? 'warning.main' : 'error.main'}
          >
            {(value * 100).toFixed(1)}%
          </Typography>
        ),
      },
    ] : []),
  ], [showConferenceStats, compact, isMobile, isTablet]);

  // Visible columns based on screen size
  const visibleColumns = useMemo(() => 
    columns.filter(col => !(isMobile && col.hideOnMobile)),
    [columns, isMobile]
  );

  // Sort data
  const sortedStandings = useMemo(() => {
    if (!standings?.length) return [];

    const sorted = [...standings].sort((a, b) => {
      let aVal: any;
      let bVal: any;

      switch (sortBy) {
        case 'rank':
          aVal = a.rank || 999;
          bVal = b.rank || 999;
          break;
        case 'wins':
          aVal = a.wins;
          bVal = b.wins;
          break;
        case 'losses':
          aVal = a.losses;
          bVal = b.losses;
          break;
        case 'win_percentage':
          aVal = a.win_percentage;
          bVal = b.win_percentage;
          break;
        case 'conference_wins':
          aVal = a.conference_wins;
          bVal = b.conference_wins;
          break;
        case 'conference_losses':
          aVal = a.conference_losses;
          bVal = b.conference_losses;
          break;
        case 'conference_win_percentage':
          aVal = a.conference_win_percentage;
          bVal = b.conference_win_percentage;
          break;
        default:
          return 0;
      }

      if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
      return 0;
    });

    return sorted;
  }, [standings, sortBy, sortOrder]);

  const handleSort = (field: SortableField) => {
    const isAsc = sortBy === field && sortOrder === 'asc';
    setSortOrder(isAsc ? 'desc' : 'asc');
    setSortBy(field);
  };

  const tableVariants = {
    initial: { opacity: 0, y: 20 },
    animate: { 
      opacity: 1, 
      y: 0,
      transition: { 
        duration: 0.4,
        staggerChildren: 0.05
      }
    },
  };

  const rowVariants = {
    initial: { opacity: 0, x: -20 },
    animate: { opacity: 1, x: 0 },
    hover: { 
      backgroundColor: alpha('#ffffff', 0.08),
      transition: { duration: 0.2 }
    },
  };

  if (error) {
    return (
      <GlassCard>
        <Alert 
          severity="error" 
          sx={{ 
            background: 'transparent',
            border: `1px solid ${theme.palette.error.main}40`,
          }}
        >
          {error}
        </Alert>
      </GlassCard>
    );
  }

  return (
    <GlassCard hover={false}>
      <TableContainer
        component={motion.div}
        variants={tableVariants}
        initial="initial"
        animate="animate"
        sx={{ 
          maxHeight: compact ? 400 : 600,
          '&::-webkit-scrollbar': {
            width: '8px',
            height: '8px',
          },
          '&::-webkit-scrollbar-track': {
            background: alpha('#ffffff', 0.1),
            borderRadius: '8px',
          },
          '&::-webkit-scrollbar-thumb': {
            background: alpha('#ffffff', 0.3),
            borderRadius: '8px',
            '&:hover': {
              background: alpha('#ffffff', 0.4),
            },
          },
        }}
      >
        <Table stickyHeader size={compact ? 'small' : 'medium'}>
          <TableHead>
            <TableRow>
              {visibleColumns.map((column) => (
                <TableCell
                  key={column.id}
                  align={column.align}
                  style={{ minWidth: column.minWidth }}
                  sx={{
                    background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0.1) 100%)',
                    backdropFilter: 'blur(20px)',
                    borderBottom: `1px solid ${alpha('#ffffff', 0.1)}`,
                    fontWeight: 700,
                    fontSize: compact ? '0.75rem' : '0.875rem',
                    color: 'text.primary',
                  }}
                >
                  {column.sortable ? (
                    <TableSortLabel
                      active={sortBy === column.id}
                      direction={sortBy === column.id ? sortOrder : 'asc'}
                      onClick={() => handleSort(column.id as SortableField)}
                      sx={{
                        color: 'text.primary !important',
                        '&.Mui-active': {
                          color: 'primary.main !important',
                        },
                        '& .MuiTableSortLabel-icon': {
                          color: 'primary.main !important',
                        },
                      }}
                    >
                      {column.label}
                    </TableSortLabel>
                  ) : (
                    column.label
                  )}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              // Loading skeleton
              Array.from({ length: 10 }).map((_, index) => (
                <TableRow key={`skeleton-${index}`}>
                  {visibleColumns.map((column) => (
                    <TableCell key={column.id} align={column.align}>
                      <Skeleton
                        variant="text"
                        width={column.id === 'team' ? '80%' : '60%'}
                        height={compact ? 20 : 24}
                        sx={{ background: alpha('#ffffff', 0.1) }}
                      />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              sortedStandings.map((standing, index) => (
                <TableRow
                  key={standing.id}
                  component={motion.tr}
                  variants={rowVariants}
                  initial="initial"
                  animate="animate"
                  whileHover="hover"
                  sx={{
                    cursor: 'pointer',
                    '&:hover': {
                      backgroundColor: alpha('#ffffff', 0.08),
                    },
                  }}
                  custom={index}
                >
                  {visibleColumns.map((column) => (
                    <TableCell
                      key={column.id}
                      align={column.align}
                      sx={{
                        borderBottom: `1px solid ${alpha('#ffffff', 0.05)}`,
                        padding: compact ? '8px 12px' : '12px 16px',
                      }}
                    >
                      {column.format 
                        ? column.format((standing as any)[column.id], standing)
                        : String((standing as any)[column.id] || '')
                      }
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>

        {!loading && sortedStandings.length === 0 && (
          <Box 
            sx={{ 
              display: 'flex', 
              justifyContent: 'center', 
              alignItems: 'center',
              minHeight: 200,
              flexDirection: 'column',
              gap: 2,
            }}
          >
            <Typography variant="h6" color="text.secondary">
              No standings data available
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Try adjusting your filters or check back later
            </Typography>
          </Box>
        )}
      </TableContainer>
    </GlassCard>
  );
};

export default StandingsTable;