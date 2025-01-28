import {Page, Locator, expect} from '@playwright/test';
import { getValue } from "../utilities/utils";

export class AdministrationPage {
    page: Page;
    adminMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        this.page = page;

        const adminElementClass = '.card.admin-menu';
        this.adminMenuItems = {
            configuration: page.locator(adminElementClass).getByText(getValue("admin", "configuration")),
            analyzers: page.locator(adminElementClass).getByText(getValue("admin", "analyzers")),
            vulnerabilitySources: page.locator(adminElementClass).getByText(getValue("admin", "vuln_sources")),
            repositories: page.locator(adminElementClass).getByText(getValue("admin", "repositories")),
            notifications: page.locator(adminElementClass).getByText(getValue("admin", "notifications")),
            integrations: page.locator(adminElementClass).getByText(getValue("admin", "integrations")),
            accessManagement: page.locator(adminElementClass).getByText(getValue("admin", "access_management")),
        };
    }

    async clickOnAdminMenu(menuName: string) {
        const menu = this.adminMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
        await this.page.waitForTimeout(2000);
    }
}

export class ConfigurationSubMenu {
    page: Page;
    configMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const configElementID = "#configuration";
        const configElementURN = "/admin/configuration";

        this.page = page;
        this.configMenuItems = {
            general: page.locator(configElementID).locator(`a[href='${configElementURN}/general']`),
            bomFormats: page.locator(configElementID).locator(`a[href='${configElementURN}/bomFormats']`),
            email: page.locator(configElementID).locator(`a[href='${configElementURN}/email']`),
            welcomeMessage: page.locator(configElementID).locator(`a[href='{configElementURN}/welcomeMessage']`),
            internalComponents: page.locator(configElementID).locator(`a[href='${configElementURN}/internalComponents']`),
            maintenance: page.locator(configElementID).locator(`a[href='${configElementURN}/maintenance']`),
            taskScheduler: page.locator(configElementID).locator(`a[href='${configElementURN}/taskScheduler']`),
            search: page.locator(configElementID).locator(`a[href='${configElementURN}/search']`),
            riskScore: page.locator(configElementID).locator(`a[href='${configElementURN}/RiskScore']`),
            experimental: page.locator(configElementID).locator(`a[href='${configElementURN}/experimental']`),
        };
    }

    async clickOnMenuItem(menuName: string) {
        const menu = this.configMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
        await this.page.waitForTimeout(2000);
    }
}

export class AnalyzersSubMenu {
    page: Page;
    analyzersMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const analyzersElementID = "#analyzers";
        const analyzersElementURN = "/admin/analyzers";

        this.page = page;
        this.analyzersMenuItems = {
            internal: page.locator(analyzersElementID).locator(`a[href='${analyzersElementURN}/internal']`),
            oss: page.locator(analyzersElementID).locator(`a[href='${analyzersElementURN}/oss']`),
            vulnDB: page.locator(analyzersElementID).locator(`a[href='${analyzersElementURN}/vulnDB']`),
            snyk: page.locator(analyzersElementID).locator(`a[href='${analyzersElementURN}/snyk']`),
            trivy: page.locator(analyzersElementID).locator(`a[href='${analyzersElementURN}/trivy']`),
        };
    }

    async clickOnMenuItem(menuName: string) {
        const menu = this.analyzersMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
        await this.page.waitForTimeout(2000);
    }
}

export class VulnerabilitySourcesSubMenu {
    page: Page;
    vulnerabilitySourcesMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const vulnerabilitySourcesElementID = "#vulnerabilitysources";
        const vulnerabilitySourcesElementURN = "/admin/vulnerabilitySources";

        this.page = page;
        this.vulnerabilitySourcesMenuItems = {
            nvd: page.locator(vulnerabilitySourcesElementID).locator(`a[href='${vulnerabilitySourcesElementURN}/nvd']`),
            github: page.locator(vulnerabilitySourcesElementID).locator(`a[href='${vulnerabilitySourcesElementURN}/github']`),
            osv: page.locator(vulnerabilitySourcesElementID).locator(`a[href='${vulnerabilitySourcesElementURN}/osv']`),
        };
    }

    async clickOnMenuItem(menuName: string) {
        const menu = this.vulnerabilitySourcesMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
        await this.page.waitForTimeout(2000);
    }
}

export class RepositoriesSubMenu {
    page: Page;
    repositoriesMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const repositoriesElementID = "#repositories";
        const repositoriesElementURN = "/admin/repositories";

        this.page = page;
        this.repositoriesMenuItems = {
            cargo: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/cargo']`),
            composer: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/composer']`),
            cpan: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/cpan']`),
            gem: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/gem']`),
            github: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/github']`),
            go: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/goModules']`),
            hackage: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/hackage']`),
            hex: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/hex']`),
            maven: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/maven']`),
            nix: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/nixpkgs']`),
            npm: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/npm']`),
            nuget: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/nuget']`),
            python: page.locator(repositoriesElementID).locator(`a[href='${repositoriesElementURN}/python']`),
        };
    }

    async clickOnMenuItem(menuName: string) {
        const menu = this.repositoriesMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
        await this.page.waitForTimeout(2000);
    }
}

export class NotificationsSubMenu {
    page: Page;
    notificationsMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const notificationsElementID = "#notifications";
        const notificationsElementURN = "/admin/notifications";

        this.page = page;
        this.notificationsMenuItems = {
            alerts: page.locator(notificationsElementID).locator(`a[href='${notificationsElementURN}/alerts']`),
            templates: page.locator(notificationsElementID).locator(`a[href='${notificationsElementURN}/templates']`),
        };
    }

    async clickOnMenuItem(menuName: string) {
        const menu = this.notificationsMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
        await this.page.waitForTimeout(2000);
    }
}

export class IntegrationsSubMenu {
    page: Page;
    integrationsMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const integrationsElementID = "#integrations";
        const integrationsElementURN = "/admin/integrations";

        this.page = page;
        this.integrationsMenuItems = {
            defectDojo: page.locator(integrationsElementID).locator(`a[href='${integrationsElementURN}/defectDojo']`),
            fortifySSC: page.locator(integrationsElementID).locator(`a[href='${integrationsElementURN}/fortifySSC']`),
            jira: page.locator(integrationsElementID).locator(`a[href='${integrationsElementURN}/jira']`),
            kennaSecurity: page.locator(integrationsElementID).locator(`a[href='${integrationsElementURN}/kennaSecurity']`),
        };
    }

    async clickOnMenuItem(menuName: string) {
        const menu = this.integrationsMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
        await this.page.waitForTimeout(2000);
    }
}

export class AccessManagementSubMenu {
    page: Page;
    accessManagementMenuItems: Record<string, Locator>;

    createTeamButton: Locator;
    createUserButton: Locator;
    deleteUserButton: Locator;
    modalContent: Locator;
    searchField: Locator;

    constructor(page: Page) {
        const accessManagementElementID = "#accessmanagement";
        const accessManagementElementURN = "/admin/accessManagement";

        this.page = page;
        this.modalContent = page.locator('.modal-content');
        this.searchField = page.locator('.search-input');

        this.accessManagementMenuItems = {
            ldapUsers: page.locator(accessManagementElementID).locator(`a[href='${accessManagementElementURN}/ldapUsers']`),
            managedUsers: page.locator(accessManagementElementID).locator(`a[href='${accessManagementElementURN}/managedUsers']`),
            oidcUsers: page.locator(accessManagementElementID).locator(`a[href='${accessManagementElementURN}/oidcUsers']`),
            oidcGroups: page.locator(accessManagementElementID).locator(`a[href='${accessManagementElementURN}/oidcGroups']`),
            teams: page.locator(accessManagementElementID).locator(`a[href='${accessManagementElementURN}/teams']`),
            permissions: page.locator(accessManagementElementID).locator(`a[href='${accessManagementElementURN}/permissions']`),
            portfolioAccessControl: page.locator(accessManagementElementID).locator(`a[href='${accessManagementElementURN}/portfolioAccessControl']`),
        };

        // TEAM
        this.createTeamButton = page.getByRole('button', { name: getValue("admin", "create_team") });

        // USER
        this.createUserButton = page.getByRole('button', { name: getValue("admin", "create_user") });
        this.deleteUserButton = page.getByRole('button', { name: getValue("admin", "delete_user") });
    }

    async fillSearchField(search: string) {
        await this.searchField.clear();
        await this.searchField.pressSequentially(search);
        await this.page.waitForTimeout(1000);
    }
    async clearSearchField() {
        await this.searchField.clear();
        await this.page.waitForTimeout(1000);
    }

    async clickOnMenuItem(menuName: string) {
        const menu = this.accessManagementMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
        await this.page.waitForTimeout(2000);
    }

    async createTeam(teamName: string) {
        await this.createTeamButton.click();

        await this.modalContent.locator('#name-input-input').fill(teamName);

        await this.modalContent.getByRole('button', { name: getValue("message", "create") }).click();
    }

    async createUser(username: string, password: string) {
        await this.createUserButton.click();

        await this.modalContent.locator('#username-input-input').fill(username);
        await this.modalContent.locator('#fullname-input-input').fill(username);
        await this.modalContent.locator('#email-input-input').fill(username);
        await this.modalContent.locator('#password-input-input').fill(password);
        await this.modalContent.locator('#confirmPassword-input-input').fill(password);

        await this.modalContent.getByRole('button', { name: getValue("message", "create") }).click();
    }

    async deleteUser(username: string) {
        await this.clickOnSpecificUser(username);
        await this.deleteUserButton.click();
    }
    
    async clickOnSpecificUser(username: string) {
        await this.page.getByRole('row', { name: username }).click();
        await this.page.waitForTimeout(1000);
    }

    async addPermissionsToSelectedUser(username: string, permissions: string | string[]) {
        const permissionsArray = Array.isArray(permissions) ? permissions : [permissions];

        await this.page.locator('button.btn.pull-right').last().click();

        const modalSearchField = this.modalContent.locator('.search-input');
        for(const permission of permissionsArray) {
            await modalSearchField.pressSequentially(permission);
            await this.page.waitForTimeout(2000);

            const tableRow = this.modalContent.locator(`tbody tr`).filter({ hasText: permission }).first();
            await expect(tableRow).toBeVisible();
            await tableRow.getByRole('checkbox').click();
            await modalSearchField.clear();
        }

        await this.modalContent.getByRole('button', { name: getValue("message", "select") }).click();
    }
}
