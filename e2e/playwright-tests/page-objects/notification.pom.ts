import { Page, Locator } from '@playwright/test';
import { getValue } from "../utilities/utils";

export class NotificationPage {
    page: Page;
    heading: Locator;
    username: Locator;
    password: Locator;
    submitButton: Locator;
    loginDescription: Locator;
    loginErrorPopup: Locator;

    constructor(page: Page) {
        this.page = page;
        this.heading = page.getByRole('heading', {name: getValue("message", "login")});
        this.username = page.getByPlaceholder(getValue("message", "username"));
        this.password = page.getByPlaceholder(getValue("message", "password"));
        this.submitButton = page.getByRole('button', {name: getValue("message", "login")});
        this.loginDescription = page.locator('.text-muted');
        this.loginErrorPopup = page.locator('.modal-content'); //language.json key -> login_unauthorized
    }

    async verifyUserCreatedAlert() {
        // user_created
        // close afterwards
    }
}