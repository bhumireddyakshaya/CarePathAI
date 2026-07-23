const logger = require('../utilities/Logger');

class BasePage {

    async waitForElement(selector, timeout = 10000) {
        logger.info(`Waiting for element: ${selector}`);
        const element = await $(selector);
        await element.waitForDisplayed({ timeout });
        return element;
    }

    async click(selector) {
        const element = await this.waitForElement(selector);
        logger.info(`Clicking on element: ${selector}`);
        await element.click();
    }

    async setValue(selector, value) {
        const element = await this.waitForElement(selector);
        logger.info(`Setting value '${value}' on element: ${selector}`);
        await element.setValue(value);
    }

    async getText(selector) {
        const element = await this.waitForElement(selector);
        const text = await element.getText();
        logger.info(`Got text '${text}' from element: ${selector}`);
        return text;
    }

    async isDisplayed(selector, timeout = 5000) {
        try {
            const element = await $(selector);
            await element.waitForDisplayed({ timeout });
            return true;
        } catch (error) {
            return false;
        }
    }

    async hideKeyboard() {
        if (await browser.isKeyboardShown()) {
            logger.info('Hiding keyboard');
            await browser.hideKeyboard();
        }
    }
}

module.exports = BasePage;
