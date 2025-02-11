import { Then } from '../fixtures/fixtures';
import {expect} from "@playwright/test";

Then('the create-vulnerability button should not be visible', async ({ vulnerabilitiesPage }) => {
    await expect(vulnerabilitiesPage.createVulnerabilityButton).not.toBeVisible();
});

Then('the create-vulnerability button should be visible', async ({ vulnerabilitiesPage }) => {
    await expect(vulnerabilitiesPage.createVulnerabilityButton).toBeVisible();
});