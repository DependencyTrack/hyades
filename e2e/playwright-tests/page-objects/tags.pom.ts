import {Page, Locator } from '@playwright/test';

export class TagsPage {
    page: Page;
    tagsTableView: Locator;
    toolbar: Locator;
    tagsList: Locator;
    deleteButton: Locator;

    constructor(page: Page) {
        this.page = page;
        this.tagsTableView = page.locator('.container-fluid');
        this.toolbar = this.tagsTableView.locator('.fixed-table-toolbar');
        this.tagsList = this.tagsTableView.locator('tbody');
        this.deleteButton = this.tagsTableView.locator('.fa-trash');
    }

}