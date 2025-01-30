import {Page, Locator, expect} from '@playwright/test';
import { getValue } from "../utilities/utils";

export class ProjectModal {
    modalContent: Locator;
    projectNameInput: Locator;
    projectVersionInput: Locator;
    projectIsLastVersionSlider: Locator;
    projectTeamSelect: Locator;
    projectClassifierSelect: Locator;
    projectParentSelect: Locator;
    projectDescriptionField: Locator;
    projectTag: Locator;
    projectActionButton: Locator;
    projectCloseButton: Locator;

    constructor(page: Page, actionButtonName: string) {
        this.modalContent = page.locator('.modal-content');
        this.projectNameInput = this.modalContent.locator('#project-name-input-input');
        this.projectVersionInput = this.modalContent.locator('#project-version-input-input');
        this.projectIsLastVersionSlider = this.modalContent.locator('#project-create-islatest-input');
        this.projectClassifierSelect = this.modalContent.locator('#v-classifier-input-input');
        this.projectTeamSelect = this.modalContent.locator('#v-team-input-input');
        this.projectParentSelect = this.modalContent.locator('##multiselect');
        this.projectDescriptionField = this.modalContent.locator('#project-description-description');
        this.projectTag = this.modalContent.locator('.ti-new-tag-input');

        this.projectActionButton = this.modalContent.getByRole('button', { name: getValue("message", actionButtonName) });
        this.projectCloseButton = this.modalContent.getByRole('button', { name: getValue("message", "close") });
    }
}

export class ProjectPage extends ProjectModal {
    page: Page;
    createProjectButton: Locator;
    inactiveProjectSlider: Locator;
    flatProjectSlider: Locator;
    searchFieldInput: Locator;

    constructor(page: Page) {
        super(page, "create");
        this.page = page;

        this.createProjectButton = page.getByRole('button', { name: getValue("message", "create_project")});
        this.inactiveProjectSlider = page.locator('#showInactiveProjects');
        this.flatProjectSlider = page.locator('#showFlatView');
        this.searchFieldInput = page.locator('.search-input');
    }

    async clickOnCreateProject() {
        await this.createProjectButton.click();
    }

    async createProject(projectName: string, projectClassifier: string, version?: string, isLastVersion?: boolean, team?: string, parent?: string, description?: string, tag?: string) {
        await this.projectNameInput.fill(projectName);
        await this.projectClassifierSelect.selectOption(projectClassifier);

        if(version) {
            await this.projectVersionInput.fill(version);
        }
        if(isLastVersion) {
            await this.projectIsLastVersionSlider.check();
        }
        if(team) {
            await this.projectTeamSelect.selectOption(team);
        }
        if(parent) {
            await this.projectParentSelect.selectOption(parent);
        }
        if(description) {
            await this.projectDescriptionField.fill(description);
        }
        if(tag) {
            await this.projectTag.fill(tag);
        }

        await this.projectActionButton.click();
    }

    async deleteProject(projectName: string) {
        const selectedProjectPage = new SelectedProjectPage(this.page);

        await this.clickOnProject(projectName);

        await selectedProjectPage.openProjectDetails();
        await selectedProjectPage.deleteProjectInProjectDetails();
    }

    async clickOnProject(projectName: string) {
        await this.page.getByRole('link', { name: projectName, exact: true }).first().click();
        await this.page.waitForTimeout(1000);
        await expect(this.page.locator('.container-fluid')).toBeVisible();
        await expect(this.page.locator('.text-nowrap.col-md-auto')).toContainText(projectName);
    }

    async fillSearchFieldInput(search: string) {
        await this.searchFieldInput.clear();
        await this.searchFieldInput.pressSequentially(search);
        await this.page.waitForTimeout(1000);
    }
    async clearSearchFieldInput() {
        await this.searchFieldInput.clear();
        await this.page.waitForTimeout(1000);
    }

    async checkInactiveProjects() {
        await this.inactiveProjectSlider.check();
    }
    async uncheckInactiveProjects() {
        await this.inactiveProjectSlider.uncheck();
    }

    async checkFlatProjectSlider() {
        await this.flatProjectSlider.check();
    }
    async uncheckFlatProjectSlider() {
        await this.flatProjectSlider.uncheck();
    }
}

export class SelectedProjectPage extends ProjectModal {
    page: Page;
    projectDetails: Locator;
    projectTabs: Locator;
    projectTabList: Record<string, Locator>;

    projectDetailsDeleteButton: Locator;
    projectDetailsPropertiesButton: Locator;
    projectDetailsAddVersionButton: Locator;

    constructor(page: Page) {
        super(page, "update");
        this.page = page;

        this.projectDetails = page.getByRole('link', { name: getValue("message", "view_details") });

        this.projectTabs = page.getByRole('tablist');
        this.projectTabList = {
            overview: this.projectTabs.getByText(getValue("message", "overview")),
            components: this.projectTabs.getByText(getValue("message", "components")),
            services: this.projectTabs.getByText(getValue("message", "services")),
            dependencyGraph: this.projectTabs.getByText(getValue("message", "dependency_graph")),
            auditVulnerabilities: this.projectTabs.getByText(getValue("message", "audit_vulnerabilities")),
            exploitPredictions: this.projectTabs.getByText(getValue("message", "exploit_predictions")),
            policyViolations: this.projectTabs.getByText(getValue("message", "policy_violations")),
        };

        this.projectDetailsDeleteButton = this.modalContent.getByRole('button', { name: getValue("message", "delete") });
        this.projectDetailsPropertiesButton = this.modalContent.getByRole('button', { name: getValue("message", "properties") });
        this.projectDetailsAddVersionButton = this.modalContent.getByRole('button', { name: getValue("message", "add_version") });
    }

    async clickOnTab(tabName: string) {
        const tab = this.projectTabList[tabName];
        if (!tab) {
            throw new Error(`Tab '${tabName}' does not exist.`);
        }
        await tab.click();
        await this.page.waitForTimeout(1000);
    }

    async getTotalTabBadgeValue(tabName: string) {
        const tab = this.projectTabList[tabName];
        if (!tab) {
            throw new Error(`Tab '${tabName}' does not exist.`);
        }
        const name = await tab.textContent();
        const regex = new RegExp(name.split("\n")[0].trim());

        const textContent = await this.projectTabs.getByRole('tab', { name: regex }).locator('span.badge.badge-tab-total').textContent();
        return parseInt(textContent, 10);
    }

    async getInfoTabBadgeValue(tabName: string) {
        const tab = this.projectTabList[tabName];
        if (!tab) {
            throw new Error(`Tab '${tabName}' does not exist.`);
        }
        const name = await tab.textContent();
        const regex = new RegExp(name.split("\n")[0].trim());

        const textContent = await this.projectTabs.getByRole('tab', { name: regex }).locator('span.badge.badge-tab-info').textContent();
        return parseInt(textContent, 10);
    }

    async getWarnTabBadgeValue(tabName: string) {
        const tab = this.projectTabList[tabName];
        if (!tab) {
            throw new Error(`Tab '${tabName}' does not exist.`);
        }
        const name = await tab.textContent();
        const regex = new RegExp(name.split("\n")[0].trim());

        const textContent = await this.projectTabs.getByRole('tab', { name: regex }).locator('span.badge.badge-tab-warn').textContent();
        return parseInt(textContent, 10);
    }

    async getFailTabBadgeValue(tabName: string) {
        const tab = this.projectTabList[tabName];
        if (!tab) {
            throw new Error(`Tab '${tabName}' does not exist.`);
        }
        const name = await tab.textContent();
        const regex = new RegExp(name.split("\n")[0].trim());

        const textContent = await this.projectTabs.getByRole('tab', { name: regex }).locator('span.badge.badge-tab-fail').textContent();
        return parseInt(textContent, 10);
    }

    async openProjectDetails() {
        await this.projectDetails.click();
        await expect(this.modalContent).toBeVisible();
        await expect(this.modalContent).toContainText(getValue("message", "project_details"));
    }

    async deleteProjectInProjectDetails() {
        await this.projectDetailsDeleteButton.click();

        await this.page.waitForTimeout(1000);

        await expect(this.modalContent).toBeVisible();
        await expect(this.modalContent).toContainText(getValue("message", "project_delete_title"));

        await this.modalContent.getByRole('button', { name: "OK" }).click();
    }

    async openPropertiesInProjectDetails() {
        await this.projectDetailsPropertiesButton.click();

        await this.page.waitForTimeout(1000);

        await expect(this.modalContent).toBeVisible();
        await expect(this.modalContent).toContainText(getValue("message", "project_properties"));
    }

    async addVersionInProjectDetails(version: string, isLatestVersion: boolean) {
        await this.projectDetailsAddVersionButton.click();

        await this.page.waitForTimeout(1000);

        await expect(this.modalContent).toBeVisible();
        await expect(this.modalContent).toContainText(getValue("message", "add_version"));

        await this.modalContent.locator('#input-1').fill(version);

        if(isLatestVersion) {
            await this.modalContent.locator('#project-details-islatest-input').check();
        }

        await this.modalContent.getByRole('button', { name: getValue("message", "create") }).click();
    }
}

export class ProjectComponentsPage {
    readonly page: Page;
    addComponentButton: Locator;
    removeComponentButton: Locator;
    uploadBomButton: Locator;
    downloadBomButton: Locator;
    downloadComponentsButton: Locator;

    modalContent: Locator;

    bomUploadBrowseButton: Locator;
    bomUploadCancelButton: Locator;
    bomUploadResetButton: Locator;
    bomUploadConfirmButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.addComponentButton = page.getByRole('button', { name: getValue("message", "add_component") });
        this.removeComponentButton = page.getByRole('button', { name: getValue("message", "remove_component") });
        this.uploadBomButton = page.locator('#upload-button');
        this.downloadBomButton = page.getByRole('button', { name: getValue("message", "download_bom") });
        this.downloadComponentsButton = page.getByRole('button', { name: getValue("message", "download_component") });


        this.modalContent = page.locator('.modal-content');

        // BOM Upload
        this.bomUploadBrowseButton = this.modalContent.locator('.custom-file-label');
        this.bomUploadCancelButton = this.modalContent.getByRole('button', { name: getValue("message", "cancel") });
        this.bomUploadResetButton = this.modalContent.getByRole('button', { name: getValue("message", "reset") });
        this.bomUploadConfirmButton = this.modalContent.getByRole('button', { name: getValue("message", "upload") });
    }

    async uploadBom(filePathFromProjectRoot?: string) {
        filePathFromProjectRoot ??= "e2e/playwright-tests/resources/dtrack-5.6.0-sbom.json";

        await this.uploadBomButton.click();
        await this.page.waitForTimeout(1000);

        await expect(this.modalContent).toBeVisible();
        await expect(this.modalContent).toContainText(getValue("message", "upload_bom"));

        const fileChooserPromise = this.page.waitForEvent('filechooser');
        await this.bomUploadBrowseButton.click();
        const fileChooser = await fileChooserPromise;

        await fileChooser.setFiles(filePathFromProjectRoot);

        await this.bomUploadConfirmButton.click();
    }
}
