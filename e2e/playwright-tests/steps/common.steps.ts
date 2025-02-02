import { Given } from '../fixtures/fixtures';

Given('the authenticated admin user navigates to dashboard', async ({ }) => {
    // Set session storage in a new context
    // add dashboardPage to verify aswell
    // https://playwright.dev/docs/auth#session-storage
});

Given('the admin user logs in to DependencyTrack', async ({ page, loginPage, navBarPage }) => {
    await page.goto('/');
    await loginPage.login('admin', process.env.RANDOM_PASSWORD);

    await navBarPage.closeSnapshotPopupIfVisible()
});
