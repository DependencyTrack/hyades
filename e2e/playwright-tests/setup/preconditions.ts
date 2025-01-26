import { test as setup } from '@playwright/test';
import { LoginPage, PasswordChangePage } from "../page-objects/login.pom";
import {AccessManagementMenu, AdministrationPage} from "../page-objects/administration.pom";
import {NavigationParPage} from "../page-objects/navigation-bar.pom";
import {NotificationPage} from "../page-objects/notification.pom";

setup('Initial Login', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await page.goto('/');
    await loginPage.login("admin", "admin");

    const initialPasswordChangePage = new PasswordChangePage(page);
    await initialPasswordChangePage.isPasswordChangePageVisible();
    await initialPasswordChangePage.doPasswordChangeFlow("admin", "admin", process.env.RANDOM_PASSWORD);

    await page.waitForTimeout(2000);
});


const users = [
    { username: 'test-user1', userFile: 'playwright-tests/.auth/user1.json', description: 'authenticate as user1', permissions: ["xyz", "xyz"], },
    { username: 'test-user2', userFile: 'playwright-tests/.auth/user2.json', description: 'authenticate as user2', permissions: ["xyz", "xyz"], },
    { username: 'test-user3', userFile: 'playwright-tests/.auth/user3.json', description: 'authenticate as user3', permissions: ["xyz", "xyz"], },
    { username: 'test-user4', userFile: 'playwright-tests/.auth/user4.json', description: 'authenticate as user4', permissions: ["xyz", "xyz"], },
    { username: 'test-user5', userFile: 'playwright-tests/.auth/user5.json', description: 'authenticate as user5', permissions: ["xyz", "xyz"], },
];
const adminFile = 'e2e/playwright-tests/.auth/admin.json';
setup('authenticate as admin', async ({ page }) => {
    const loginPage = new LoginPage(page);
    // const dashboardPage = new DashboardPage(page);
    const navBarPage = new NavigationParPage(page);
    const administrationPage = new AdministrationPage(page);
    const accessManagementMenu = new AccessManagementMenu(page);
    const notificationPage = new NotificationPage(page);

    await page.goto('/');

    await loginPage.isLoginPageVisible();
    await loginPage.login("admin", process.env.RANDOM_PASSWORD);

    await page.waitForURL('http://localhost:8081/dashboard');

    // await dashboardPage.isDashboardPageVisible();
    // await administrationPage
    // todo create users nachdem mit Niklas gesprochen

    await navBarPage.clickOnAdministrationTab();
    await administrationPage.clickOnAdminMenu('AccessManagement');

    for(const username of users) {
        await accessManagementMenu.createUser(username.username, process.env.RANDOM_PASSWORD);
        // todo await notificationPage.verifyUserCreatedAlert();
        await page.waitForTimeout(1000);
        // todo await accessManagementMenu.addPermissionsToUser(username.username, username.permissions);
        await page.waitForTimeout(1000);
    }

    await navBarPage.clickOnDashboardTab();

    await page.context().storageState({ path: adminFile });
});



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
    authenticateUser(username, userFile, description).then(() => {});
});
