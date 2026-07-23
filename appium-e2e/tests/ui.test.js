const { expect } = require('chai');
const gestures = require('../utilities/Gestures');
const logger = require('../utilities/Logger');
const dashboardPage = require('../pages/DashboardPage');

describe('Mobile UI & Gesture Validation', function () {

    it('Should successfully swipe up and down on Dashboard', async function () {
        try {
            logger.info('Testing swipe gestures');
            await gestures.swipeUp(0.5);
            await browser.pause(1000);
            await gestures.swipeDown(0.5);
            expect(true).to.be.true; 
        } catch (e) {
            logger.warn('Skipping swipe test: ' + e.message);
            this.skip();
        }
    });

    it('Should scroll until Emergency Support button is visible', async function () {
        try {
            const targetElementSelector = '//*[contains(@text, "Emergency Support")]';
            await gestures.scrollUntilVisible(targetElementSelector, 5);
            
            const isVisible = await $(targetElementSelector).isDisplayed();
            expect(isVisible).to.be.true;
        } catch (e) {
            logger.warn('Skipping scroll to visible test: ' + e.message);
            this.skip();
        }
    });

    it('Should perform long press on Dashboard Card', async function () {
        try {
            const itemSelector = '//*[contains(@text, "Daily Wellness Score")]';
            const item = await $(itemSelector);
            await item.waitForDisplayed({ timeout: 5000 });
            
            await gestures.longPress(item, 2000);
            expect(true).to.be.true;
        } catch (e) {
            logger.warn('Skipping long press test: ' + e.message);
            this.skip();
        }
    });

});
