import { When } from '../fixtures/fixtures';

When('the dashboard should be visible', async ({ dashboardPage }) => {
    await dashboardPage.pageShouldBeVisible();
});