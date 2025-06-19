import { test, expect } from '@playwright/test';

test.describe('Standings User Workflow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/standings');
    // Wait for initial data to load
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('complete user workflow: view → filter → sort → view details', async ({ page }) => {
    // Step 1: View initial standings
    await expect(page.getByRole('heading', { name: /team standings/i })).toBeVisible();
    await expect(page.getByRole('table')).toBeVisible();
    
    // Verify initial data is loaded
    const initialRows = await page.getByRole('row').count();
    expect(initialRows).toBeGreaterThan(1); // Header + at least one data row

    // Step 2: Apply year filter
    await page.getByLabel('Year').click();
    await page.getByRole('option', { name: '2023' }).click();
    
    // Wait for data to update
    await page.waitForTimeout(500);
    
    // Step 3: Apply conference filter
    await page.getByLabel('Conference').click();
    await page.getByText('SEC').click();
    
    // Verify filter chip appears
    await expect(page.getByText('Conference: SEC')).toBeVisible();
    
    // Step 4: Apply search filter
    await page.getByLabel('Search Teams').fill('Georgia');
    
    // Verify search filter chip appears
    await expect(page.getByText('Search: "Georgia"')).toBeVisible();
    
    // Step 5: Sort by wins column
    await page.getByRole('columnheader', { name: /wins/i }).click();
    
    // Wait for sort to complete
    await page.waitForTimeout(300);
    
    // Step 6: Sort by wins again (reverse order)
    await page.getByRole('columnheader', { name: /wins/i }).click();
    await page.waitForTimeout(300);
    
    // Step 7: View details by hovering over a row (if hover effects are implemented)
    const firstDataRow = page.getByRole('row').nth(1);
    await firstDataRow.hover();
    
    // Verify the workflow completed without errors
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('filter by year and verify results update', async ({ page }) => {
    // Get initial data count
    const initialRowCount = await page.getByRole('row').count();
    
    // Change year filter
    await page.getByLabel('Year').click();
    await page.getByRole('option', { name: '2022' }).click();
    
    // Wait for data to update
    await page.waitForTimeout(1000);
    
    // Verify table still exists (data may or may not change depending on mock data)
    await expect(page.getByRole('table')).toBeVisible();
    
    // Verify URL updated with year parameter
    await expect(page).toHaveURL(/year=2022/);
  });

  test('filter by conference and verify conference-specific data', async ({ page }) => {
    // Apply conference filter
    await page.getByLabel('Conference').click();
    await page.getByText('Big Ten').click();
    
    // Verify filter is applied
    await expect(page.getByText('Conference: Big Ten')).toBeVisible();
    
    // Wait for data to update
    await page.waitForTimeout(1000);
    
    // Verify URL updated
    await expect(page).toHaveURL(/conference=Big%20Ten/);
    
    // Verify table still displays data
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('search for specific team and verify filtering', async ({ page }) => {
    // Enter search term
    await page.getByLabel('Search Teams').fill('Alabama');
    
    // Wait for debounced search
    await page.waitForTimeout(500);
    
    // Verify search filter chip appears
    await expect(page.getByText('Search: "Alabama"')).toBeVisible();
    
    // Clear search using the clear button
    await page.getByRole('button', { name: /clear/i }).first().click();
    
    // Verify search is cleared
    await expect(page.getByLabel('Search Teams')).toHaveValue('');
    await expect(page.getByText('Search: "Alabama"')).not.toBeVisible();
  });

  test('sort by different columns and verify sort indicators', async ({ page }) => {
    // Sort by rank
    await page.getByRole('columnheader', { name: /rank/i }).click();
    
    // Verify sort indicator (look for sort arrow or active state)
    const rankHeader = page.getByRole('columnheader', { name: /rank/i });
    await expect(rankHeader).toHaveClass(/Mui-active|active/);
    
    // Sort by win percentage
    await page.getByRole('columnheader', { name: /win %/i }).click();
    await page.waitForTimeout(300);
    
    // Sort by wins
    await page.getByRole('columnheader', { name: /wins/i }).click();
    await page.waitForTimeout(300);
    
    // Verify table remains functional
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('use multiple filters simultaneously', async ({ page }) => {
    // Apply year filter
    await page.getByLabel('Year').click();
    await page.getByRole('option', { name: '2023' }).click();
    
    // Apply conference filter
    await page.getByLabel('Conference').click();
    await page.getByText('ACC').click();
    
    // Apply search filter
    await page.getByLabel('Search Teams').fill('Duke');
    
    // Verify all filters are active
    await expect(page.getByText('Conference: ACC')).toBeVisible();
    await expect(page.getByText('Search: "Duke"')).toBeVisible();
    await expect(page.getByText('2 active')).toBeVisible();
    
    // Verify URL contains all parameters
    await expect(page).toHaveURL(/year=2023/);
    await expect(page).toHaveURL(/conference=ACC/);
    await expect(page).toHaveURL(/search=Duke/);
  });

  test('clear all filters and verify reset', async ({ page }) => {
    // Apply multiple filters
    await page.getByLabel('Conference').click();
    await page.getByText('SEC').click();
    await page.getByLabel('Search Teams').fill('Georgia');
    
    // Wait for filters to be applied
    await page.waitForTimeout(500);
    
    // Verify filters are active
    await expect(page.getByText('Clear All')).toBeVisible();
    
    // Clear all filters
    await page.getByText('Clear All').click();
    
    // Verify filters are cleared
    await expect(page.getByText('Clear All')).not.toBeVisible();
    await expect(page.getByText('Conference: SEC')).not.toBeVisible();
    await expect(page.getByText('Search: "Georgia"')).not.toBeVisible();
    
    // Verify URL is reset
    await expect(page).not.toHaveURL(/conference=/);
    await expect(page).not.toHaveURL(/search=/);
  });

  test('refresh data using refresh button', async ({ page }) => {
    // Apply a filter first
    await page.getByLabel('Search Teams').fill('Test');
    await page.waitForTimeout(500);
    
    // Click refresh button
    await page.getByRole('button', { name: /refresh/i }).click();
    
    // Verify page remains functional
    await expect(page.getByRole('table')).toBeVisible();
    await expect(page.getByLabel('Search Teams')).toHaveValue('Test');
  });

  test('handle empty results gracefully', async ({ page }) => {
    // Search for something that likely won't exist
    await page.getByLabel('Search Teams').fill('XYZ_NONEXISTENT_TEAM_123');
    await page.waitForTimeout(500);
    
    // Should show no results message
    await expect(page.getByText(/no standings data found/i)).toBeVisible();
    
    // Clear search to restore results
    await page.getByRole('button', { name: /clear/i }).first().click();
    
    // Table should show data again
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('responsive behavior during filtering and sorting', async ({ page }) => {
    // Test on mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Apply filters on mobile
    await page.getByLabel('Year').click();
    await page.getByRole('option', { name: '2023' }).click();
    
    await page.getByLabel('Search Teams').fill('State');
    await page.waitForTimeout(500);
    
    // Sort table on mobile
    await page.getByRole('columnheader', { name: /rank/i }).click();
    
    // Verify functionality works on mobile
    await expect(page.getByRole('table')).toBeVisible();
    
    // Test on tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    
    // Verify responsive layout
    await expect(page.getByRole('table')).toBeVisible();
    await expect(page.getByText('Filter Standings')).toBeVisible();
  });

  test('filter performance with large datasets', async ({ page }) => {
    // Mock a large dataset response
    await page.route('**/api/v2/standings**', route => {
      const mockData = {
        content: Array.from({ length: 100 }, (_, i) => ({
          id: `${i + 1}`,
          team: {
            id: `${i + 1}`,
            name: `Team ${i + 1}`,
            conference: ['SEC', 'ACC', 'Big Ten', 'Big 12'][i % 4],
            division: 'Division',
            location: `City ${i + 1}`,
            abbreviation: `T${i + 1}`,
            color: '#000000',
            imageUrl: `https://example.com/team${i + 1}.png`,
          },
          year: 2024,
          wins: Math.floor(Math.random() * 12),
          losses: Math.floor(Math.random() * 5),
          conference_wins: Math.floor(Math.random() * 8),
          conference_losses: Math.floor(Math.random() * 4),
          win_percentage: Math.random(),
          conference_win_percentage: Math.random(),
          points_for: Math.floor(Math.random() * 500),
          points_against: Math.floor(Math.random() * 400),
          rank: i < 25 ? i + 1 : null,
          conference_rank: (i % 10) + 1,
          votes: Math.floor(Math.random() * 1000),
        })),
        totalElements: 100,
        totalPages: 1,
      };
      
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockData),
      });
    });
    
    await page.reload();
    await expect(page.getByRole('table')).toBeVisible();
    
    // Apply filters to large dataset
    const startTime = Date.now();
    await page.getByLabel('Conference').click();
    await page.getByText('SEC').click();
    
    // Search should be responsive even with large dataset
    await page.getByLabel('Search Teams').fill('Team 1');
    await page.waitForTimeout(1000);
    
    const endTime = Date.now();
    const duration = endTime - startTime;
    
    // Filtering should complete within reasonable time (2 seconds)
    expect(duration).toBeLessThan(2000);
    
    // Verify table still works
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('keyboard navigation through workflow', async ({ page }) => {
    // Start with keyboard navigation
    await page.keyboard.press('Tab'); // Navigate to first focusable element
    
    // Navigate to year filter using keyboard
    await page.getByLabel('Year').focus();
    await page.keyboard.press('Enter');
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter');
    
    // Navigate to search field
    await page.getByLabel('Search Teams').focus();
    await page.keyboard.type('Georgia');
    
    // Navigate to table and sort using keyboard
    await page.getByRole('columnheader', { name: /wins/i }).focus();
    await page.keyboard.press('Enter');
    
    // Verify workflow completed successfully
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('maintain filter state across page navigation', async ({ page }) => {
    // Apply filters
    await page.getByLabel('Conference').click();
    await page.getByText('SEC').click();
    await page.getByLabel('Search Teams').fill('Alabama');
    
    // Navigate away to dashboard
    await page.getByRole('link', { name: /dashboard/i }).click();
    await expect(page).toHaveURL('/');
    
    // Navigate back to standings
    await page.goto('/standings');
    
    // Filters should be restored from URL
    await expect(page.getByText('Conference: SEC')).toBeVisible();
    await expect(page.getByLabel('Search Teams')).toHaveValue('Alabama');
  });
});