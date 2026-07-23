const logger = require('./Logger');

class Gestures {
    
    async swipeUp(percentage = 0.5) {
        const { width, height } = await browser.getWindowSize();
        const startX = width / 2;
        const startY = height * 0.8;
        const endY = height * (0.8 - percentage);

        logger.info(`Swiping up from ${startY} to ${endY}`);
        await this.swipe(startX, startY, startX, endY);
    }

    async swipeDown(percentage = 0.5) {
        const { width, height } = await browser.getWindowSize();
        const startX = width / 2;
        const startY = height * 0.2;
        const endY = height * (0.2 + percentage);

        logger.info(`Swiping down from ${startY} to ${endY}`);
        await this.swipe(startX, startY, startX, endY);
    }

    async swipeLeft(percentage = 0.5) {
        const { width, height } = await browser.getWindowSize();
        const startY = height / 2;
        const startX = width * 0.8;
        const endX = width * (0.8 - percentage);

        logger.info(`Swiping left from ${startX} to ${endX}`);
        await this.swipe(startX, startY, endX, startY);
    }

    async swipeRight(percentage = 0.5) {
        const { width, height } = await browser.getWindowSize();
        const startY = height / 2;
        const startX = width * 0.2;
        const endX = width * (0.2 + percentage);

        logger.info(`Swiping right from ${startX} to ${endX}`);
        await this.swipe(startX, startY, endX, startY);
    }

    async swipe(startX, startY, endX, endY, duration = 1000) {
        await browser.performActions([
            {
                type: 'pointer',
                id: 'finger1',
                parameters: { pointerType: 'touch' },
                actions: [
                    { type: 'pointerMove', duration: 0, x: startX, y: startY },
                    { type: 'pointerDown', button: 0 },
                    { type: 'pause', duration: 100 },
                    { type: 'pointerMove', duration: duration, x: endX, y: endY },
                    { type: 'pointerUp', button: 0 }
                ]
            }
        ]);
        await browser.releaseActions();
    }

    async scrollUntilVisible(elementSelector, maxSwipes = 5) {
        let swipes = 0;
        let isVisible = false;

        while (swipes < maxSwipes && !isVisible) {
            try {
                const el = await $(elementSelector);
                isVisible = await el.isDisplayed();
                if (isVisible) break;
            } catch (error) {
                // Element not in DOM yet
            }
            await this.swipeUp(0.3);
            swipes++;
        }
        
        if (!isVisible) {
            throw new Error(`Element ${elementSelector} not visible after ${maxSwipes} swipes`);
        }
    }

    async longPress(element, durationMs = 2000) {
        const location = await element.getLocation();
        const size = await element.getSize();
        const x = location.x + size.width / 2;
        const y = location.y + size.height / 2;
        
        logger.info(`Long pressing at (${x}, ${y}) for ${durationMs}ms`);

        await browser.performActions([{
            type: 'pointer',
            id: 'finger1',
            parameters: { pointerType: 'touch' },
            actions: [
                { type: 'pointerMove', duration: 0, x, y },
                { type: 'pointerDown', button: 0 },
                { type: 'pause', duration: durationMs },
                { type: 'pointerUp', button: 0 }
            ]
        }]);
        await browser.releaseActions();
    }
}

module.exports = new Gestures();
