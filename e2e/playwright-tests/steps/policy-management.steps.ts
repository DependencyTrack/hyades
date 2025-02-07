import { Then } from '../fixtures/fixtures';
import { DataTable } from 'playwright-bdd';

Then('the user creates the following test policies', async ({ page, policyPage, notificationToast }, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await policyPage.clickOnCreatePolicy();
        await policyPage.createPolicy(row.policyName);
        await notificationToast.verifySuccessfulPolicyCreatedToast();
        await page.waitForTimeout(1000);
    }
});

Then('the user updates a policy with the following values', async ({ page, policyPage, notificationToast }, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await policyPage.fillSearchFieldInput(row.policyName);
        await policyPage.togglePolicyDetailView(row.policyName);

        await policyPage.updatePolicyName(row.newPolicyName);
        await policyPage.updatePolicyOperator(row.operator);
        await policyPage.updatePolicyViolationState(row.violationState);
        await notificationToast.verifySuccessfulUpdatedToast();
        // Playwright .selectOption inside updatePolicyOperator() and updatePolicyViolationState() doesn't get recognized by DTrack so the Toast is only displayed once

        await policyPage.togglePolicyDetailView(row.policyName);
        await page.waitForTimeout(1000);
    }
});

Then('the user adds conditions to {string} with the following values', async ({ page, policyPage, notificationToast }, policyName: string, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await policyPage.fillSearchFieldInput(policyName);
        await policyPage.togglePolicyDetailView(policyName);

        await policyPage.addConditionToPolicy(row.conditionSubject, row.conditionOperator, row.conditionInputValue);
        await notificationToast.verifySuccessfulUpdatedToast();
        await page.waitForTimeout(1000);
    }
    await policyPage.togglePolicyDetailView(policyName);
});

Then('the user deletes the following test policies if they exist', async ({ page, policyPage, notificationToast }, dataTable: DataTable) => {
    const count = await page.locator('tbody tr').count();
    if(count === 0) {
        return;
    }

    for(const row of dataTable.hashes()) {
        await policyPage.fillSearchFieldInput(row.policyName);

        const policyDoesntExist = await page.locator('.no-records-found').first().isVisible();
        if(policyDoesntExist) {
            console.warn(`Couldn't find project with name ${row.policyName}. Moving on.`);
            continue;
        }

        await policyPage.togglePolicyDetailView(row.policyName);
        await policyPage.deletePolicy();
        await notificationToast.verifySuccessfulPolicyDeletedToast();
        await page.waitForTimeout(1000);
    }
});