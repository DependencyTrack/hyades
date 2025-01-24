import { request } from '@playwright/test';
import { promises as fsPromises } from 'fs';
import * as fs from 'fs';
import * as path from "node:path";

// WILL NOT BE TRACKED IN REPORT
async function globalSetup() {
    let locale: string;

    if (process.env.CI) {
        locale = process.env.LOCALE;
        // process.env.RANDOM_PASSWORD is set via uuidgen inside workflow
    } else {
        locale = 'en';
        process.env.RANDOM_PASSWORD = 'difficultPw123'
    }
    const filePath = await findMatchingLocaleFile(locale);
    process.env.LOCALE_JSON = await fsPromises.readFile(filePath, 'utf8');
}

async function findMatchingLocaleFile(locale: string, localeDir = './locales') {
    let filePath: fs.PathOrFileDescriptor;
    console.info(`Will try to download correct Locale...`);

    const context = await request.newContext({
        baseURL: 'https://api.github.com',
        extraHTTPHeaders: {
            'Accept': 'application/vnd.github.v3+json',
        },
    });

    const apiResponse = await context.get("https://api.github.com/repos/DependencyTrack/frontend/contents/src/i18n/locales");

    if (apiResponse.ok()) {
        const data = await apiResponse.json();

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

    return filePath;
}

export default globalSetup;