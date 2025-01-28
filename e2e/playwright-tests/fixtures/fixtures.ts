// Note: import base from playwright-bdd, not from @playwright/test!
import { test as base, createBdd } from 'playwright-bdd';
import {
    AdministrationPage,
    ConfigurationSubMenu,
    AnalyzersSubMenu,
    VulnerabilitySourcesSubMenu,
    RepositoriesSubMenu,
    NotificationsSubMenu,
    IntegrationsSubMenu,
    AccessManagementSubMenu
} from "../page-objects/administration.pom";
import {
    LoginPage
} from "../page-objects/login.pom";
import {
    NavigationParPage
} from "../page-objects/navigation-bar.pom";
import {
    NotificationToast
} from "../page-objects/notification-toast.pom";
import {
    DashboardPage
} from "../page-objects/dashboard.pom";


// export custom test fixtures
export const test = base.extend<
    {
        administrationPage: AdministrationPage;
        configurationSubMenu: ConfigurationSubMenu;
        analyzersSubMenu: AnalyzersSubMenu;
        vulnerabilitySourcesSubMenu: VulnerabilitySourcesSubMenu;
        repositoriesSubMenu: RepositoriesSubMenu;
        notificationsSubMenu: NotificationsSubMenu;
        integrationsSubMenu: IntegrationsSubMenu;
        accessManagementSubMenu: AccessManagementSubMenu;
        loginPage: LoginPage;
        dashboardPage: DashboardPage;
        navBarPage: NavigationParPage;
        notificationToast: NotificationToast;
    }>({
    administrationPage: async ({ page }, use) => {
        await use(new AdministrationPage(page));
    },
    configurationSubMenu: async ({ page }, use) => {
        await use(new ConfigurationSubMenu(page));
    },
    analyzersSubMenu: async ({ page }, use) => {
        await use(new AnalyzersSubMenu(page));
    },
    vulnerabilitySourcesSubMenu: async ({ page }, use) => {
        await use(new VulnerabilitySourcesSubMenu(page));
    },
    repositoriesSubMenu: async ({ page }, use) => {
        await use(new RepositoriesSubMenu(page));
    },
    notificationsSubMenu: async ({ page }, use) => {
        await use(new NotificationsSubMenu(page));
    },
    integrationsSubMenu: async ({ page }, use) => {
        await use(new IntegrationsSubMenu(page));
    },
    accessManagementSubMenu: async ({ page }, use) => {
        await use(new AccessManagementSubMenu(page));
    },
    loginPage: async ({ page }, use) => {
        await use(new LoginPage(page));
    },
    dashboardPage: async ({ page }, use) => {
        await use(new DashboardPage(page));
    },
    navBarPage: async ({ page }, use) => {
        await use(new NavigationParPage(page));
    },
    notificationToast: async ({ page }, use) => {
        await use(new NotificationToast(page));
    }
});

// export changes
export const { Given, When, Then } = createBdd(test);
