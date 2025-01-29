import { Then } from '../fixtures/fixtures';
import { DataTable } from 'playwright-bdd';

Then('the user creates projects with the following values', async ({ page, projectPage, notificationToast }, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await projectPage.clickOnCreateProject();
        await projectPage.createProject(
            row.name,
            row.classifier,
            row.version || undefined,
            row.isLastVersion ? row.isLastVersion === "true" : undefined,
            row.team || undefined,
            row.parent || undefined,
            row.description || undefined,
            row.tag || undefined
        );
        await notificationToast.verifySuccessfulProjectCreatedToast();
        await page.waitForTimeout(1000);
    }
});

Then('the user deletes the following test projects if they exist', async ({ page, projectPage, notificationToast }, dataTable: DataTable) => {
    const count = await page.locator('tbody tr').count();
    if(count === 0) {
        return;
    }

    for(const row of dataTable.hashes()) {
        await projectPage.fillSearchFieldInput(row.name);

        const userDoesntExist = await page.locator('.no-records-found').isVisible();
        if(userDoesntExist) {
            console.warn(`Couldn't find project with name ${row.name}. Moving on.`);
            continue;
        }

        await projectPage.deleteProject(row.name);
        await notificationToast.verifySuccessfulProjectDeletedToast();
        await page.waitForTimeout(1000);
    }
});
Then('the user opens the project with the name {string}', async ({ page, projectPage }, projectName: string) => {
    await projectPage.clickOnProject(projectName);
});
Then('the user navigates to project {string} tab', async ({ selectedProjectPage }, projectTab: string) => {
    await selectedProjectPage.clickOnTab(projectTab);
});
Then('the user uploads default BOM', async ({ projectComponentsPage, notificationToast }) => {
    await projectComponentsPage.uploadBom();
    await notificationToast.verifySuccessfulBomUploadedToast();
});