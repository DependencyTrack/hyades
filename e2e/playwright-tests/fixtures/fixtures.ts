// Note: import base from playwright-bdd, not from @playwright/test!
import { test as base, createBdd } from 'playwright-bdd';
import {
    AdministrationPage,
    ConfigurationMenu,
    AnalyzersMenu,
    VulnerabilitySourcesMenu,
    RepositoriesMenu,
    NotificationsMenu,
    IntegrationsMenu,
    AccessManagementMenu
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


// export custom test fixtures
export const test = base.extend<
    {
        administrationPage: AdministrationPage;
        configurationMenu: ConfigurationMenu;
        analyzersMenu: AnalyzersMenu;
        vulnerabilitySourcesMenu: VulnerabilitySourcesMenu;
        repositoriesMenu: RepositoriesMenu;
        notificationsMenu: NotificationsMenu;
        integrationsMenu: IntegrationsMenu;
        accessManagementMenu: AccessManagementMenu;
        loginPage: LoginPage;
        navBarPage: NavigationParPage;
        notificationToast: NotificationToast;
    }>({
    administrationPage: async ({ page }, use) => {
        await use(new AdministrationPage(page));
    },
    configurationMenu: async ({ page }, use) => {
        await use(new ConfigurationMenu(page));
    },
    analyzersMenu: async ({ page }, use) => {
        await use(new AnalyzersMenu(page));
    },
    vulnerabilitySourcesMenu: async ({ page }, use) => {
        await use(new VulnerabilitySourcesMenu(page));
    },
    repositoriesMenu: async ({ page }, use) => {
        await use(new RepositoriesMenu(page));
    },
    notificationsMenu: async ({ page }, use) => {
        await use(new NotificationsMenu(page));
    },
    integrationsMenu: async ({ page }, use) => {
        await use(new IntegrationsMenu(page));
    },
    accessManagementMenu: async ({ page }, use) => {
        await use(new AccessManagementMenu(page));
    },
    loginPage: async ({ page }, use) => {
        await use(new LoginPage(page));
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
