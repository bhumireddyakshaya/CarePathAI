const logger = require('./Logger');
const fs = require('fs');
const path = require('path');

class AppiumUtils {
    
    /**
     * Explicitly waits for an element to be displayed.
     * @param {string} selector - WebDriverIO selector
     * @param {number} timeout - Wait timeout in ms (default: 10000)
     * @returns {Promise<WebdriverIO.Element>}
     */
    async waitForElementDisplayed(selector, timeout = 10000) {
        logger.info(`Waiting for element to be displayed: ${selector}`);
        const element = await $(selector);
        await element.waitForDisplayed({ timeout });
        return element;
    }

    /**
     * Explicitly waits for an element to be clickable.
     * @param {string} selector - WebDriverIO selector
     * @param {number} timeout - Wait timeout in ms (default: 10000)
     * @returns {Promise<WebdriverIO.Element>}
     */
    async waitForElementClickable(selector, timeout = 10000) {
        logger.info(`Waiting for element to be clickable: ${selector}`);
        const element = await $(selector);
        await element.waitForClickable({ timeout });
        return element;
    }

    /**
     * Checks if an element is currently visible on the screen.
     * @param {string} selector - WebDriverIO selector
     * @returns {Promise<boolean>}
     */
    async isElementVisible(selector) {
        try {
            const element = await $(selector);
            return await element.isDisplayed();
        } catch (error) {
            return false;
        }
    }

    /**
     * Safe keyboard hiding utility.
     */
    async hideKeyboard() {
        try {
            const isShown = await browser.isKeyboardShown();
            if (isShown) {
                logger.info('Hiding active keyboard');
                await browser.hideKeyboard();
            }
        } catch (error) {
            logger.warn(`Could not hide keyboard: ${error.message}`);
        }
    }

    /**
     * Alerts handling utility (accept, dismiss, or get text).
     * @param {string} action - 'accept' | 'dismiss' | 'get'
     */
    async handleAlert(action = 'accept') {
        try {
            const isAlertOpen = await browser.isAlertOpen();
            if (!isAlertOpen) {
                logger.warn('No alert dialog is currently open');
                return null;
            }

            if (action === 'accept') {
                logger.info('Accepting alert dialog');
                await browser.acceptAlert();
            } else if (action === 'dismiss') {
                logger.info('Dismissing alert dialog');
                await browser.dismissAlert();
            } else if (action === 'get') {
                const text = await browser.getAlertText();
                logger.info(`Fetched alert text: "${text}"`);
                return text;
            }
        } catch (error) {
            logger.error(`Alert handling failed: ${error.message}`);
        }
        return null;
    }

    /**
     * Captures a screenshot to the default screenshots folder.
     * @param {string} name - Screenshot description name
     */
    async captureScreenshot(name) {
        const timestamp = new Date().getTime();
        const screenshotDir = path.join(process.cwd(), 'screenshots');
        if (!fs.existsSync(screenshotDir)) {
            fs.mkdirSync(screenshotDir, { recursive: true });
        }
        const filepath = path.join(screenshotDir, `${name.replace(/\s+/g, '_')}_${timestamp}.png`);
        try {
            await browser.saveScreenshot(filepath);
            logger.info(`Screenshot captured successfully: ${filepath}`);
            return filepath;
        } catch (error) {
            logger.error(`Failed to capture screenshot: ${error.message}`);
            return null;
        }
    }

    /**
     * Captures Android Logcat logs.
     * @returns {Promise<Array>} logs array
     */
    async captureDeviceLogs() {
        try {
            logger.info('Capturing logcat logs');
            const logs = await browser.getLogs('logcat');
            return logs;
        } catch (error) {
            logger.warn(`Failed to capture device logs: ${error.message}`);
            return [];
        }
    }

    /**
     * Retries any action/function if it fails (useful for handling flaky animations or network lags).
     * @param {Function} action - Async function/arrow function to execute
     * @param {number} retries - Number of retries (default: 3)
     * @param {number} delay - Delay between retries in ms (default: 1000)
     */
    async retryAction(action, retries = 3, delay = 1000) {
        for (let i = 0; i < retries; i++) {
            try {
                return await action();
            } catch (error) {
                if (i === retries - 1) throw error;
                logger.warn(`Action failed. Retrying (${i + 1}/${retries}) in ${delay}ms... Error: ${error.message}`);
                await browser.pause(delay);
            }
        }
    }
}

module.exports = new AppiumUtils();
