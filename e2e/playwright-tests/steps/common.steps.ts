import { Given, Then } from '../fixtures/fixtures';

Given('the authenticated admin user navigates to dashboard', async ({ }) => {
    // Set session storage in a new context
    // add dashboardPage to verify aswell
    // https://playwright.dev/docs/auth#session-storage
});

Given('the admin user logs in to DependencyTrack', async ({ loginPage, navBarPage }) => {
    await loginPage.goto();
    await loginPage.verifyVisibleLoginPage();
    await loginPage.login('admin', process.env.RANDOM_PASSWORD);
    await navBarPage.verifyNavTabIsActive('dashboardTab');
});

Given('the user {string} tries to log in to DependencyTrack', async ({ loginPage, navBarPage }, username: string) => {
    await loginPage.goto();
    await loginPage.verifyVisibleLoginPage();
    await loginPage.login(username, process.env.RANDOM_PASSWORD);
});

Given('the user {string} logs in to DependencyTrack', async ({ loginPage, navBarPage }, username: string) => {
    await loginPage.goto();
    await loginPage.verifyVisibleLoginPage();
    await loginPage.login(username, process.env.RANDOM_PASSWORD);
    await navBarPage.verifyNavTabIsActive('dashboardTab');
});

Given('the user {string} tries to log in to DependencyTrack with password {string}', async ({ loginPage }, username: string, password: string) => {
    await loginPage.goto();
    await loginPage.verifyVisibleLoginPage();
    await loginPage.login(username, password);
});

Then('the user sees wrong log in credentials modal content popup', async ({ loginPage }) => {
    await loginPage.verifyLoginErrorPopup();
    await loginPage.closeLoginErrorPopup();

});