import {Page, Locator, expect} from '@playwright/test';
import { getValue } from "../utilities/utils";

export class VulnerabilityModal {
    page: Page;
    modalContent: Locator;
    tabPanel: Locator;

    modalTab: Record<string, Locator>;

    vulnerabilityIdInput: Locator;
    titleInput: Locator;
    severitySelect: Locator;
    cvssSeveritySelect: Locator;
    owaspRiskRatingSeveritySelect: Locator;
    cweButton: Locator;
    descriptionField: Locator;

    closeButton: Locator;
    createButton: Locator;

    constructor(page: Page) {
        this.modalContent = page.locator('.modal-content');
        this.tabPanel = this.modalContent.locator('.tab-pane.active');

        this.modalTab = {
            generalTab: this.tabPanel.getByRole('tab', { name: getValue("message", "general") }),
            extendedTab: this.tabPanel.getByRole('tab', { name: getValue("message", "extended") }),
            cvssV2Tab: this.tabPanel.getByRole('tab', { name: getValue("message", "cvss_v2") }),
            cvssV3Tab: this.tabPanel.getByRole('tab', { name: getValue("message", "cvss_v3") }),
            owaspRiskRatingTab: this.tabPanel.getByRole('tab', { name: getValue("message", "owasp_rr") }),
            affectedComponentsTab: this.tabPanel.getByRole('tab', { name: getValue("message", "affected_components") }),
            datesTab: this.tabPanel.getByRole('tab', { name: getValue("message", "dates") })
        };

        this.vulnerabilityIdInput = this.tabPanel.locator('#vulnerability-id-input-input');
        this.titleInput = this.tabPanel.locator('#vulnerability-title-input-input');
        this.severitySelect = this.tabPanel.locator('#undefined-input').nth(0);
        this.cvssSeveritySelect = this.tabPanel.locator('#undefined-input').nth(1);
        this.owaspRiskRatingSeveritySelect = this.tabPanel.locator('#undefined-input').nth(2);
        this.cweButton = this.tabPanel.locator('.fa-plus-square');
        this.descriptionField = this.tabPanel.locator('#vulnerability-description-description');

        this.closeButton = this.tabPanel.getByRole('button', { name: getValue("message", "close") });
        this.createButton = this.tabPanel.getByRole('button', { name: getValue("message", "create") });
    }

    async clickOnTab(tabName: string) {
        const tab = this.modalTab[tabName];
        if (!tab) {
            throw new Error(`Tab '${tabName}' does not exist.`);
        }
        await tab.click();
        await this.page.waitForTimeout(1000);
        await expect(tab).toHaveClass(/active/);
    }

    async createVulnerability(vulnerabilityId: string, severity: string, cvssSeverity?: string, owaspRiskRating?: string, cwe?: string, decription?: string) {
        await expect(this.modalContent).toBeVisible();
        await this.vulnerabilityIdInput.fill(vulnerabilityId);
        await this.severitySelect.selectOption(severity);

        if(cvssSeverity) {
            await this.cvssSeveritySelect.selectOption(cvssSeverity);
        }
        if(owaspRiskRating) {
            await this.owaspRiskRatingSeveritySelect.selectOption(owaspRiskRating);
        }
        if(cwe) {
            await this.cweButton.click();
            const cweModal = this.page.locator('#selectCweModal___BV_modal_content_');
            await cweModal.locator('.search-input').pressSequentially(cwe);
            await this.page.waitForTimeout(1000);
            await cweModal.locator('tbody').locator('.bs-checkbox ').getByRole('checkbox').check();
            await cweModal.locator('.btn-primary').click();
        }
        if(decription) {
            await this.descriptionField.fill(decription);
        }
        await this.createButton.click();
    }
}

export class VulnerabilitiesPage extends VulnerabilityModal {
    page: Page;
    toolbar: Locator;
    createVulnerabilityButton: Locator;
    searchFieldInput: Locator;

    constructor(page: Page) {
        super(page);

        this.page = page;
        this.toolbar = page.locator('.fixed-table-toolbar');
        this.createVulnerabilityButton = this.toolbar.getByRole('button', { name: getValue("message", "create_vulnerability") });
    }

    async fillSearchFieldInput(search: string) {
        await this.searchFieldInput.clear();
        await this.searchFieldInput.pressSequentially(search);
        await this.page.waitForTimeout(1000);
    }

    async ClearSearchFieldInput() {
        await this.searchFieldInput.clear();
    }

    async clickOnCreateVulnerability() {
        await this.createVulnerabilityButton.click();
    }
}