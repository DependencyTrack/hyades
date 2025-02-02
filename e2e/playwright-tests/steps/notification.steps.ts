import { Then } from '../fixtures/fixtures';

Then('the user receives project creation error and warn toast', async ({ notificationToast }) => {
    await notificationToast.verifyFailedProjectCreatedToasts();
});
