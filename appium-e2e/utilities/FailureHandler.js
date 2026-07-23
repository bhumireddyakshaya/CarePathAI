const logger = require('./Logger');
const fs = require('fs');
const path = require('path');
const excelReporter = require('./ExcelReporter');

class FailureHandler {

    async captureFailure(testName, error, deviceName, osVersion) {
        logger.error(`Test Failed: ${testName} - Reason: ${error.message}`);
        
        const timestamp = new Date().getTime();
        const safeTestName = testName.replace(/[^a-zA-Z0-9]/g, '_');
        
        let screenshotPath = '';
        let activityName = 'Unknown';
        
        // Try getting current activity
        try {
            activityName = await browser.getCurrentActivity();
        } catch (e) {
            logger.warn('Could not fetch current activity');
        }

        // Try getting screenshot
        try {
            screenshotPath = path.join(process.cwd(), 'reports', 'failures', `screenshot_${safeTestName}_${timestamp}.png`);
            await browser.saveScreenshot(screenshotPath);
            logger.info(`Screenshot saved at: ${screenshotPath}`);
        } catch (e) {
            logger.error(`Failed to capture screenshot: ${e.message}`);
        }

        // Log to Excel
        excelReporter.addFailedTest({
            testName,
            reason: error.message,
            screenshot: screenshotPath,
            device: deviceName,
            os: osVersion,
            activity: activityName
        });
        
        // We log execution step as well
        excelReporter.addExecutionLog({
            timestamp: new Date().toISOString(),
            testName: testName,
            step: 'Execution Failed',
            result: 'FAIL',
            remarks: error.message
        });
    }
}

module.exports = new FailureHandler();
