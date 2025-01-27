import { expect } from '@playwright/test';
import { Given, Then } from '../fixtures/fixtures';
import { DataTable } from 'playwright-bdd';

Given('the admin user logged in and sees the dashboard', async ({  }) => {

});

Then('the admin user navigates to administration {string}', async ({ administrationPage }, adminMenu: string) => {
    await administrationPage.clickOnAdminMenu(adminMenu);
});

Then('the admin user clicks on access-management {string}', async ({ accessManagementMenu }, adminSubMenu: string) => {
    await accessManagementMenu.clickMenu(adminSubMenu);
});

Then('the admin user creates the following test users', async ({ page, accessManagementMenu, notificationToast }, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await accessManagementMenu.createUser(row.username, process.env.RANDOM_PASSWORD);
        await notificationToast.verifySuccessfulUserCreatedToast();
        await page.waitForTimeout(2000);
    }
});

Then('the admin user provides {string} with the following permissions', async ({ page, notificationToast, accessManagementMenu }, username: string, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await accessManagementMenu.addPermissionsToUser(username, row.permission);
        await notificationToast.verifySuccessfulUpdatedToast();
        await page.waitForTimeout(2000);
    }
});