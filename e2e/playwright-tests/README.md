# E2E Test Execution with Playwright
- [Playwright](#1-playwright-minimum-requirements)
    - [Installation](#12-installation)
- [Non-Cucumber Tests with Playwright](#2-Non-Cucumber-Tests-with-Playwright)
    - [Running a test](#21-running-a-test)
    - [Debugging](#22-Debugging)
- [Cucumber Tests with Playwright](#3-Cucumber-Tests-with-Playwright)
    - [Writing a Test with Gherkin Syntax](#31-writing-a-test-with-gherkin-syntax)
    - [Running a test](#32-Running-a-test)
- [Test Report](#4-test-report)
    - [Open report manually](#41-Open-report-manually)
    - [Open report via script](#42-Open-report-via-script)
***

# 1 Playwright minimum requirements
In order to install and execute playwright tests, check whether you meet the minimum requirements:
-  Node.js 16+
-  OS
    - Windows 10+, Windows Server 2016+ or Windows Subsystem for Linux (WSL).
    -  MacOS 12 Monterey, MacOS 13 Ventura, or MacOS 14 Sonoma.
    -  Debian 11, Debian 12, Ubuntu 20.04 or Ubuntu 22.04, with x86-64 or arm64 architecture.

⚠️ As the main  `.\package.json`  runs the `\playwright-tests\package.json` which uses at least Node.js 16, you can
**ONLY** run the following scripts using at least `npm version >= 7.x`
## 1.2 Installation
As of now, there is a script command inside the package.json within the root directory, that allows a simple installation of playwright and it's dependencies:

    npm run playwright:initialize

If no error occurred during setup, you can start using playwright :grin:

## 1.3 Local Proxy Configuration
When a https_proxy variable is set inside the terminal/CMD where you start playwright tests, https_proxy is automatically used for all tests against non-localhost URLs (UI and API-Tests)

# 2 Non-Cucumber Tests with Playwright
## 2.1 Running a test
For starters, there is a second script inside the same package.json as mentioned before, that will execute all `.spec.ts` files inside `./playwright-tests/e2e` using chromium.

    npm run playwright:run

In order to run a single file just add the relative path to that file (including `--` to inform node, that there are parameters):

    npm run playwright:run -- relative/path/to/the/test

## 2.2 Debugging
These parameters are the most common to be used. Just add them to the run-command (including `--`)
- `--headed` - If you want to run the tests visually (though it is very fast)
- `--debug` - If you want to use the integrated debugger (Does not need `--headed`)
- `--ui` - If you want to have a UI, similar to Cypress

Another way to debug, would be the Trace Viewer, which helps to get a better understanding of what happened

    npm run playwright:traceViewer -- "relative/path/to/trace/.zip"

Or open https://trace.playwright.dev/ and search for the file in the explorer

In order for it to work, both the application (frontend + backend) and keycloak need to be running.

# 3 Cucumber Tests with Playwright
As of March 2024 Playwright does not support Behavior-Driven-Development (BDD). ([See official issue](https://github.com/microsoft/playwright/issues/11975))
Luckily someone created a plugin for that making all the features of Playwright possible with Cucumber tests: https://vitalets.github.io/playwright-bdd/#/

## 3.1 Writing a Test with Gherkin Syntax
The _.feature_ files stay the same, but the _.step_ files need the following snippet added.

    import { expect } from '@playwright/test';
    import { createBdd } from 'playwright-bdd';

    const { Given, When, Then } = createBdd();

All the tests use fixtures instead of the page-object-model. Orientate on the implementation of already migrated tests. (You can also have a look at the fixture file inside cucumber)

## 3.2 Running a test
Using the plugin we can generate playwright tests out of the _.feature_ and _.step_ files. The only thing that needs to happen is, that the _.feature_ files and the _.step_ files are in their respective folder inside `\playwright-tests\cucumber`

To generate the test files manually you can use bddgen (you can exclude any tags you want):

    npx bddgen --tags "not @todo"

A script has been provided to execute inmemory cucumber tests from the root-dir inside the project

    npm run playwright:cucumber:inmemory:run

# 4 Test report
The current solution uses [Allure](https://allurereport.org/) to generate a test report out of the test results. In order to use it, just execute the following command after a run:

    npm run allure:report
## 4.1 Open report manually
Open the file at `./target/playwright-report/allure-report/index.html`

## 4.2 Open report via script
    npm run allure:open

If it doesn't work with your default browser, try it with a different one. Otherwise, open it manually

For more information on playwright visit the [official dev page](https://playwright.dev/)

