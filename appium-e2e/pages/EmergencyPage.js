const BasePage = require('./BasePage');

class EmergencyPage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "Emergency SOS") or contains(@text, "Emergency Assistant")]';
        this.btnTriggerSos = '//android.widget.Button[contains(@text, "SOS") or contains(@text, "EMERGENCY")]';
        this.sosActiveBanner = '//*[contains(@text, "SOS Alert Sent") or contains(@text, "Calling Emergency")]';
        this.btnCancelSos = '//android.widget.Button[contains(@text, "Cancel SOS") or contains(@text, "I am Safe")]';
    }

    async isEmergencyPageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }
}

module.exports = new EmergencyPage();
