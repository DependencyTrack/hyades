import { Given } from '../fixtures/fixtures';

Given('the dashboard should be visible', async ({ dashboardPage }) => {
    await dashboardPage.pageShouldBeVisible();
});