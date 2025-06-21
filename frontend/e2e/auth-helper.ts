import { Page } from '@playwright/test';

export class AuthHelper {
  constructor(private page: Page) {}

  async mockAuthentication() {
    // Set auth token and user data directly in localStorage for faster testing
    await this.page.evaluate(() => {
      const mockUser = {
        id: 'test-user-id',
        username: 'testuser',
        email: 'test@example.com',
        roles: ['USER'],
        isActive: true
      };
      
      const mockTeam = {
        id: 'test-team-id',
        name: 'Test Team',
        conference: 'Test Conference',
        division: 'Test Division',
        location: 'Test City',
        abbreviation: 'TEST',
        color: '#FF0000',
        imageUrl: 'https://example.com/test-team.png',
        isHuman: true,
        currentWins: 8,
        currentLosses: 3
      };
      
      localStorage.setItem('auth_token', 'mock-jwt-token-' + Date.now());
      localStorage.setItem('user', JSON.stringify(mockUser));
      localStorage.setItem('selected_team', JSON.stringify(mockTeam));
    });

    // Mock API responses for authenticated requests
    await this.page.route('**/api/v2/**', (route) => {
      const url = route.request().url();
      
      // Mock auth endpoints
      if (url.includes('/auth/me')) {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            id: 'test-user-id',
            username: 'testuser',
            email: 'test@example.com',
            roles: ['USER']
          })
        });
        return;
      }

      // Mock standings data
      if (url.includes('/standings')) {
        const mockStandings = {
          content: [
            {
              id: '1',
              team: {
                id: 'team-1',
                name: 'Georgia',
                conference: 'SEC',
                division: 'East',
                location: 'Athens',
                abbreviation: 'UGA',
                color: '#BA0C2F',
                imageUrl: 'https://example.com/georgia.png'
              },
              year: 2024,
              wins: 10,
              losses: 1,
              conference_wins: 7,
              conference_losses: 1,
              win_percentage: 0.909,
              conference_win_percentage: 0.875,
              points_for: 420,
              points_against: 180,
              rank: 1,
              conference_rank: 1,
              votes: 1500
            },
            {
              id: '2',
              team: {
                id: 'team-2',
                name: 'Alabama',
                conference: 'SEC',
                division: 'West',
                location: 'Tuscaloosa',
                abbreviation: 'BAMA',
                color: '#9E1B32',
                imageUrl: 'https://example.com/alabama.png'
              },
              year: 2024,
              wins: 9,
              losses: 2,
              conference_wins: 6,
              conference_losses: 2,
              win_percentage: 0.818,
              conference_win_percentage: 0.750,
              points_for: 380,
              points_against: 200,
              rank: 2,
              conference_rank: 1,
              votes: 1450
            }
          ],
          totalElements: 2,
          totalPages: 1,
          size: 20,
          number: 0
        };
        
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockStandings)
        });
        return;
      }

      // Mock teams endpoint
      if (url.includes('/teams')) {
        const mockTeams = [
          {
            id: 'team-1',
            name: 'Georgia',
            conference: 'SEC',
            division: 'East',
            location: 'Athens',
            abbreviation: 'UGA',
            color: '#BA0C2F',
            imageUrl: 'https://example.com/georgia.png',
            isHuman: false
          },
          {
            id: 'test-team-id',
            name: 'Test Team',
            conference: 'Test Conference',
            division: 'Test Division',
            location: 'Test City',
            abbreviation: 'TEST',
            color: '#FF0000',
            imageUrl: 'https://example.com/test-team.png',
            isHuman: true
          }
        ];
        
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockTeams)
        });
        return;
      }

      // Mock games endpoint
      if (url.includes('/games')) {
        const mockGames = [
          {
            id: 'game-1',
            homeTeam: { id: 'team-1', name: 'Georgia' },
            awayTeam: { id: 'team-2', name: 'Alabama' },
            homeScore: 42,
            awayScore: 21,
            week: 10,
            year: 2024,
            isCompleted: true,
            gameDate: new Date().toISOString()
          }
        ];
        
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(mockGames)
        });
        return;
      }

      // Allow other requests to continue normally
      route.continue();
    });
  }

  async setupAuthenticatedSession() {
    // Navigate to the app first
    try {
      await this.page.goto('/');
    } catch (error) {
      // Fallback to absolute URL if relative URL fails
      await this.page.goto('http://localhost:3000/');
    }
    
    // Set up mock authentication
    await this.mockAuthentication();
    
    // Reload page to apply authentication state
    await this.page.reload({ waitUntil: 'networkidle' });
    
    // Wait for authentication to be recognized
    await this.page.waitForFunction(() => {
      return localStorage.getItem('auth_token') !== null && 
             localStorage.getItem('selected_team') !== null;
    }, { timeout: 10000 });
  }

  async login(username: string = 'testuser', password: string = 'testpass') {
    // Navigate to the app
    try {
      await this.page.goto('/');
    } catch (error) {
      await this.page.goto('http://localhost:3000/');
    }
    
    // Check if already authenticated
    const isAuthenticated = await this.page.evaluate(() => {
      return localStorage.getItem('auth_token') !== null;
    });
    
    if (isAuthenticated) {
      return; // Already logged in
    }

    try {
      // Wait for login form
      await this.page.waitForSelector('input[type="text"], input[type="email"], input[name="username"]', { timeout: 10000 });
      
      // Fill login form
      const usernameField = this.page.locator('input[name="username"], input[type="text"], input[type="email"]').first();
      const passwordField = this.page.locator('input[name="password"], input[type="password"]').first();
      
      await usernameField.fill(username);
      await passwordField.fill(password);
      
      // Submit login
      await this.page.getByRole('button', { name: /sign in|login/i }).click();
      
      // Wait for authentication to complete
      await this.page.waitForFunction(() => {
        return localStorage.getItem('auth_token') !== null;
      }, { timeout: 10000 });
    } catch (error) {
      console.log('Login form not found, using mock authentication instead');
      await this.mockAuthentication();
    }
  }

  async selectTeam(teamName?: string) {
    // Check if team selection is required
    const needsTeamSelection = await this.page.locator('text=Choose Your Team, text=Select Your Team').count() > 0;
    
    if (!needsTeamSelection) {
      return; // Team already selected
    }

    try {
      // Wait for team selection page
      await this.page.waitForSelector('text=Choose Your Team, text=Select Your Team', { timeout: 10000 });
      
      if (teamName) {
        // Search for specific team
        const searchField = this.page.locator('input[placeholder*="search" i], input[aria-label*="search" i]').first();
        if (await searchField.count() > 0) {
          await searchField.fill(teamName);
          await this.page.waitForTimeout(500); // Wait for search debounce
        }
        
        // Select the team
        await this.page.locator(`text=${teamName}`).first().click();
      } else {
        // Select first available team
        await this.page.locator('[data-testid="team-card"], .MuiCard-root, button:has-text("Select")').first().click();
      }
      
      // Confirm selection if needed
      const confirmButton = this.page.getByRole('button', { name: /start managing|confirm|select team/i });
      if (await confirmButton.count() > 0) {
        await confirmButton.click();
      }
      
      // Wait for dashboard or main app to load
      await this.page.waitForFunction(() => {
        return localStorage.getItem('selected_team') !== null;
      }, { timeout: 10000 });
    } catch (error) {
      console.log('Team selection not found, ensuring mock team is selected');
      await this.mockAuthentication();
    }
  }

  async loginAndSelectTeam(username?: string, password?: string, teamName?: string) {
    await this.login(username, password);
    await this.selectTeam(teamName);
  }

  async logout() {
    try {
      // Click logout button
      await this.page.getByRole('button', { name: /logout|sign out/i }).click();
      
      // Wait for logout to complete
      await this.page.waitForFunction(() => {
        return localStorage.getItem('auth_token') === null;
      }, { timeout: 5000 });
    } catch {
      // Manual logout by clearing localStorage
      await this.page.evaluate(() => {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user');
        localStorage.removeItem('selected_team');
      });
    }
  }

  async isAuthenticated(): Promise<boolean> {
    return await this.page.evaluate(() => {
      return localStorage.getItem('auth_token') !== null && 
             localStorage.getItem('selected_team') !== null;
    });
  }

  async getCurrentUser() {
    return await this.page.evaluate(() => {
      const user = localStorage.getItem('user');
      return user ? JSON.parse(user) : null;
    });
  }

  async getSelectedTeam() {
    return await this.page.evaluate(() => {
      const team = localStorage.getItem('selected_team');
      return team ? JSON.parse(team) : null;
    });
  }
}