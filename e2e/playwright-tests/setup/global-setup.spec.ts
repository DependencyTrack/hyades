import { request, type FullConfig } from '@playwright/test';
import { promises as fsPromises } from 'fs';
import * as fs from 'fs';
import * as path from "node:path";

// process.env.RANDOM_PASSWORD for creating safe passwords for users
// hier login mit password change etc.
// auch locale selection

// --> das aber denke ich ebenfalls mit BDD

async function globalSetup(config: FullConfig) {
    let locale: string;
    let password: string;

    if (process.env.CI) {
        locale = process.env.LOCALE;
        // process.env.RANDOM_PASSWORD set via uuidgen inside workflow
    } else {
        locale = 'en';
        process.env.RANDOM_PASSWORD = 'difficultPw123'
    }
    const filePath = await findMatchingLocaleFile(locale);

    const data = await fsPromises.readFile(filePath, 'utf8');
    process.env.LOCALE_JSON = JSON.stringify(data);

    /* HIER NUR PASSWORD CHANGE KEIN STORAGE STATE
    const { baseURL, storageState } = config.projects[0].use;
    const browser = await chromium.launch();
    const page = await browser.newPage();
    await page.goto(baseURL!);
    await page.getByLabel('User Name').fill('user');
    await page.getByLabel('Password').fill('password');
    await page.getByText('Sign in').click();
    await page.context().storageState({ path: storageState as string });
    await browser.close();
     */
}

async function findMatchingLocaleFile(locale: string, localeDir = './locales') {
    const localesPath = path.resolve(localeDir);
    let filePath: fs.PathOrFileDescriptor;

    // Ensure the locales directory exists
    if (!fs.existsSync(localesPath)) {
        console.error(`Locales directory not found at: ${localesPath}. Will try to download...`);

        const context = await request.newContext({
            baseURL: 'https://api.github.com',
            extraHTTPHeaders: {
                // We set this header per GitHub guidelines.
                'Accept': 'application/vnd.github.v3+json',
                // Add authorization token to all requests.
                // Assuming personal access token available in the environment.
                // 'Authorization': `token ${process.env.API_TOKEN}`,
            },
        });

        const apiResponse = await context.get("https://api.github.com/repos/DependencyTrack/frontend/contents/src/i18n/locales");

        if (apiResponse.ok()) {
            const data = await apiResponse.json();

            // Find the item that matches the locale
            const matchingItem = data.find(item => item.name.startsWith(`${locale}.json`));

            if (matchingItem) {
                const fileUrl = matchingItem.download_url;
                const fileResponse = await context.get(fileUrl);

                if (fileResponse.ok()) {
                    const fileContent = await fileResponse.body();

                    filePath = path.join(__dirname, localeDir, matchingItem.name);

                    fs.mkdirSync(path.dirname(filePath), { recursive: true });

                    fs.writeFileSync(filePath, fileContent);

                    console.log(`Downloaded: ${matchingItem.name}`);
                } else {
                    throw new Error(`Failed to download ${matchingItem.name}: ${fileResponse.status()}`);
                }
            } else {
                throw new Error(`Locale file for ${locale} not found.`);
            }
        } else {
            throw new Error(`Failed to fetch directory contents: ${apiResponse.status()} ${apiResponse.statusText()}`);
        }

        await context.dispose();
    } else {
        const localeFiles = fs.readdirSync(localesPath);

        const matchingFile = localeFiles.find((file) =>
            file.toLowerCase().includes(locale.toLowerCase())
        );

        if (!matchingFile) {
            throw new Error(`No matching locale file found for: ${locale}`);
        }

        filePath = path.join(localesPath, matchingFile);
    }
    return filePath;
}

export default globalSetup;