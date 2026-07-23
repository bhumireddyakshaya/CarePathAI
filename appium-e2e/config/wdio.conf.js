const path = require('path');
const fs = require('fs');

// Load .env file manually if it exists
const envPath = path.join(__dirname, '..', '.env');
if (fs.existsSync(envPath)) {
    const envConfig = fs.readFileSync(envPath, 'utf-8').split('\n');
    envConfig.forEach(line => {
        if (line && line.includes('=')) {
            const [key, value] = line.split('=');
            process.env[key.trim()] = value.trim();
        }
    });
}

const isCI = process.env.ENV === 'ci';

exports.config = {
    //
    // ====================
    // Runner Configuration
    // ====================
    //
    runner: 'local',
    port: 4723,

    autoCompileOpts: {
        autoCompile: false
    },

    //
    // ==================
    // Specify Test Files
    // ==================
    //
    specs: [
        '../tests/**/*.test.js'
    ],
    exclude: [
        // 'path/to/excluded/files'
    ],

    //
    // ============
    // Capabilities
    // ============
    //
    maxInstances: 1,
    capabilities: [require('../drivers/DriverFactory').getCapabilities()],

    //
    // ===================
    // Test Configurations
    // ===================
    //
    logLevel: 'info',
    bail: 0,
    baseUrl: '',
    waitforTimeout: 10000,
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,

    services: [],

    framework: 'mocha',
    reporters: [
        'spec',
        ['mochawesome', {
            outputDir: './reports',
            outputFileFormat: function(opts) { 
                return `results-${opts.cid}.json`
            }
        }]
    ],
    
    mochaOpts: {
        ui: 'bdd',
        timeout: 60000
    },

    //
    // =====
    // Hooks
    // =====
    //
    onPrepare: function (config, capabilities) {
        const dirs = ['./reports', './reports/failures', './screenshots', './logs', './excel'];
        dirs.forEach(dir => {
            if (!fs.existsSync(dir)) {
                fs.mkdirSync(dir, { recursive: true });
            }
        });
    },

    afterTest: async function(test, context, { error, result, duration, passed, retries }) {
        if (error) {
            const timestamp = new Date().getTime();
            const screenshotPath = `./reports/failures/screenshot_${test.title.replace(/\s+/g, '_')}_${timestamp}.png`;
            await browser.saveScreenshot(screenshotPath);
            
            // Capture logcat
            try {
                const logs = await browser.getLogs('logcat');
                fs.writeFileSync(`./reports/failures/logcat_${test.title.replace(/\s+/g, '_')}_${timestamp}.txt`, JSON.stringify(logs, null, 2));
            } catch (e) {
                console.error("Failed to capture logcat:", e.message);
            }
        }
    }
};
