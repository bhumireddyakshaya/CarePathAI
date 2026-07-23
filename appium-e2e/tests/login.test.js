const { expect } = require('chai');
const loginPage = require('../pages/LoginPage');
const dashboardPage = require('../pages/DashboardPage');
const logger = require('../utilities/Logger');
const testData = require('../testdata/testData.json');

describe('Authentication Testing', function () {

    before(async function () {
        logger.info('Ensuring test user is registered on the device/Firebase...');
        try {
            // Check if we are on Login screen. If we are already on Dashboard, log out.
            const onDashboard = await dashboardPage.isDashboardVisible();
            if (onDashboard) {
                await dashboardPage.logout();
            }

            // Go to SignUp screen to register the test user
            await loginPage.navigateToSignUp();
            
            const fullNameInput = await $('//android.widget.EditText[contains(@text, "Full Name")]');
            await fullNameInput.setValue('Test User');

            const emailInput = await $('//android.widget.EditText[contains(@text, "Email")]');
            await emailInput.setValue(testData.authentication.validUser.username);

            const mobileInput = await $('//android.widget.EditText[contains(@text, "Mobile Number")]');
            await mobileInput.setValue('1234567890');

            const passInput = await $('(//android.widget.EditText[contains(@text, "Password")])[1]');
            await passInput.setValue(testData.authentication.validUser.password);

            const confirmPassInput = await $('(//android.widget.EditText[contains(@text, "Password")])[2]');
            await confirmPassInput.setValue(testData.authentication.validUser.password);

            await browser.hideKeyboard();

            const signUpBtn = await $('//android.widget.Button[@text="Sign Up"]');
            await signUpBtn.click();
            
            logger.info('Sign Up request sent. Waiting to see if Dashboard or Login opens...');
            // Wait up to 5 seconds. If sign up succeeds, it logs in and opens Dashboard.
            // If it fails (e.g. email already exists), it might show Toast but stay on SignUp screen.
            const isDashboard = await dashboardPage.isDashboardVisible();
            if (isDashboard) {
                logger.info('Sign Up successful. Logging out to run login tests.');
                await dashboardPage.logout();
            } else {
                logger.info('Sign Up did not redirect to Dashboard (likely user already exists). Returning to Login.');
                const loginLink = await $('//android.widget.Button[@text="Login" or contains(@text, "Login")]');
                await loginLink.click();
            }
        } catch (e) {
            logger.warn('Pre-registration check skipped/failed: ' + e.message);
            // Attempt to recover by clicking back or going to Login screen
            try {
                const loginLink = await $('//android.widget.Button[@text="Login" or contains(@text, "Login")]');
                await loginLink.click();
            } catch(err) {}
        }
    });

    beforeEach(async function () {
        logger.info('Starting Authentication Scenario');
    });

    it('Should display validation error on empty credentials', async function () {
        const { username, password } = testData.authentication.emptyUser;
        await loginPage.login(username, password);
        
        // Compose blocks login client-side (button is disabled), so we assert Login button is not clickable
        const loginBtn = await $(loginPage.btnLogin);
        expect(await loginBtn.isEnabled()).to.be.false;
    });

    it('Should display error for invalid credentials', async function () {
        const { username, password } = testData.authentication.invalidUser;
        await loginPage.login(username, password);
        
        // Wait to see if error Toast or dialog appears
        try {
            const toast = await $('//android.widget.Toast');
            const toastText = await toast.getText();
            expect(toastText).to.not.be.empty;
        } catch (e) {
            logger.warn('Toast message not captured or missing: ' + e.message);
            // Fallback: make sure we did not log in
            const onDashboard = await dashboardPage.isDashboardVisible();
            expect(onDashboard).to.be.false;
        }
    });

    it('Should login successfully with valid credentials', async function () {
        const { username, password } = testData.authentication.validUser;
        await loginPage.login(username, password);
        const isDashboardVisible = await dashboardPage.isDashboardVisible();
        expect(isDashboardVisible).to.be.true;
    });

    it('Should persist session and logout successfully', async function () {
        const { username, password } = testData.authentication.validUser;
        let onDashboard = await dashboardPage.isDashboardVisible();
        if (!onDashboard) {
            await loginPage.login(username, password);
        }
        
        await dashboardPage.logout();
        const onLoginScreen = await loginPage.isLoginScreenVisible();
        expect(onLoginScreen).to.be.true;
    });

});
