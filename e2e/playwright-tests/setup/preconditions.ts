import { test as setup } from '@playwright/test';
import {LoginPage, PasswordChangePage} from "../page-objects/login.pom";

setup('Initial Login', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await page.goto('/');
    await loginPage.login("admin", "admin");

    const initialPasswordChangePage = new PasswordChangePage(page);
    await initialPasswordChangePage.isPasswordChangePageVisible();
    await initialPasswordChangePage.doPasswordChangeFlow("admin", "admin", process.env.RANDOM_PASSWORD);

    await page.waitForTimeout(2000);
});

const adminFile = 'e2e/playwright-tests/.auth/admin.json';
setup('authenticate as admin', async ({ page }) => {
    const loginPage = new LoginPage(page);
    // const dashboardPage = new DashboardPage(page);

    await page.goto('/');

    await loginPage.isLoginPageVisible();
    await loginPage.login("admin", process.env.RANDOM_PASSWORD);

    await page.waitForURL('http://localhost:8081/dashboard');

    // await dashboardPage.isDashboardPageVisible();
    // await administrationPage
    // create users with groups

    await page.context().storageState({ path: adminFile });
});

/*
const users = [
    { username: 'test-user1', userFile: 'playwright-tests/.auth/user1.json', description: 'authenticate as user1' },
    { username: 'test-user2', userFile: 'playwright-tests/.auth/user2.json', description: 'authenticate as user2' },
    { username: 'test-user3', userFile: 'playwright-tests/.auth/user3.json', description: 'authenticate as user3' },
    { username: 'test-user4', userFile: 'playwright-tests/.auth/user4.json', description: 'authenticate as user4' },
    { username: 'test-user5', userFile: 'playwright-tests/.auth/user5.json', description: 'authenticate as user5' },
];

const authenticateUser = async (username: string, userFile: string, description: string) => {
    setup(description, async ({ page }) => {
        const loginPage = new LoginPage(page);
        await page.goto('/');

        await loginPage.isLoginPageVisible();
        await loginPage.login(username, process.env.RANDOM_PASSWORD);

        await page.waitForURL('http://localhost:8081/dashboard');

        // const dashboardPage = new DashboardPage(page);
        // await dashboardPage.isDashboardPageVisible();

        await page.context().storageState({ path: userFile });
    });
};

users.forEach(({ username, userFile, description }) => {
    authenticateUser(username, userFile, description);
});
*/