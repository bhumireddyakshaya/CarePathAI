const { execSync } = require('child_process');
const logger = require('../utilities/Logger');

class DriverFactory {
    
    /**
     * Dynamically detects connected devices using adb and sets up capabilities for testing.
     * WebdriverIO usually handles initialization, but this factory acts as a configuration builder 
     * to ensure tests run dynamically on available devices or emulators.
     */
    static getCapabilities() {
        const caps = {
            platformName: 'Android',
            'appium:automationName': 'UiAutomator2',
            'appium:autoGrantPermissions': true,
            'appium:noReset': true,
            'appium:fullReset': false,
            'appium:newCommandTimeout': 240,
            'appium:uiautomator2ServerInstallTimeout': 60000,
        };

        if (process.env.APK_PATH) {
            caps['appium:app'] = process.env.APK_PATH;
            logger.info(`Configuring capabilities for APK execution: ${process.env.APK_PATH}`);
        } else {
            caps['appium:appPackage'] = process.env.APP_PACKAGE || 'com.carepathai.app';
            caps['appium:appActivity'] = process.env.APP_ACTIVITY || 'com.carepathai.app.MainActivity';
            logger.info(`Configuring capabilities for installed app: ${caps['appium:appPackage']}`);
        }

        const device = this.detectDevice();
        if (device) {
            caps['appium:udid'] = device;
            logger.info(`Dynamically configuring test to run on device: ${device}`);
        } else {
            logger.warn('No devices detected dynamically. Will rely on default emulator settings if running locally.');
        }

        return caps;
    }

    /**
     * Executes adb devices to find the first available connected device or emulator.
     */
    static detectDevice() {
        const fs = require('fs');
        const path = require('path');
        let adbPath = 'adb';
        
        // standard android sdk location
        const standardSdkPath = path.join('C:', 'Users', 'aksha', 'AppData', 'Local', 'Android', 'Sdk', 'platform-tools', 'adb.exe');
        if (fs.existsSync(standardSdkPath)) {
            adbPath = `"${standardSdkPath}"`;
        }

        try {
            const output = execSync(`${adbPath} devices`).toString();
            const lines = output.split('\n');
            for (let i = 1; i < lines.length; i++) {
                const line = lines[i].trim();
                if (line && line.endsWith('device')) {
                    const udid = line.split('\t')[0];
                    return udid;
                }
            }
        } catch (error) {
            logger.warn('Error detecting devices via adb: ' + error.message);
        }
        return null;
    }
}

module.exports = DriverFactory;
