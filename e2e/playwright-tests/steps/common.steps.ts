import { expect } from '@playwright/test';
import { Given, Then } from '../fixtures/fixtures';
import { DataTable } from 'playwright-bdd';

// todo add dashboardPage to verify aswell
Given('the admin user navigates to dashboard', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/dashboard/);
});

