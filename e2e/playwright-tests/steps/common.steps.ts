import { Given } from '../fixtures/fixtures';

// todo add dashboardPage to verify aswell
// todo fix with sessionStorage
Given('the admin user navigates to dashboard', async ({ }) => {
    // Set session storage in a new context
    // https://playwright.dev/docs/auth#session-storage
});

Given('the admin user logs in to DependencyTrack', async ({ page, loginPage, navBarPage }) => {
    await page.goto('/');
    await loginPage.login('admin', process.env.RANDOM_PASSWORD);

    await navBarPage.closeSnapshotPopupIfVisible()
});


