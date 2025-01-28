import { expect } from '@playwright/test';
import { Given, Then } from '../fixtures/fixtures';
import { DataTable } from 'playwright-bdd';

Given('the admin user navigates to administration page', async ({ navBarPage }) => {
    await navBarPage.clickOnAdministrationTab();
});