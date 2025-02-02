import { Page, Locator, expect } from '@playwright/test';
import { getValue } from "../utilities/utils";

export class NavigationParPage {
    page: Page;

    navBarToggle: Locator;
    sideBarMinimizer: Locator;
    snapshotPopup: Locator;

    navBarItems: Record<string, Locator>;

    breadCrumb: Locator;

    accountDropDown: Locator;
    accountDropDownUpdatePassword: Locator;
    accountDropDownChangePassword: Locator;
    accountDropDownChangeLanguage: Locator;
    accountDropDownLogout: Locator;

    updateProfilePopup: Locator;
    updateProfilePopupUsernameInput: Locator;
    updateProfilePopupEmailInput: Locator;
    updateProfilePopupCloseButton: Locator;
    updateProfilePopupUpdateButton: Locator;

    constructor(page: Page) {
        this.page = page;

        this.navBarItems = {
            dashboardTab: page.getByRole('link', { name: getValue("message", "dashboard") }),
            projectsTab: page.getByRole('link', { name: getValue("message", "projects") }),
            componentsTab: page.getByRole('link', { name: getValue("message", "components") }),
            vulnerabilitiesTab: page.getByRole('link', { name: getValue("message", "vulnerabilities") }),
            licencesTab: page.getByRole('link', { name: getValue("message", "licenses") }),
            tagsTab: page.getByRole('link', { name: getValue("message", "tags") }),
            vulnerabilityAuditTab: page.getByRole('link', { name: getValue("message", "vulnerability_audit") }),
            policyViolationAuditTab: page.getByRole('link', { name: getValue("message", "policy_violation_audit") }),
            policyManagementTab: page.getByRole('link', { name: getValue("message", "policy_management") }),
            administrationTab: page.getByRole('link', { name: getValue("message", "administration") }),
        };

        this.breadCrumb = page.locator('.breadcrumb');

        this.navBarToggle = page.locator('button.d-md-down-none.navbar-toggler');
        this.sideBarMinimizer = page.locator('button.sidebar-minimizer');
        this.snapshotPopup = page.locator('.modal-content');

        // Account Dropdown top right
        this.accountDropDown = page.locator('li.dropdown'); //todo if not working a.dropdown-toggle aber dann alle darunter anpassen
        this.accountDropDownUpdatePassword = this.accountDropDown.getByText(getValue("message", "logout"));
        this.accountDropDownChangePassword = this.accountDropDown.locator('/change-password');
        this.accountDropDownChangeLanguage = this.accountDropDown.locator('#locale-picker-form').locator('.custom-select');
        this.accountDropDownLogout = this.accountDropDown.getByText(getValue("message", "profile_update"));

        // Update Profile Popup
        this.updateProfilePopup = page.locator('.modal-content');
        this.updateProfilePopupUsernameInput = this.updateProfilePopup.locator('#fullname-input-input');
        this.updateProfilePopupEmailInput = this.updateProfilePopup.locator('#email-input-input');
        this.updateProfilePopupCloseButton = this.updateProfilePopup.getByText(getValue("message", "close"));
        this.updateProfilePopupUpdateButton = this.updateProfilePopup.getByText(getValue("message", "update"));
    }

    async logout() {
        await this.clickOnAccountDropDown();
        await this.clickOnAccountDropDownLogout();
    }

    async clickOnNavTab(tabName: string) {
        const tab = this.navBarItems[tabName];
        if (!tab) {
            throw new Error(`Menu '${tabName}' does not exist.`);
        }
        await tab.click();
        await this.page.waitForTimeout(1000);
        await expect(this.breadCrumb).toContainText(await tab.textContent());
    }

    async clickOnNavBarToggler() {
        await this.navBarToggle.click();
    }

    async clickOnSideBarMinimizer() {
        await this.sideBarMinimizer.click();
    }

    async closeSnapshotPopupIfVisible() {
        const isVisible = await this.snapshotPopup.isVisible(); // Todo ask niklas if this can be removed somehow

        if(isVisible) {
            await this.snapshotPopup.locator('button').click();
            await this.page.waitForTimeout(1000);
        }
    }

    async clickOnAccountDropDown() {
        await this.accountDropDown.click();
    }

    async clickOnAccountDropDownUpdatePassword() {
        await this.accountDropDownUpdatePassword.click();
    }

    async clickOnAccountDropDownChangePassword() {
        await this.accountDropDownChangePassword.click();
    }

    async clickOnAccountDropDownChangeLanguage() {
        await this.accountDropDownChangeLanguage.click();
    }

    async clickOnAccountDropDownLogout() {
        await this.accountDropDownLogout.click();
    }

    async fillUpdateProfilePopupUsernameInput(username: string) {
        await this.updateProfilePopupUsernameInput.fill(username);
    }

    async fillUpdateProfilePopupEmailInput(email: string) {
        await this.updateProfilePopupEmailInput.fill(email);
    }

    async clickUpdateProfilePopupCloseButton() {
        await this.updateProfilePopupCloseButton.click();
    }

    async clickUpdateProfilePopupUpdateButton() {
        await this.updateProfilePopupUpdateButton.click();
    }

    async expectUpdateProfilePopupToBeVisible() {
        await expect(this.updateProfilePopup).toBeVisible();
    }
}