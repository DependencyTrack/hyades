import { Given, Then } from '../fixtures/fixtures';

Given('the user navigates to administration page', async ({ navBarPage }) => {
    await navBarPage.clickOnNavTab("administrationTab");
});

Then('the user navigates to project page', async ({ navBarPage }) => {
    await navBarPage.clickOnNavTab("projectsTab");
});