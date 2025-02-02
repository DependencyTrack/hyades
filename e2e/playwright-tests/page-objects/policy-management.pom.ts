import {Page, Locator, expect} from '@playwright/test';
import { getValue } from "../utilities/utils";
import {NotificationToast} from "./notification-toast.pom";

export class PolicyPage {
    page: Page;
    tabPanel: Locator;
    createPolicyButton: Locator;
    searchFieldInput: Locator;

    modalContent: Locator;

    policyNameInput: Locator;
    policyCreationCreateButton: Locator;
    policyCreationClosePolicyButton: Locator;

    policyDetailView: Locator;

    policyDetailNameInput: Locator;
    policyDetailOperatorSelect: Locator;
    policyDetailViolationStateSelect: Locator;
    policyDetailConditionSubjectSelect: Locator;
    policyDetailConditionOperatorSelect: Locator;
    policyDetailConditionInput: Locator;

    policyDetailAddConditionButton: Locator;
    policyDetailDeleteConditionButton: Locator;

    policyDeletionButton: Locator;

    constructor(page: Page) {
        this.page = page;

        this.tabPanel = page.locator('.tab-pane.active');

        this.createPolicyButton = this.tabPanel.getByRole('button', { name: getValue("message", "create_policy") });
        this.searchFieldInput = this.tabPanel.locator('.search-input');

        this.modalContent = this.page.locator('.modal-content');

        // Create Policy
        this.policyNameInput = this.modalContent.locator('#identifier-input');
        this.policyCreationCreateButton = this.modalContent.getByRole('button', { name: getValue("message", "create") });
        this.policyCreationClosePolicyButton = this.modalContent.getByRole('button', { name: getValue("message", "close") });

        this.policyDetailView = this.tabPanel.locator('.detail-view');

        // Edit Policy
        this.policyDetailNameInput = this.policyDetailView.locator('#identifier-input');
        this.policyDetailOperatorSelect = this.policyDetailView.locator('#input-repository-type-input').first();// getByLabel(getValue("message", "operator"));
        this.policyDetailViolationStateSelect = this.policyDetailView.locator('#input-repository-type-input').last(); // .getByLabel(getValue("message", "violation_state"));
        this.policyDetailConditionSubjectSelect = this.policyDetailView.locator('#input-subject-input');
        this.policyDetailConditionOperatorSelect = this.policyDetailView.locator('#input-operator-input');
        this.policyDetailConditionInput = this.policyDetailView.locator('#input-value-input');

        this.policyDetailAddConditionButton = this.policyDetailView.locator('button.btn.pull-right').filter({ has: this.page.locator('.fa.fa-plus-square') }).first();
        this.policyDetailDeleteConditionButton = this.policyDetailView.locator('button.btn.pull-right').filter({ has: this.page.locator('.fa.fa-trash-o') }).first();

        // Delete Policy
        this.policyDeletionButton = this.policyDetailView.getByRole('button', { name: getValue("message", "delete_policy")})
    }

    async clickOnCreatePolicy() {
        await this.createPolicyButton.click();
    }

    async fillSearchFieldInput(search: string) {
        await this.searchFieldInput.clear();
        await this.searchFieldInput.pressSequentially(search);
        await this.page.waitForTimeout(1000);
    }

    async ClearSearchFieldInput() {
        await this.searchFieldInput.clear();
    }

    async createPolicy(policyName: string) {
        await expect(this.modalContent).toBeVisible();
        await expect(this.modalContent).toContainText(getValue("message", "create_policy"));

        await this.policyNameInput.pressSequentially(policyName);
        await this.policyCreationCreateButton.click();
    }

    async togglePolicyDetailView(policyName: string) {
        await this.fillSearchFieldInput(policyName);
        await this.tabPanel.getByRole('row', { name: policyName }).click();
    }

    async addConditionToPolicy(subject: string, operator: string, value: string) {
        await this.policyDetailAddConditionButton.click();
        await this.policyDetailConditionSubjectSelect.selectOption(subject);
        await this.policyDetailConditionOperatorSelect.selectOption(operator);
        await this.policyDetailConditionInput.pressSequentially(value);
    }

    async deleteAllConditions() {
        const notificationToast = new NotificationToast(this.page);

        while (await this.policyDetailDeleteConditionButton.count() > 0) {
            await this.policyDetailDeleteConditionButton.click();
            await notificationToast.verifySuccessfulConditionDeletedToast();
            await this.page.waitForTimeout(1000);
        }
    }

    async deletePolicy() {
        await expect(this.policyDetailView).toBeVisible();
        await this.policyDeletionButton.click();
    }

    async updatePolicyName(newPolicyName: string) {
        await this.policyDetailNameInput.clear();
        await this.policyDetailNameInput.pressSequentially(newPolicyName);
    }

    async updatePolicyOperator(operator: string) {
        await this.policyDetailOperatorSelect.selectOption(operator);
    }

    async updatePolicyViolationState(violationState: string) {
        await this.policyDetailViolationStateSelect.selectOption(violationState);
    }

}