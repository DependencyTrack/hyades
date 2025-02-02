import { test as setup } from '@playwright/test';
import { LoginPage } from "../page-objects/login.pom";
import * as fs from "node:fs";

const adminFile = 'e2e/playwright-tests/.auth/admin.json';

setup('Store Admin Authentication', async ({ page }) => {
    const loginPage = new LoginPage(page);

    await page.goto('/');
    await loginPage.login("admin", process.env.RANDOM_PASSWORD);

    await page.waitForURL('http://localhost:8081/dashboard', { timeout: 5000 });
    await page.context().storageState({ path: adminFile });
    // todo currently not working..fix with sessionStorage https://playwright.dev/docs/auth#session-storage
    //  add in common.steps when fixed and change playwright.config
});
