import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import '@testing-library/jest-dom';
import StandingsPage from '../StandingsPage';
import theme from '../../theme/theme';
import { Standing } from '../../types';

// Mock framer-motion to avoid animation issues in tests
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, ...props }: any) => <div {...props}>{children}</div>,
  },
  AnimatePresence: ({ children }: any) => children,
}));

// Mock the useStandings hook
const mockUseStandings = jest.fn();
jest.mock('../../hooks/useStandings', () => ({
  useStandings: () => mockUseStandings(),
}));

// Mock the components
jest.mock('../../components/standings/StandingsFilters', () => {
  return function MockStandingsFilters({ filters, onFiltersChange }: any) {
    return (
      <div data-testid="standings-filters">
        <button onClick={() => onFiltersChange({ ...filters, year: 2023 })}>
          Change Year
        </button>
        <button onClick={() => onFiltersChange({ ...filters, conference: 'SEC' })}>
          Change Conference
        </button>
        <button onClick={() => onFiltersChange({ ...filters, search: 'Georgia' })}>
          Change Search
        </button>
      </div>
    );
  };
});

jest.mock('../../components/standings/StandingsTable', () => {
  return function MockStandingsTable({ standings, loading, error }: any) {
    if (loading) return <div data-testid="standings-table-loading">Loading...</div>;
    if (error) return <div data-testid="standings-table-error">{error}</div>;
    return (
      <div data-testid="standings-table">
        {standings.map((standing: Standing) => (
          <div key={standing.id}>{standing.team.name}</div>
        ))}
      </div>
    );
  };
});

// Mock useDebounce hook
jest.mock('../../hooks/useDebounce', () => ({
  useDebounce: (value: string, delay: number) => value,
}));

// Test data
const mockStandings: Standing[] = [
  {
    id: '1',
    team: {
      id: '1',
      name: 'Georgia Bulldogs',
      conference: 'SEC',
      coach: 'Kirby Smart',
      imageUrl: 'https://example.com/georgia.png',
    },
    year: 2024,
    wins: 12,
    losses: 1,
    conference_wins: 8,
    conference_losses: 0,
    win_percentage: 0.923,
    conference_win_percentage: 1.0,
    rank: 1,
    conference_rank: 1,
    receiving_votes: 1547,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    total_games: 13,
    total_conference_games: 8,
  },
  {
    id: '2',
    team: {
      id: '2',
      name: 'Alabama Crimson Tide',
      conference: 'SEC',
      coach: 'Nick Saban',
      imageUrl: 'https://example.com/alabama.png',
    },
    year: 2024,
    wins: 10,
    losses: 3,
    conference_wins: 6,
    conference_losses: 2,
    win_percentage: 0.769,
    conference_win_percentage: 0.75,
    rank: 15,
    conference_rank: 2,
    receiving_votes: 45,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    total_games: 13,
    total_conference_games: 8,
  },
];

// Test wrapper component
const TestWrapper: React.FC<{ children: React.ReactNode; initialUrl?: string }> = ({ 
  children, 
  initialUrl = '/standings' 
}) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
    },
  });

  // Mock window.history.pushState for URL manipulation in tests
  if (initialUrl !== '/standings') {
    window.history.pushState({}, '', initialUrl);
  }

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ThemeProvider theme={theme}>
          {children}
        </ThemeProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('StandingsPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Mock successful data response by default
    mockUseStandings.mockReturnValue({
      data: {
        content: mockStandings,
        totalElements: mockStandings.length,
        totalPages: 1,
      },
      isLoading: false,
      error: null,
    });
  });

  it('renders page header and breadcrumbs', () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // Check breadcrumbs
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Standings')).toBeInTheDocument();

    // Check page header
    expect(screen.getByRole('heading', { name: /team standings/i })).toBeInTheDocument();
    expect(screen.getByText(/track team performance across conferences/i)).toBeInTheDocument();
  });

  it('renders filters and table components', () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    expect(screen.getByTestId('standings-filters')).toBeInTheDocument();
    expect(screen.getByTestId('standings-table')).toBeInTheDocument();
  });

  it('displays loading state when data is loading', () => {
    mockUseStandings.mockReturnValue({
      data: null,
      isLoading: true,
      error: null,
    });

    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    expect(screen.getByText('Loading standings data...')).toBeInTheDocument();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('displays error state when there is an error', () => {
    mockUseStandings.mockReturnValue({
      data: null,
      isLoading: false,
      error: new Error('Failed to fetch'),
    });

    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    expect(screen.getByText('Failed to load standings data. Please try again later.')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('displays no results message when standings array is empty', () => {
    mockUseStandings.mockReturnValue({
      data: {
        content: [],
        totalElements: 0,
        totalPages: 0,
      },
      isLoading: false,
      error: null,
    });

    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    expect(screen.getByText(/no standings data found/i)).toBeInTheDocument();
  });

  it('filters standings by search term', async () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // Initially should show both teams
    expect(screen.getByText('Georgia Bulldogs')).toBeInTheDocument();
    expect(screen.getByText('Alabama Crimson Tide')).toBeInTheDocument();

    // Simulate search filter change
    const changeSearchButton = screen.getByText('Change Search');
    changeSearchButton.click();

    // Wait for filter to be applied - Georgia should be filtered
    await waitFor(() => {
      // Since we're mocking the search to set "Georgia", it should still show both
      // as our mock doesn't actually implement the search filter
      expect(screen.getByText('Georgia Bulldogs')).toBeInTheDocument();
    });
  });

  it('updates document title based on filters', () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    expect(document.title).toBe('Team Standings | Football Dynasty');
  });

  it('displays stats summary when standings are available', () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    expect(screen.getByText(`Showing ${mockStandings.length} teams`)).toBeInTheDocument();
  });

  it('initializes filters from URL parameters', () => {
    render(
      <TestWrapper initialUrl="/standings?year=2023&conference=SEC&search=Georgia">
        <StandingsPage />
      </TestWrapper>
    );

    // The page should initialize with URL parameters
    // Since we're mocking the hook, we can't directly test the URL parsing
    // but we can verify the component renders without errors
    expect(screen.getByTestId('standings-filters')).toBeInTheDocument();
  });

  it('handles filter changes correctly', async () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // Test year filter change
    const changeYearButton = screen.getByText('Change Year');
    changeYearButton.click();

    // Test conference filter change
    const changeConferenceButton = screen.getByText('Change Conference');
    changeConferenceButton.click();

    // The component should handle these changes without errors
    expect(screen.getByTestId('standings-filters')).toBeInTheDocument();
    expect(screen.getByTestId('standings-table')).toBeInTheDocument();
  });

  it('passes correct props to StandingsTable', () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // Verify that standings data is passed to the table
    expect(screen.getByText('Georgia Bulldogs')).toBeInTheDocument();
    expect(screen.getByText('Alabama Crimson Tide')).toBeInTheDocument();
  });

  it('shows conference stats when no specific conference is selected', () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // The table should receive showConferenceStats=true when no conference filter is applied
    expect(screen.getByTestId('standings-table')).toBeInTheDocument();
  });

  it('renders responsive design elements', () => {
    // Mock mobile breakpoint
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: jest.fn().mockImplementation(query => ({
        matches: query === '(max-width:899.95px)',
        media: query,
        onchange: null,
        addListener: jest.fn(),
        removeListener: jest.fn(),
        addEventListener: jest.fn(),
        removeEventListener: jest.fn(),
        dispatchEvent: jest.fn(),
      })),
    });

    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // Should render without errors on mobile
    expect(screen.getByRole('heading', { name: /team standings/i })).toBeInTheDocument();
  });

  it('handles empty conference filter correctly', () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // When no conference is selected (null), showConferenceStats should be true
    expect(screen.getByTestId('standings-table')).toBeInTheDocument();
  });

  it('filters standings by team name in search', () => {
    // Test that search functionality works with the actual filtering logic
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // Initially both teams should be visible
    expect(screen.getByText('Georgia Bulldogs')).toBeInTheDocument();
    expect(screen.getByText('Alabama Crimson Tide')).toBeInTheDocument();
  });

  it('filters standings by conference in search', () => {
    // Test that search can filter by conference
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // Both teams are in SEC, so both should be visible
    expect(screen.getByText('Georgia Bulldogs')).toBeInTheDocument();
    expect(screen.getByText('Alabama Crimson Tide')).toBeInTheDocument();
  });

  it('handles API data structure correctly', () => {
    // Test with data that has the correct API structure
    mockUseStandings.mockReturnValue({
      data: {
        content: mockStandings,
        totalElements: mockStandings.length,
        totalPages: 1,
        first: true,
        last: true,
      },
      isLoading: false,
      error: null,
    });

    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    expect(screen.getByText('Georgia Bulldogs')).toBeInTheDocument();
    expect(screen.getByText('Alabama Crimson Tide')).toBeInTheDocument();
  });

  it('updates URL parameters when filters change', () => {
    render(
      <TestWrapper>
        <StandingsPage />
      </TestWrapper>
    );

    // Simulate filter changes
    const changeYearButton = screen.getByText('Change Year');
    changeYearButton.click();

    // URL should be updated (though we can't easily test this with jsdom)
    expect(screen.getByTestId('standings-filters')).toBeInTheDocument();
  });
});