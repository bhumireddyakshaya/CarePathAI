const excelReporter = require('../utilities/ExcelReporter');
const failureHandler = require('../utilities/FailureHandler');
const logger = require('../utilities/Logger');

let executionStartTime;

before(async function () {
    executionStartTime = new Date();
    logger.info('--- Execution Started ---');
    
    // Auto-bypass Splash and Onboarding screens if they appear at app start
    try {
        logger.info('Bypassing Splash/Onboarding screens...');
        
        // Wait for Splash/Onboarding (up to 5 seconds)
        const skipBtnSelector = '//*[contains(@text, "Skip") or contains(@text, "Get Started")]';
        const skipBtn = await $(skipBtnSelector);
        
        // Wait up to 6 seconds for Splash animation and transition to Onboarding
        await skipBtn.waitForDisplayed({ timeout: 6000 });
        await skipBtn.click();
        logger.info('Bypassed Onboarding screen. Now on Login screen.');
    } catch (e) {
        logger.info('Splash/Onboarding skip button not found or already bypassed: ' + e.message);
    }
});


after(async function () {
    const executionEndTime = new Date();
    const durationMs = executionEndTime - executionStartTime;
    const durationSec = (durationMs / 1000).toFixed(2);
    
    // Add Summary record
    let passed = 0, failed = 0, skipped = 0;
    this.test.parent.suites.forEach(suite => {
        suite.tests.forEach(test => {
            if (test.state === 'passed') passed++;
            if (test.state === 'failed') failed++;
            if (test.isPending()) skipped++;
        });
    });
    const total = passed + failed + skipped;
    const passPercentage = total > 0 ? ((passed / total) * 100).toFixed(2) + '%' : '0%';
    
    let deviceName = 'Unknown';
    let osVersion = 'Unknown';
    try {
        const caps = await browser.getCapabilities();
        deviceName = caps.deviceModel || caps.deviceName || 'Emulator';
        osVersion = caps.platformVersion || 'Unknown';
    } catch(e) {}

    excelReporter.addSummary({
        date: new Date().toISOString().split('T')[0],
        device: deviceName,
        os: osVersion,
        total,
        passed,
        failed,
        skipped,
        percentage: passPercentage,
        duration: `${durationSec}s`
    });

    await excelReporter.saveReport();
    logger.info(`--- Execution Finished. Duration: ${durationSec}s ---`);
});

afterEach(async function () {
    const test = this.currentTest;
    const duration = test.duration ? (test.duration / 1000).toFixed(2) : 0;
    
    let deviceName = 'Unknown';
    let osVersion = 'Unknown';
    try {
        const caps = await browser.getCapabilities();
        deviceName = caps.deviceModel || caps.deviceName || 'Emulator';
        osVersion = caps.platformVersion || 'Unknown';
    } catch(e) {}

    // Add Test Case record
    excelReporter.addTestCase({
        id: `TC-${Math.floor(Math.random() * 10000)}`,
        module: test.parent.title,
        scenario: test.title,
        device: deviceName,
        status: test.state ? test.state.toUpperCase() : 'SKIPPED',
        start: new Date(Date.now() - (test.duration || 0)).toISOString(),
        end: new Date().toISOString(),
        duration: `${duration}s`
    });

    // Add Execution log
    excelReporter.addExecutionLog({
        timestamp: new Date().toISOString(),
        testName: test.title,
        step: 'Test Completed',
        result: test.state ? test.state.toUpperCase() : 'SKIPPED',
        remarks: test.state === 'passed' ? 'Success' : (test.err ? test.err.message : '')
    });

    if (test.state === 'failed') {
        await failureHandler.captureFailure(test.title, test.err, deviceName, osVersion);
    }
});
