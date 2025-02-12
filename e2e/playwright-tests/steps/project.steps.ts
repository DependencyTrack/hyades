import { Then } from '../fixtures/fixtures';
import { DataTable } from 'playwright-bdd';
import {expect} from "@playwright/test";

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

Then('the user tries to create a project with name {string} and classifier {string}', async ({ page, projectPage, notificationToast }, projectName: string, classifier: string) => {
    await projectPage.clickOnCreateProject();
    await projectPage.createProject(
        projectName,
        classifier,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined
    );
    await page.waitForTimeout(1000);
});

Then('the user deletes the following test projects if they exist', async ({ page, projectPage, notificationToast }, dataTable: DataTable) => {
    await projectPage.showInactiveProjects();
    const count = await page.locator('tbody tr').count();
    if(count === 0) {
        return;
    }

    for(const row of dataTable.hashes()) {
        await projectPage.fillSearchFieldInput(row.name);

        const projectDoesntExist = await page.locator('.no-records-found').isVisible();
        if(projectDoesntExist) {
            console.warn(`Couldn't find project with name ${row.name}. Moving on.`);
            continue;
        }

        await projectPage.deleteProject(row.name);
        await notificationToast.verifySuccessfulProjectDeletedToast();
        await page.waitForTimeout(1000);
    }
    await projectPage.hideInactiveProjects();
});

Then('the user deletes the following test projects', async ({ page, projectPage, notificationToast }, dataTable: DataTable) => {
    for(const row of dataTable.hashes()) {
        await projectPage.fillSearchFieldInput(row.name);

        const projectDoesntExist = await page.locator('.no-records-found').isVisible();
        if(projectDoesntExist) {
            throw new Error(`Couldn't find project with name ${row.name}. This shouldn't happen.`);
        }

        await projectPage.deleteProject(row.name);
        await notificationToast.verifySuccessfulProjectDeletedToast();
        await page.waitForTimeout(1000);
    }
});

Then('the user opens the project with the name {string}', async ({ projectPage }, projectName: string) => {
    await projectPage.clickOnProject(projectName);
});

Then('the user navigates to project {string} tab and verifies', async ({ selectedProjectPage }, projectTab: string) => {
    await selectedProjectPage.clickOnTab(projectTab);
});

Then('the user opens project details', async ({ selectedProjectPage }) => {
    await selectedProjectPage.openProjectDetails();
});

const projectShouldNotBeVisible = Then('the project {string} should not be visible in the list', async ({ projectPage }, projectName: string) => {
    await expect(projectPage.projectList).not.toContainText(projectName);
});

Then('the user makes inactive projects visible in the list', async ({ projectPage }) => {
    await projectPage.showInactiveProjects();
});

const projectShouldBeVisible = Then('the project {string} should be visible in the list', async ({ projectPage }, projectName: string) => {
    await expect(projectPage.projectList).toContainText(projectName);
});

Then('the user sets the current project to inactive and verifies', async ({ selectedProjectPage, notificationToast }) => {
    await selectedProjectPage.makeProjectInactive();
    await selectedProjectPage.clickOnUpdateButton();
    await notificationToast.verifySuccessfulProjectUpdatedToast();
    const inactiveTag = (await selectedProjectPage.getProjectCard(0)).locator('.badge.badge-tab-warn');
    await expect(inactiveTag).toContainText(/INACTIVE/);
});

Then('the user uploads default BOM', async ({ projectComponentsPage, notificationToast }) => {
    await projectComponentsPage.uploadBom();
    await notificationToast.verifySuccessfulBomUploadedToast();
});

Then('the user verifies {string} with the badge number of {int} on current project', async ({ selectedProjectPage }, tabName: string, expectedTotalBadge: number) => {
    const actualTotalBadge = await selectedProjectPage.getTotalTabBadgeValue(tabName);
    expect(actualTotalBadge).toEqual(expectedTotalBadge);
});

Then('the user verifies Audit Vulnerabilities with the badge number of {int} excluding and {int} including aliases on current project',
  async ({ selectedProjectPage }, expectedTotalBadge: number, expectedInfoBadge: number) => {
    const tabName = "auditVulnerabilities";
    const actualExcludingAlias = await selectedProjectPage.getTotalTabBadgeValue(tabName);
    const actualIncludingAlias = await selectedProjectPage.getInfoTabBadgeValue(tabName);

    expect(actualExcludingAlias).toEqual(expectedTotalBadge);
    expect(actualIncludingAlias).toEqual(expectedInfoBadge);
});

Then('the user verifies Policy Violations with the badge number of {int} total {int} info {int} warn {int} fail violations on current project',
  async ({ selectedProjectPage }, totalBadge: number, infoBadge: number, warnBadge: number, failBadge: number) => {
    const tabName = "policyViolations";
    const actualTotalBadge = await selectedProjectPage.getTotalTabBadgeValue(tabName);
    const actualInfoBadge = await selectedProjectPage.getInfoTabBadgeValue(tabName);
    const actualWarnBadge = await selectedProjectPage.getWarnTabBadgeValue(tabName);
    const actualFailBadge = await selectedProjectPage.getFailTabBadgeValue(tabName);

    expect(actualTotalBadge).toBe(totalBadge);
    expect(actualInfoBadge).toBe(infoBadge);
    expect(actualWarnBadge).toBe(warnBadge);
    expect(actualFailBadge).toBe(failBadge);
});

Then('the table on the respective projects tab is visible and contains entries', async ({ page }) => {
    const tabPanelListLocator = page.locator('.tab-pane.active');

    await expect(tabPanelListLocator).toBeVisible();
    expect(await tabPanelListLocator.locator('tbody tr').count()).toBeGreaterThan(0);
});

Then('the dependency graph tab is visible and contains a node with child entries', async ({ projectDependencyGraphPage }) => {
    await projectDependencyGraphPage.toggleTreeNodeExpansion();

    await expect(projectDependencyGraphPage.tabPanel).toBeVisible();
    expect(await projectDependencyGraphPage.treeNodeChild.count()).toBeGreaterThan(0);

    await projectDependencyGraphPage.toggleTreeNodeExpansion();
});

Then('the user opens the policy violation of Component {string}', async ({ projectPolicyViolationsPage }, component: string) => {
    await projectPolicyViolationsPage.fillSearchFieldInput(component);
    await projectPolicyViolationsPage.clickOnSpecificViolation(component);
});

Then('the user comments the current policy violation with {string}', async ({ projectPolicyViolationsPage, notificationToast }, comment: string) => {
    await projectPolicyViolationsPage.fillDetailViewCommentField(comment);
    await projectPolicyViolationsPage.clickOnDetailViewAddCommentButton();
    await notificationToast.verifySuccessfulUpdatedToast();
});

Then('the user sets the analysis to {string}', async ({ projectPolicyViolationsPage, notificationToast }, option: string) => {
    await projectPolicyViolationsPage.setDetailViewAnalysisSelect(option);
    await notificationToast.verifySuccessfulUpdatedToast();
});

Then('the user suppresses the current policy violation', async ({ projectPolicyViolationsPage, notificationToast }) => {
    await projectPolicyViolationsPage.clickDetailViewSuppressToggle();
    await notificationToast.verifySuccessfulUpdatedToast();
});

Then('the audit trail should contain {string}', async ({ projectPolicyViolationsPage }, content: string) => {
    const regex = new RegExp(content, "i");
    await expect(projectPolicyViolationsPage.detailViewAuditTrailField).toHaveValue(regex);
});

Then('the policy violation of the component {string} will not appear in search result', async ({ projectPolicyViolationsPage }, component: string) => {
    await projectPolicyViolationsPage.fillSearchFieldInput(component);
    await projectPolicyViolationsPage.clearSearchFieldInput();
    await projectPolicyViolationsPage.fillSearchFieldInput(component);
    await expect(projectPolicyViolationsPage.tableList).toContainText(/No matching records found/);
});

Then('the project {string} should be a parent project and contain {string} as child project', async ({ projectPage }, parent: string, child: string) => {
    const parentRow = await projectPage.getProjectTableRow(parent);

    await expect(parentRow).toHaveClass(/treegrid-collapsed/);
    await projectShouldNotBeVisible({projectPage}, child);
    await expect(projectPage.projectList).not.toContainText(child);

    await parentRow.locator('.treegrid-expander').click();
    await projectShouldBeVisible({projectPage}, child);
});

Then('the upload-bom button is invisible', async ({ projectComponentsPage }) => {
    await expect(projectComponentsPage.uploadBomButton).not.toBeVisible();
});

Then('the upload-bom button should be visible', async ({ projectComponentsPage }) => {
    await expect(projectComponentsPage.uploadBomButton).toBeVisible();
});

Then('the project {string} tab should not be visible', async ({ selectedProjectPage }, projectTab: string) => {
    const tabLocator = await selectedProjectPage.getTabLocator(projectTab);
    await expect(tabLocator).not.toBeVisible();
});

Then('the project {string} tab should be visible', async ({ selectedProjectPage }, projectTab: string) => {
    const tabLocator = await selectedProjectPage.getTabLocator(projectTab)
    await expect(tabLocator).toBeVisible();
});

Then('the create-project button should not visible', async ({ projectPage }) => {
    await expect(projectPage.createProjectButton).not.toBeVisible();
});

Then('the create-project button should be visible', async ({ projectPage }) => {
    await expect(projectPage.createProjectButton).toBeVisible();
});

Then('the add-component button should not be visible', async ({ projectComponentsPage }) => {
    await expect(projectComponentsPage.addComponentButton).not.toBeVisible();
});

Then('the remove-component button should not be visible', async ({ projectComponentsPage }) => {
    await expect(projectComponentsPage.removeComponentButton).not.toBeVisible();
});

Then('the add-component button should be visible', async ({ projectComponentsPage }) => {
    await expect(projectComponentsPage.addComponentButton).toBeVisible();
});

Then('the remove-component button should be visible', async ({ projectComponentsPage }) => {
    await expect(projectComponentsPage.removeComponentButton).toBeVisible();
});

Then('the delete-project button in project details should not be visible', async ({ selectedProjectPage }) => {
    await expect(selectedProjectPage.projectDetailsDeleteButton).not.toBeVisible();
});

Then('the project-properties button in project details should not be visible', async ({ selectedProjectPage }) => {
    await expect(selectedProjectPage.projectDetailsPropertiesButton).not.toBeVisible();
});

Then('the add-version button in project details should not be visible', async ({ selectedProjectPage }) => {
    await expect(selectedProjectPage.projectDetailsAddVersionButton).not.toBeVisible();
});

Then('the update-project button in project details should not be visible', async ({ selectedProjectPage }) => {
    await expect(selectedProjectPage.projectDetailsUpdateButton).not.toBeVisible();
});

Then('the delete-project button in project details should be visible', async ({ selectedProjectPage }) => {
    await expect(selectedProjectPage.projectDetailsDeleteButton).toBeVisible();
});

Then('the project-properties button in project details should be visible', async ({ selectedProjectPage }) => {
    await expect(selectedProjectPage.projectDetailsPropertiesButton).toBeVisible();
});

Then('the add-version button in project details should be visible', async ({ selectedProjectPage }) => {
    await expect(selectedProjectPage.projectDetailsAddVersionButton).toBeVisible();
});

Then('the update-project button in project details should be visible', async ({ selectedProjectPage }) => {
    await expect(selectedProjectPage.projectDetailsUpdateButton).toBeVisible();
});

Then('the user expands the first vulnerability on audit Vulnerability project tab', async ({ projectAuditVulnerabilitiesPage }) => {
    await projectAuditVulnerabilitiesPage.tableList.first().locator('td').first().click();
});

Then('the user verifies read access on the vulnerability audit view on audit Vulnerability project tab', async ({ projectAuditVulnerabilitiesPage }) => {
    await expect(projectAuditVulnerabilitiesPage.detailViewTitleField).toBeVisible();
    await expect(projectAuditVulnerabilitiesPage.detailViewDescriptionField).toBeVisible();
    await expect(projectAuditVulnerabilitiesPage.detailViewAuditTrailField).toBeVisible();
});