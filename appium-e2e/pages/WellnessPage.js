const BasePage = require('./BasePage');

class WellnessPage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "Wellness Tracker") or contains(@text, "Daily Log")]';
        this.waterCounterBtn = '//*[contains(@text, "+ 250ml") or contains(@text, "Add Water")]';
        this.moodSlider = '//android.widget.SeekBar';
        this.sleepHoursInput = '//android.widget.EditText[contains(@text, "Sleep") or contains(@text, "Hours")]';
        this.btnSaveWellness = '//android.widget.Button[contains(@text, "Save Daily Log") or contains(@text, "Submit")]';
    }

    async isWellnessPageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }
}

module.exports = new WellnessPage();
