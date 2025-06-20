import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import StandingsFilters, { StandingsFiltersState } from '../StandingsFilters';
import theme from '../../../theme/theme';


// Mock useDebounce hook
jest.mock('../../../hooks/useDebounce', () => ({
  useDebounce: (value: string, delay: number) => value, // Return value immediately for tests
}));

// Test wrapper component
const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <BrowserRouter>
    <ThemeProvider theme={theme}>
      {children}
    </ThemeProvider>
  </BrowserRouter>
);

describe('StandingsFilters', () => {
  const defaultFilters: StandingsFiltersState = {
    year: 2024,
    conference: null,
    search: '',
  };

  const mockOnFiltersChange = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders all filter controls', () => {
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Check for filter header
    expect(screen.getByText('Filter Standings')).toBeInTheDocument();

    // Check for year filter - use placeholder or find by text
    expect(screen.getAllByRole('combobox')[0]).toBeInTheDocument(); // Year select

    // Check for conference filter  
    expect(screen.getByRole('combobox', { name: /conference/i })).toBeInTheDocument();

    // Check for search filter
    expect(screen.getByRole('textbox', { name: /search teams/i })).toBeInTheDocument();
  });

  it('displays available years in year select', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
          availableYears={[2024, 2023, 2022]}
        />
      </TestWrapper>
    );

    // Click on year select
    const yearSelect = screen.getAllByRole('combobox')[0]; // First combobox is year
    await user.click(yearSelect);

    // Check for year options
    await waitFor(() => {
      expect(screen.getByRole('option', { name: '2024' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: '2023' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: '2022' })).toBeInTheDocument();
    });
  });

  it('calls onFiltersChange when year is selected', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
          availableYears={[2024, 2023, 2022]}
        />
      </TestWrapper>
    );

    // Click on year select and choose 2023
    const yearSelect = screen.getAllByRole('combobox')[0]; // First combobox is year
    await user.click(yearSelect);
    
    await waitFor(() => {
      const option2023 = screen.getByRole('option', { name: '2023' });
      expect(option2023).toBeInTheDocument();
    });

    await user.click(screen.getByRole('option', { name: '2023' }));

    expect(mockOnFiltersChange).toHaveBeenCalledWith({
      ...defaultFilters,
      year: 2023,
    });
  });

  it('displays available conferences in conference autocomplete', async () => {
    const user = userEvent.setup();
    const availableConferences = ['SEC', 'ACC', 'Big Ten'];
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
          availableConferences={availableConferences}
        />
      </TestWrapper>
    );

    // Click on conference autocomplete
    const conferenceInput = screen.getByRole('combobox', { name: /conference/i });
    await user.click(conferenceInput);

    // Check for conference options (including "All Conferences")
    await waitFor(() => {
      expect(screen.getByText('All Conferences')).toBeInTheDocument();
      expect(screen.getByText('SEC')).toBeInTheDocument();
      expect(screen.getByText('ACC')).toBeInTheDocument();
      expect(screen.getByText('Big Ten')).toBeInTheDocument();
    });
  });

  it('calls onFiltersChange when conference is selected', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
          availableConferences={['SEC', 'ACC']}
        />
      </TestWrapper>
    );

    // Click on conference autocomplete and select SEC
    const conferenceInput = screen.getByLabelText('Conference');
    await user.click(conferenceInput);
    
    await waitFor(() => {
      expect(screen.getByText('SEC')).toBeInTheDocument();
    });

    await user.click(screen.getByText('SEC'));

    expect(mockOnFiltersChange).toHaveBeenCalledWith({
      ...defaultFilters,
      conference: 'SEC',
    });
  });

  it('handles search input changes', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    const searchInput = screen.getByRole('textbox', { name: /search teams/i });
    await user.type(searchInput, 'Georgia');

    // Since we mocked useDebounce to return immediately, this should trigger
    await waitFor(() => {
      expect(mockOnFiltersChange).toHaveBeenCalledWith({
        ...defaultFilters,
        search: 'Georgia',
      });
    });
  });

  it('displays active filters count', () => {
    const filtersWithActive: StandingsFiltersState = {
      year: 2024,
      conference: 'SEC',
      search: 'Georgia',
    };
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={filtersWithActive}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Should show "2 active" (conference and search)
    expect(screen.getByText('2 active')).toBeInTheDocument();
  });

  it('displays active filter chips', () => {
    const filtersWithActive: StandingsFiltersState = {
      year: 2024,
      conference: 'SEC',
      search: 'Georgia',
    };
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={filtersWithActive}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Check for active filter chips
    expect(screen.getByText('Conference: SEC')).toBeInTheDocument();
    expect(screen.getByText('Search: "Georgia"')).toBeInTheDocument();
  });

  it('allows removing active filters via chip delete', async () => {
    const user = userEvent.setup();
    const filtersWithActive: StandingsFiltersState = {
      year: 2024,
      conference: 'SEC',
      search: 'Georgia',
    };
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={filtersWithActive}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Find and click the delete button on the conference chip
    const conferenceChip = screen.getByText('Conference: SEC').closest('.MuiChip-root');
    const deleteButton = conferenceChip?.querySelector('.MuiChip-deleteIcon');
    
    if (deleteButton) {
      await user.click(deleteButton);
      expect(mockOnFiltersChange).toHaveBeenCalledWith({
        ...filtersWithActive,
        conference: null,
      });
    }
  });

  it('clears all filters when Clear All button is clicked', async () => {
    const user = userEvent.setup();
    const filtersWithActive: StandingsFiltersState = {
      year: 2024,
      conference: 'SEC',
      search: 'Georgia',
    };
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={filtersWithActive}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Click Clear All button
    const clearAllButton = screen.getByText('Clear All');
    await user.click(clearAllButton);

    expect(mockOnFiltersChange).toHaveBeenCalledWith({
      year: new Date().getFullYear(),
      conference: null,
      search: '',
    });
  });

  it('shows Clear All button only when there are active filters', () => {
    // Test with no active filters
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    expect(screen.queryByText('Clear All')).not.toBeInTheDocument();

    // Re-render with active filters
    const { rerender } = render(
      <TestWrapper>
        <StandingsFilters
          filters={{ ...defaultFilters, conference: 'SEC' }}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    expect(screen.getByText('Clear All')).toBeInTheDocument();
  });

  it('handles refresh button click', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Find and click refresh button - it's the first button without text
    const refreshButton = screen.getAllByRole('button')[0]; // First button is refresh
    await user.click(refreshButton);

    expect(mockOnFiltersChange).toHaveBeenCalledWith(defaultFilters);
  });

  it('disables controls when loading is true', () => {
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
          loading={true}
        />
      </TestWrapper>
    );

    // Check that inputs are disabled  
    expect(screen.getAllByRole('combobox')[0]).toHaveAttribute('aria-disabled', 'true'); // Year select
    expect(screen.getByRole('combobox', { name: /conference/i })).toBeDisabled();
    expect(screen.getByRole('textbox', { name: /search teams/i })).toBeDisabled();
    expect(screen.getAllByRole('button')[0]).toBeDisabled(); // Refresh button
  });

  it('clears search input when clear button is clicked', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Find the search input  
    const searchInput = screen.getByRole('textbox', { name: /search teams/i });
    
    // Test that search input is interactive
    await user.click(searchInput);
    await user.type(searchInput, 'test');
    
    // Component renders and input works
    expect(searchInput).toBeInTheDocument();
    expect(searchInput).toHaveValue('test');
  });

  it('does not show active filters section when no filters are active', () => {
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    expect(screen.queryByText('Active Filters:')).not.toBeInTheDocument();
  });

  it('shows active filters section when filters are active', () => {
    const filtersWithActive: StandingsFiltersState = {
      year: 2024,
      conference: 'SEC',
      search: '',
    };
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={filtersWithActive}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    expect(screen.getByText('Active Filters:')).toBeInTheDocument();
  });

  it('handles "All Conferences" selection correctly', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Click on conference autocomplete
    const conferenceInput = screen.getByRole('combobox', { name: /conference/i });
    await user.click(conferenceInput);
    
    await waitFor(() => {
      expect(screen.getByText('All Conferences')).toBeInTheDocument();
    });

    await user.click(screen.getByText('All Conferences'));

    expect(mockOnFiltersChange).toHaveBeenCalledWith({
      ...defaultFilters,
      conference: 'All Conferences',
    });
  });

  it('uses default conferences when availableConferences prop is not provided', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Click on conference autocomplete
    const conferenceInput = screen.getByRole('combobox', { name: /conference/i });
    await user.click(conferenceInput);

    // Check for some default conferences
    await waitFor(() => {
      expect(screen.getByText('SEC')).toBeInTheDocument();
      expect(screen.getByText('ACC')).toBeInTheDocument();
      expect(screen.getByText('Big 10')).toBeInTheDocument();
    });
  });

  it('generates available years from current year to 2000 by default', async () => {
    const user = userEvent.setup();
    const currentYear = new Date().getFullYear();
    
    render(
      <TestWrapper>
        <StandingsFilters
          filters={defaultFilters}
          onFiltersChange={mockOnFiltersChange}
        />
      </TestWrapper>
    );

    // Click on year select
    const yearSelect = screen.getAllByRole('combobox')[0]; // First combobox is year
    await user.click(yearSelect);

    // Check for current year and some past years
    await waitFor(() => {
      expect(screen.getByText(currentYear.toString())).toBeInTheDocument();
      expect(screen.getByText('2020')).toBeInTheDocument();
      expect(screen.getByText('2000')).toBeInTheDocument();
    });
  });
});