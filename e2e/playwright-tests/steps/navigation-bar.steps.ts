import { Then } from '../fixtures/fixtures';

Then('the user navigates to {string} page', async ({ navBarPage }, navTab: string) => {
    await navBarPage.clickOnNavTab(navTab);
});