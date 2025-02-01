import {Page, Locator, expect} from '@playwright/test';
import { getValue } from "../utilities/utils";

export class NotificationToast {
    page: Page;
    successToast: Locator;
    warnToast: Locator;
    errorToast: Locator;
    toastTitle: Locator;
    toastMessage: Locator;

    constructor(page: Page) {
        this.page = page;
        this.successToast = page.locator('div.toast-success');
        this.warnToast = page.locator('div.toast-warning');
        this.errorToast = page.locator('div.toast-error');

        this.toastTitle = page.locator('div.toast-title');
        this.toastMessage = page.locator('div.toast-message');
    }

    async verifySuccessfulPasswordChangeToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("message", "password_change_success"));
        await this.successToast.click();
    }

    async verifySuccessfulUserCreatedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("admin", "user_created"));
        await this.successToast.click();
    }

    async verifySuccessfulUserDeletedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("admin", "user_deleted"));
        await this.successToast.click();
    }

    async verifySuccessfulUpdatedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("message", "updated"));
        await this.successToast.click();
    }

    async verifySuccessfulProjectCreatedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("message", "project_created"));
        await this.successToast.click();
    }

    async verifySuccessfulProjectDeletedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("message", "project_deleted"));
        await this.successToast.click();
    }

    async verifySuccessfulBomUploadedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("message", "bom_uploaded"));
        await this.successToast.click();
    }

    async verifySuccessfulPolicyCreatedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("message", "policy_created"));
        await this.successToast.click();
    }

    async verifySuccessfulPolicyDeletedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("message", "policy_deleted"));
        await this.successToast.click();
    }

    async verifySuccessfulConditionDeletedToast() {
        await expect(this.successToast).toBeVisible();
        await expect(this.successToast).toContainText(getValue("message", "condition_deleted"));
        await this.successToast.click();
    }
}