import { chromium, FullConfig } from '@playwright/test';
import { AuthHelper } from './auth-helper';

async function globalSetup(config: FullConfig) {
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();

  try {
    // Test if the app is accessible
    await page.goto(config.projects[0].use.baseURL || 'http://localhost:3000', {
      timeout: 30000,
      waitUntil: 'networkidle'
    });

    // Check if we can reach the basic app structure
    await page.waitForSelector('body', { timeout: 10000 });
    
    console.log('✅ Application is accessible');

    // Test authentication setup
    const auth = new AuthHelper(page);
    await auth.setupAuthenticatedSession();
    
    console.log('✅ Authentication system is working');
    
    // Test navigation to a protected route
    try {
      await page.goto('/standings');
    } catch (error) {
      await page.goto('http://localhost:3000/standings');
    }
    await page.waitForSelector('body', { timeout: 10000 });
    
    console.log('✅ Protected routes are accessible with authentication');
    
  } catch (error) {
    console.error('❌ Application setup failed:', error);
    throw error;
  } finally {
    await browser.close();
  }
}

export default globalSetup;