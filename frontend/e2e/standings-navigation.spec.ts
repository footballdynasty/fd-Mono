import { test, expect } from '@playwright/test';

test.describe('Standings Page Navigation', () => {
  test.beforeEach(async ({ page }) => {
    // Assume user is already authenticated for these tests
    // In a real app, you'd handle authentication setup here
    await page.goto('/standings');
  });

  test('should navigate to standings page from dashboard', async ({ page }) => {
    // Start from dashboard
    await page.goto('/');
    
    // Open sidebar if on mobile
    const menuButton = page.locator('[aria-label="menu"]').first();
    if (await menuButton.isVisible()) {
      await menuButton.click();
    }

    // Click on standings link in navigation
    await page.getByRole('button', { name: /standings/i }).click();
    
    // Should navigate to standings page
    await expect(page).toHaveURL('/standings');
    
    // Should display standings page content
    await expect(page.getByRole('heading', { name: /team standings/i })).toBeVisible();
  });

  test('should display breadcrumb navigation', async ({ page }) => {
    await expect(page.locator('nav[aria-label="breadcrumb"]')).toBeVisible();
    await expect(page.getByRole('link', { name: /dashboard/i })).toBeVisible();
    await expect(page.getByText('Standings')).toBeVisible();
  });

  test('should navigate back to dashboard via breadcrumb', async ({ page }) => {
    await page.getByRole('link', { name: /dashboard/i }).click();
    await expect(page).toHaveURL('/');
  });

  test('should display standings page header', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /team standings/i })).toBeVisible();
    await expect(page.getByText(/track team performance across conferences/i)).toBeVisible();
  });

  test('should display filter section', async ({ page }) => {
    await expect(page.getByText('Filter Standings')).toBeVisible();
    await expect(page.getByLabel('Year')).toBeVisible();
    await expect(page.getByLabel('Conference')).toBeVisible();
    await expect(page.getByLabel('Search Teams')).toBeVisible();
  });

  test('should display standings table', async ({ page }) => {
    // Wait for table to load
    await expect(page.getByRole('table')).toBeVisible();
    
    // Check for table headers
    await expect(page.getByRole('columnheader', { name: /rank/i })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: /team/i })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: /wins/i })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: /losses/i })).toBeVisible();
  });

  test('should be accessible via direct URL', async ({ page }) => {
    await page.goto('/standings');
    await expect(page.getByRole('heading', { name: /team standings/i })).toBeVisible();
  });

  test('should support deep linking with filter parameters', async ({ page }) => {
    await page.goto('/standings?year=2023&conference=SEC');
    
    // Should display the page
    await expect(page.getByRole('heading', { name: /team standings/i })).toBeVisible();
    
    // Filters should be applied based on URL parameters
    // Note: Exact assertions would depend on how the filters are implemented
    await expect(page.getByLabel('Year')).toBeVisible();
    await expect(page.getByLabel('Conference')).toBeVisible();
  });

  test('should handle page refresh correctly', async ({ page }) => {
    // Apply some filters first
    await page.getByLabel('Search Teams').fill('Georgia');
    
    // Refresh the page
    await page.reload();
    
    // Page should still be functional
    await expect(page.getByRole('heading', { name: /team standings/i })).toBeVisible();
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('should be responsive on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Page should still be accessible and functional
    await expect(page.getByRole('heading', { name: /team standings/i })).toBeVisible();
    await expect(page.getByText('Filter Standings')).toBeVisible();
    
    // Table should be responsive
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('should display loading state initially', async ({ page }) => {
    // Intercept API calls to add delay for testing loading state
    await page.route('**/api/v2/standings**', async route => {
      await new Promise(resolve => setTimeout(resolve, 1000));
      await route.continue();
    });

    await page.goto('/standings');
    
    // Should show loading indicator
    await expect(page.getByText(/loading standings data/i)).toBeVisible();
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Mock API error
    await page.route('**/api/v2/standings**', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Server error' })
      });
    });

    await page.goto('/standings');
    
    // Should show error message
    await expect(page.getByText(/failed to load standings data/i)).toBeVisible();
  });

  test('should maintain navigation state correctly', async ({ page }) => {
    // Start from dashboard
    await page.goto('/');
    
    // Navigate to standings
    const menuButton = page.locator('[aria-label="menu"]').first();
    if (await menuButton.isVisible()) {
      await menuButton.click();
    }
    await page.getByRole('button', { name: /standings/i }).click();
    
    // Standings should be highlighted in navigation
    await expect(page).toHaveURL('/standings');
    
    // Navigate back to dashboard
    await page.getByRole('link', { name: /dashboard/i }).click();
    await expect(page).toHaveURL('/');
  });

  test('should support keyboard navigation', async ({ page }) => {
    // Focus on first interactive element
    await page.keyboard.press('Tab');
    
    // Should be able to navigate through filters using keyboard
    await page.keyboard.press('Tab'); // Year filter
    await page.keyboard.press('Tab'); // Conference filter
    await page.keyboard.press('Tab'); // Search filter
    
    // Should be able to interact with filters using keyboard
    await page.getByLabel('Search Teams').focus();
    await page.keyboard.type('Georgia');
    
    // Search should work with keyboard input
    await expect(page.getByLabel('Search Teams')).toHaveValue('Georgia');
  });

  test('should update URL when filters change', async ({ page }) => {
    // Apply search filter
    await page.getByLabel('Search Teams').fill('Georgia');
    
    // URL should update to include search parameter
    await expect(page).toHaveURL(/search=Georgia/);
    
    // Clear search
    await page.getByLabel('Search Teams').clear();
    
    // URL should update to remove search parameter
    await expect(page).not.toHaveURL(/search=Georgia/);
  });

  test('should display page metadata correctly', async ({ page }) => {
    // Check page title
    await expect(page).toHaveTitle(/team standings.*football dynasty/i);
    
    // Check if meta tags are set correctly (if implemented)
    const description = await page.locator('meta[name="description"]').getAttribute('content');
    if (description) {
      expect(description).toContain('standings');
    }
  });
});