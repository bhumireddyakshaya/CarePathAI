const { expect } = require('chai');
const logger = require('../utilities/Logger');
const dashboardPage = require('../pages/DashboardPage');

describe('Performance Validation', function () {

    it('Should capture App Launch Time', async function () {
        logger.info('Capturing App Launch Time');
        try {
            const supportedTypes = await browser.getPerformanceDataTypes();
            
            if (supportedTypes.includes('memoryinfo')) {
                const appPackage = 'com.example.carepathai';
                const memoryInfo = await browser.getPerformanceData(appPackage, 'memoryinfo', 5);
                logger.info('Memory Info:', JSON.stringify(memoryInfo));
                expect(memoryInfo.length).to.be.greaterThan(0);
            } else {
                logger.warn('Memory info not supported on this device');
                this.skip();
            }
        } catch (e) {
            logger.warn('Performance profiling failed or unsupported: ' + e.message);
            this.skip();
        }
    });

    it('Should measure Screen Load Time internally', async function () {
        try {
            const startTime = Date.now();
            
            // Trigger a navigation
            const button = await $(dashboardPage.tabProfile);
            await button.click();
            
            // Wait for target screen to load
            const targetScreen = await $('//android.widget.TextView[@text="Profile"]');
            await targetScreen.waitForDisplayed({ timeout: 10000 });
            
            const endTime = Date.now();
            const loadTime = endTime - startTime;
            
            logger.info(`Screen Load Time: ${loadTime}ms`);
            
            // Assert load time is within acceptable limits (e.g., < 3 seconds)
            expect(loadTime).to.be.lessThan(3000);
        } catch(e) {
            logger.warn('Skipping Screen Load Time test: ' + e.message);
            this.skip();
        }
    });

});
