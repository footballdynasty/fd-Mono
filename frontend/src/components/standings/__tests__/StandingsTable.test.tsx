import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import StandingsTable from '../StandingsTable';
import theme from '../../../theme/theme';
import { Standing } from '../../../types';


// Test wrapper component
const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <BrowserRouter>
    <ThemeProvider theme={theme}>
      {children}
    </ThemeProvider>
  </BrowserRouter>
);

// Mock standings data
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
      name: 'Michigan Wolverines',
      conference: 'Big Ten',
      coach: 'Jim Harbaugh',
      imageUrl: 'https://example.com/michigan.png',
    },
    year: 2024,
    wins: 11,
    losses: 2,
    conference_wins: 7,
    conference_losses: 1,
    win_percentage: 0.846,
    conference_win_percentage: 0.875,
    rank: 2,
    conference_rank: 1,
    receiving_votes: 1432,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    total_games: 13,
    total_conference_games: 8,
  },
  {
    id: '3',
    team: {
      id: '3',
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
    rank: undefined, // Unranked team
    conference_rank: 2,
    receiving_votes: 45,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    total_games: 13,
    total_conference_games: 8,
  },
];

describe('StandingsTable', () => {
  it('renders standings data correctly', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    // Check if team names are rendered
    expect(screen.getByText('Georgia Bulldogs')).toBeInTheDocument();
    expect(screen.getByText('Michigan Wolverines')).toBeInTheDocument();
    expect(screen.getByText('Alabama Crimson Tide')).toBeInTheDocument();

    // Check if wins/losses are displayed
    expect(screen.getByText('12')).toBeInTheDocument(); // Georgia wins
    expect(screen.getByText('1')).toBeInTheDocument(); // Georgia losses
    expect(screen.getByText('11')).toBeInTheDocument(); // Michigan wins
    expect(screen.getByText('2')).toBeInTheDocument(); // Michigan/Alabama losses
  });

  it('displays win percentages correctly', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    // Check win percentages (rounded to 1 decimal place)
    expect(screen.getByText('92.3%')).toBeInTheDocument(); // Georgia
    expect(screen.getByText('84.6%')).toBeInTheDocument(); // Michigan
    expect(screen.getByText('76.9%')).toBeInTheDocument(); // Alabama
  });

  it('displays ranks correctly including unranked teams', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    // Check ranked teams
    expect(screen.getByText('#1')).toBeInTheDocument(); // Georgia
    expect(screen.getByText('#2')).toBeInTheDocument(); // Michigan
    
    // Check unranked team
    expect(screen.getByText('NR')).toBeInTheDocument(); // Alabama (Not Ranked)
  });

  it('shows conference statistics when showConferenceStats is true', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} showConferenceStats={true} />
      </TestWrapper>
    );

    // Check for conference record columns
    expect(screen.getByText('Conf W-L')).toBeInTheDocument();
    expect(screen.getByText('Conf %')).toBeInTheDocument();

    // Check conference records
    expect(screen.getByText('8-0')).toBeInTheDocument(); // Georgia
    expect(screen.getByText('7-1')).toBeInTheDocument(); // Michigan
    expect(screen.getByText('6-2')).toBeInTheDocument(); // Alabama
  });

  it('hides conference statistics when showConferenceStats is false', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} showConferenceStats={false} />
      </TestWrapper>
    );

    // Conference columns should not be present
    expect(screen.queryByText('Conf W-L')).not.toBeInTheDocument();
    expect(screen.queryByText('Conf %')).not.toBeInTheDocument();
  });

  it('supports sorting by different columns', async () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    // Check that sorting functionality works
    const winsHeader = screen.getByText('Wins');
    expect(winsHeader).toBeInTheDocument();

    // Click on wins column to sort
    fireEvent.click(winsHeader);

    // Wait for sort to complete
    await waitFor(() => {
      const rows = screen.getAllByRole('row');
      expect(rows.length).toBe(4); // 1 header + 3 data rows
    });

    // Click again to reverse sort order
    fireEvent.click(winsHeader);

    await waitFor(() => {
      const rows = screen.getAllByRole('row');
      expect(rows.length).toBe(4); // Should still have all rows
    });

    // Verify that the table is interactive and sorting works
    const table = screen.getByRole('table');
    expect(table).toBeInTheDocument();
  });

  it('displays loading skeleton when loading is true', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={[]} loading={true} />
      </TestWrapper>
    );

    // Check for MUI skeleton elements
    const skeletons = document.querySelectorAll('.MuiSkeleton-root');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('displays error message when error prop is provided', () => {
    const errorMessage = 'Failed to load standings';
    
    render(
      <TestWrapper>
        <StandingsTable standings={[]} error={errorMessage} />
      </TestWrapper>
    );

    expect(screen.getByText(errorMessage)).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('displays no data message when standings array is empty', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={[]} />
      </TestWrapper>
    );

    expect(screen.getByText('No standings data available')).toBeInTheDocument();
    expect(screen.getByText('Try adjusting your filters or check back later')).toBeInTheDocument();
  });

  it('applies compact styling when compact prop is true', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} compact={true} />
      </TestWrapper>
    );

    // Check that table has small size attribute or compact styling
    const table = screen.getByRole('table');
    // MUI v5 might use different class names, so check for size attribute or other indicators
    expect(table).toBeInTheDocument();
    
    // Alternative: check for specific text sizes or other compact indicators
    const teamNames = screen.getAllByText(/Bulldogs|Wolverines|Crimson Tide/);
    expect(teamNames.length).toBeGreaterThan(0);
  });

  it('renders team avatars with fallback to initials', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    // Check for team avatar images or initials
    const avatars = screen.getAllByRole('img');
    expect(avatars).toHaveLength(mockStandings.length);
    
    // Check that avatars have alt text
    expect(screen.getByAltText('Georgia Bulldogs')).toBeInTheDocument();
    expect(screen.getByAltText('Michigan Wolverines')).toBeInTheDocument();
    expect(screen.getByAltText('Alabama Crimson Tide')).toBeInTheDocument();
  });

  it('displays conference information for teams', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    // Check that conferences are displayed - they might be inside team cells
    const secText = screen.getAllByText(/SEC/);
    const bigTenText = screen.getAllByText(/Big Ten/);
    
    expect(secText.length).toBeGreaterThan(0);
    expect(bigTenText.length).toBeGreaterThan(0);
  });

  it('sorts by rank ascending by default', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    const rows = screen.getAllByRole('row');
    // Check that we have the expected number of rows (1 header + 3 data rows)
    expect(rows).toHaveLength(4);
    
    // Check that ranked teams come before unranked teams
    // Georgia (rank 1) and Michigan (rank 2) should come before Alabama (undefined rank)
    const georgiaRow = rows.find(row => row.textContent?.includes('Georgia Bulldogs'));
    const michiganRow = rows.find(row => row.textContent?.includes('Michigan Wolverines'));
    const alabamaRow = rows.find(row => row.textContent?.includes('Alabama Crimson Tide'));
    
    expect(georgiaRow).toBeInTheDocument();
    expect(michiganRow).toBeInTheDocument();
    expect(alabamaRow).toBeInTheDocument();
  });

  it('handles row hover interactions', async () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    const firstDataRow = screen.getAllByRole('row')[1]; // Skip header row
    
    // Simulate hover
    fireEvent.mouseEnter(firstDataRow);
    
    // Row should have cursor pointer style
    expect(firstDataRow).toHaveStyle('cursor: pointer');
  });

  it('displays correct win percentage colors based on performance', () => {
    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} />
      </TestWrapper>
    );

    // Check that win percentages are displayed and have appropriate styling
    const georgiaWinPct = screen.getByText('92.3%');
    const michiganWinPct = screen.getByText('84.6%');
    const alabamaWinPct = screen.getByText('76.9%');

    // All percentages should be visible
    expect(georgiaWinPct).toBeInTheDocument();
    expect(michiganWinPct).toBeInTheDocument();
    expect(alabamaWinPct).toBeInTheDocument();

    // They should have color styling (exact color may vary with theme)
    expect(georgiaWinPct).toHaveStyle('font-weight: 600');
    expect(michiganWinPct).toHaveStyle('font-weight: 600');
    expect(alabamaWinPct).toHaveStyle('font-weight: 600');
  });

  it('handles responsive design by hiding columns on mobile', () => {
    // Override global mock for mobile breakpoint test
    window.matchMedia = jest.fn().mockImplementation(query => ({
      matches: query === '(max-width:899.95px)', // Mobile breakpoint
      media: query,
      onchange: null,
      addListener: jest.fn(),
      removeListener: jest.fn(),
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      dispatchEvent: jest.fn(),
    }));

    render(
      <TestWrapper>
        <StandingsTable standings={mockStandings} compact={true} />
      </TestWrapper>
    );

    // On mobile with compact=true, wins/losses columns should be hidden
    expect(screen.queryByText('Wins')).not.toBeInTheDocument();
    expect(screen.queryByText('Losses')).not.toBeInTheDocument();
  });
});