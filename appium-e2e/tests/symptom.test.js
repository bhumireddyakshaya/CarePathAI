const { expect } = require('chai');
const logger = require('../utilities/Logger');
const loginPage = require('../pages/LoginPage');
const dashboardPage = require('../pages/DashboardPage');
const testData = require('../testdata/testData.json');

describe('CarePathAI Symptom Assessment E2E Flow', function () {

    before(async function () {
        logger.info('Preparing for Symptom Assessment Test');
        // Ensure we are logged in first
        const isLoginVisible = await loginPage.isLoginScreenVisible();
        if (isLoginVisible) {
            const { username, password } = testData.authentication.validUser;
            await loginPage.login(username, password);
        }
    });

    it('Should navigate to Symptom Assessment Screen', async function () {
        const onDashboard = await dashboardPage.isDashboardVisible();
        expect(onDashboard).to.be.true;

        logger.info('Clicking on Symptom Checker Card');
        const symptomCard = await $(dashboardPage.cardSymptomAnalysis);
        await symptomCard.waitForDisplayed({ timeout: 5000 });
        await symptomCard.click();

        // Check if step 1 progress or header is visible
        const progressIndicator = await $('//android.widget.ProgressBar');
        expect(await progressIndicator.isDisplayed()).to.be.true;
    });

    it('Should complete Step 1: Discomfort Area Selection', async function () {
        logger.info('Selecting discomfort area: Head');
        const headCard = await $('//*[contains(@text, "Head")]');
        await headCard.waitForDisplayed({ timeout: 5000 });
        await headCard.click();

        logger.info('Clicking Next button');
        const nextBtn = await $('//android.widget.Button[@text="Next"]');
        await nextBtn.click();
    });

    it('Should complete Step 2: Symptom Selection', async function () {
        logger.info('Selecting symptom: Headache');
        // In Compose, elements might be within lazy columns. Let's find Headache checkbox
        const headacheItem = await $('//*[contains(@text, "Headache")]');
        await headacheItem.waitForDisplayed({ timeout: 5000 });
        await headacheItem.click();

        logger.info('Clicking Next button');
        const nextBtn = await $('//android.widget.Button[@text="Next"]');
        await nextBtn.click();
    });

    it('Should complete Step 3: Additional Details & Trigger Analysis', async function () {
        logger.info('Entering additional details');
        
        // Wait for inputs
        const durationInput = await $('//android.widget.EditText[contains(@text, "How long") or @index=0]');
        await durationInput.waitForDisplayed({ timeout: 5000 });
        await durationInput.setValue('3 days');

        const notesInput = await $('//android.widget.EditText[contains(@text, "additional notes") or @index=1]');
        await notesInput.setValue('Feeling mild headache and warm body');

        await browser.hideKeyboard();

        logger.info('Triggering AI Analysis');
        const analyzeBtn = await $('//android.widget.Button[@text="Analyze Symptoms"]');
        await analyzeBtn.click();
    });

    it('Should show AI Analysis Results and return to Dashboard', async function () {
        logger.info('Waiting for AI Analysis results screen');
        const backToDashboardBtn = await $('//android.widget.Button[contains(@text, "Dashboard")]');
        await backToDashboardBtn.waitForDisplayed({ timeout: 10000 });

        const isResultsHeaderVisible = await $('//*[contains(@text, "AI Health Insights")]').isDisplayed();
        expect(isResultsHeaderVisible).to.be.true;

        logger.info('Returning to Dashboard');
        await backToDashboardBtn.click();

        const onDashboard = await dashboardPage.isDashboardVisible();
        expect(onDashboard).to.be.true;
    });
});
