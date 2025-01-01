import { test, expect, type Page } from '@playwright/test';

// auch locale selection
// hier denke ich auch storage state mit neuen usern etc
// https://playwright.dev/docs/auth

// --> das aber denke ich ebenfalls mit BDD

// PRECONDITIONS SIND LOGIN
test('Should log password', async ({ page }) => {
    const pw = process.env.RANDOM_PASSWORD;
    // const json = JSON.parse(process.env.LOCALE_JSON);

    console.log(pw);
    // console.log(json);
});