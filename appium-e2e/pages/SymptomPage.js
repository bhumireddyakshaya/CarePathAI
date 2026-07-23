const BasePage = require('./BasePage');

class SymptomPage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "Symptom Assessment") or contains(@text, "Describe Symptoms")]';
        this.symptomSearchInput = '//android.widget.EditText[contains(@text, "Search symptom") or contains(@text, "Describe")]';
        this.severitySlider = '//android.widget.SeekBar';
        this.durationDropdown = '//*[contains(@text, "Duration") or contains(@text, "Days")]';
        this.bodyPartSelector = '//*[contains(@text, "Head") or contains(@text, "Chest") or contains(@text, "Abdomen")]';
        this.btnSubmitSymptom = '//android.widget.Button[contains(@text, "Analyze") or contains(@text, "Submit")]';
        this.validationErrorMsg = '//*[contains(@text, "Please select at least one symptom")]';
    }

    async isSymptomPageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }

    async enterSymptomDescription(description) {
        await this.setValue(this.symptomSearchInput, description);
    }

    async selectBodyPart(part) {
        const selector = `//*[contains(@text, "${part}")]`;
        if (await this.isDisplayed(selector, 2000)) {
            await this.click(selector);
        }
    }

    async submitAssessment() {
        await this.click(this.btnSubmitSymptom);
    }
}

module.exports = new SymptomPage();
