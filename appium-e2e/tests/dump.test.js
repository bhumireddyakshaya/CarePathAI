const fs = require('fs');
const path = require('path');

describe('Dump Page Source', function() {
    it('should dump page source to a file', async function() {
        // Wait 5 seconds to make sure the app loads and is on the login screen
        await browser.pause(5000);
        const source = await browser.getPageSource();
        const outputPath = path.join(__dirname, '..', 'logs', 'page_source.xml');
        fs.writeFileSync(outputPath, source);
        console.log('PAGE SOURCE WRITTEN TO:', outputPath);
    });
});
