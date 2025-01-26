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
    }
}


export class ConfigurationMenu {
    page: Page;
    configMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const configElementID = "#configuration";
        const configElementURN = "/admin/configuration";

        this.page = page;
        this.configMenuItems = {
            general: page.locator(configElementID).locator(`${configElementURN}/general`),
            bomFormats: page.locator(configElementID).locator(`${configElementURN}/bomFormats`),
            email: page.locator(configElementID).locator(`${configElementURN}/email`),
            welcomeMessage: page.locator(configElementID).locator(`${configElementURN}/welcomeMessage`),
            internalComponents: page.locator(configElementID).locator(`${configElementURN}/internalComponents`),
            maintenance: page.locator(configElementID).locator(`${configElementURN}/maintenance`),
            taskScheduler: page.locator(configElementID).locator(`${configElementURN}/taskScheduler`),
            search: page.locator(configElementID).locator(`${configElementURN}/search`),
            riskScore: page.locator(configElementID).locator(`${configElementURN}/RiskScore`),
            experimental: page.locator(configElementID).locator(`${configElementURN}/experimental`),
        };
    }

    async clickMenu(menuName: string) {
        const menu = this.configMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
    }
}

export class AnalyzersMenu {
    page: Page;
    analyzersMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const analyzersElementID = "#analyzers";
        const analyzersElementURN = "/admin/analyzers";

        this.page = page;
        this.analyzersMenuItems = {
            internal: page.locator(analyzersElementID).locator(`${analyzersElementURN}/internal`),
            oss: page.locator(analyzersElementID).locator(`${analyzersElementURN}/oss`),
            vulnDB: page.locator(analyzersElementID).locator(`${analyzersElementURN}/vulnDB`),
            snyk: page.locator(analyzersElementID).locator(`${analyzersElementURN}/snyk`),
            trivy: page.locator(analyzersElementID).locator(`${analyzersElementURN}/trivy`),
        };
    }

    async clickMenu(menuName: string) {
        const menu = this.analyzersMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
    }
}

export class VulnerabilitySourcesMenu {
    page: Page;
    vulnerabilitySourcesMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const vulnerabilitySourcesElementID = "#vulnerabilitysources";
        const vulnerabilitySourcesElementURN = "/admin/vulnerabilitySources";

        this.page = page;
        this.vulnerabilitySourcesMenuItems = {
            nvd: page.locator(vulnerabilitySourcesElementID).locator(`${vulnerabilitySourcesElementURN}/nvd`),
            github: page.locator(vulnerabilitySourcesElementID).locator(`${vulnerabilitySourcesElementURN}/github`),
            osv: page.locator(vulnerabilitySourcesElementID).locator(`${vulnerabilitySourcesElementURN}/osv`),
        };
    }

    async clickMenu(menuName: string) {
        const menu = this.vulnerabilitySourcesMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
    }
}

export class RepositoriesMenu {
    page: Page;
    repositoriesMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const repositoriesElementID = "#repositories";
        const repositoriesElementURN = "/admin/repositories";

        this.page = page;
        this.repositoriesMenuItems = {
            cargo: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/cargo`),
            composer: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/composer`),
            cpan: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/cpan`),
            gem: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/gem`),
            github: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/github`),
            go: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/goModules`),
            hackage: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/hackage`),
            hex: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/hex`),
            maven: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/maven`),
            nix: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/nixpkgs`),
            npm: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/npm`),
            nuget: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/nuget`),
            python: page.locator(repositoriesElementID).locator(`${repositoriesElementURN}/python`),
        };
    }

    async clickMenu(menuName: string) {
        const menu = this.repositoriesMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
    }
}

export class NotificationsMenu {
    page: Page;
    notificationsMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const notificationsElementID = "#notifications";
        const notificationsElementURN = "/admin/notifications";

        this.page = page;
        this.notificationsMenuItems = {
            alerts: page.locator(notificationsElementID).locator(`${notificationsElementURN}/alerts`),
            templates: page.locator(notificationsElementID).locator(`${notificationsElementURN}/templates`),
        };
    }

    async clickMenu(menuName: string) {
        const menu = this.notificationsMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
    }
}

export class IntegrationsMenu {
    page: Page;
    integrationsMenuItems: Record<string, Locator>;

    constructor(page: Page) {
        const integrationsElementID = "#integrations";
        const integrationsElementURN = "/admin/integrations";

        this.page = page;
        this.integrationsMenuItems = {
            defectDojo: page.locator(integrationsElementID).locator(`${integrationsElementURN}/defectDojo`),
            fortifySSC: page.locator(integrationsElementID).locator(`${integrationsElementURN}/fortifySSC`),
            jira: page.locator(integrationsElementID).locator(`${integrationsElementURN}/jira`),
            kennaSecurity: page.locator(integrationsElementID).locator(`${integrationsElementURN}/kennaSecurity`),
        };
    }

    async clickMenu(menuName: string) {
        const menu = this.integrationsMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
    }
}

export class AccessManagementMenu {
    page: Page;
    accessManagementMenuItems: Record<string, Locator>;

    createTeamButton: Locator;
    createUserButton: Locator;
    modalContent: Locator;
    searchField: Locator;

    constructor(page: Page) {
        const accessManagementElementID = "#accessmanagement";
        const accessManagementElementURN = "/admin/accessManagement";

        this.page = page;
        this.modalContent = page.locator('.modal-content');
        this.searchField = page.locator('.search-input');

        this.accessManagementMenuItems = {
            ldapUsers: page.locator(accessManagementElementID).locator(`${accessManagementElementURN}/ldapUsers`),
            managedUsers: page.locator(accessManagementElementID).locator(`${accessManagementElementURN}/managedUsers`),
            oidcUsers: page.locator(accessManagementElementID).locator(`${accessManagementElementURN}/oidcUsers`),
            oidcGroups: page.locator(accessManagementElementID).locator(`${accessManagementElementURN}/oidcGroups`),
            teams: page.locator(accessManagementElementID).locator(`${accessManagementElementURN}/teams`),
            permissions: page.locator(accessManagementElementID).locator(`${accessManagementElementURN}/permissions`),
            portfolioAccessControl: page.locator(accessManagementElementID).locator(`${accessManagementElementURN}/portfolioAccessControl`),
        };

        // todo create team; edit team
        this.createTeamButton = page.getByRole('button', { name: getValue("admin", "create_team") });

        // todo create user; edit user
        this.createUserButton = page.getByRole('button', { name: getValue("admin", "create_user") });
    }

    async clickMenu(menuName: string) {
        const menu = this.accessManagementMenuItems[menuName];
        if (!menu) {
            throw new Error(`Menu '${menuName}' does not exist.`);
        }
        await menu.click();
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

    async addPermissionsToUser(username: string, permissions: string | string[]) {
        const permissionsArray = Array.isArray(permissions) ? permissions : [permissions];

        await this.searchField.fill(username);
        await this.page.locator(username).click();
        await this.page.locator('legend:has-text("Permissions")').locator('>> following-sibling::div//button').click();

        const searchField = this.modalContent.locator('.search-input');
        for(const permission of permissionsArray) {
            await searchField.fill(permission);
            await this.page.waitForTimeout(2000);

            const tableRow = this.modalContent.locator(`tbody tr:has(td:text-is(${permission}))`);
            await expect(tableRow).toBeVisible();
            await tableRow.getByRole('checkbox').click();
        }

        await this.modalContent.getByRole('button', { name: getValue("message", "select") }).click();
        await this.searchField.clear();
    }
}
