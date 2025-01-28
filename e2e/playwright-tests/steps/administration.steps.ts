import { Then } from '../fixtures/fixtures';
import { DataTable } from 'playwright-bdd';

Then('the admin user navigates to administration {string}', async ({ administrationPage }, adminMenu: string) => {
    await administrationPage.clickOnAdminMenu(adminMenu);
});

Then('the admin user clicks on access-management {string}', async ({ accessManagementSubMenu }, adminSubMenu: string) => {
    await accessManagementSubMenu.clickOnMenuItem(adminSubMenu);
});

Then('the admin user creates the following test users', async ({ page, accessManagementSubMenu, notificationToast }, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await accessManagementSubMenu.createUser(row.username, process.env.RANDOM_PASSWORD);
        await notificationToast.verifySuccessfulUserCreatedToast();
        await page.waitForTimeout(2000);
    }
});

Then('the admin user provides {string} with the following permissions', async ({ page, notificationToast, accessManagementSubMenu }, username: string, dataTable: DataTable) => {
    /* todo maybe irgendwann gefixt von niklas
    const arrayOfStrings = [];

    for(const row of dataTable.hashes()) {
        arrayOfStrings.push(row.permission);
    }
    await accessManagementSubMenu.addPermissionsToUser(username, arrayOfStrings);
    await notificationToast.verifySuccessfulUpdatedToast();
    await page.waitForTimeout(2000);
    */

    for(const row of dataTable.hashes()) {
        await accessManagementSubMenu.fillSearchField(username);
        await accessManagementSubMenu.clickOnSpecificUser(username);
        await accessManagementSubMenu.addPermissionsToSelectedUser(username, row.permission);
        await notificationToast.verifySuccessfulUpdatedToast();
        await accessManagementSubMenu.clickOnSpecificUser(username);
        await page.waitForTimeout(2000);
    }
});

Then('the admin user deletes the following test users', async ({ page, accessManagementSubMenu, notificationToast }, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await accessManagementSubMenu.fillSearchField(row.username);
        await page.waitForTimeout(2000);

        const userDoesntExist = await page.locator('.no-records-found').isVisible();

        if(userDoesntExist) {
            continue;
        }

        await accessManagementSubMenu.deleteUser(row.username);
        await notificationToast.verifySuccessfulUserDeletedToast();
        await page.waitForTimeout(2000);
    }
});