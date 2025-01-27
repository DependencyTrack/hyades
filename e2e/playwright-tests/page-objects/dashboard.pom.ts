import {Page, Locator, expect} from '@playwright/test';
import { getValue } from "../utilities/utils";

export class DashboardPage {
    page: Page;

    constructor(page: Page) {
        this.page = page;

        const adminElementClass = '.card.admin-menu';
    }
}