const BasePage = require('./BasePage');

class HistoryPage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "Health History") or contains(@text, "Past Reports")]';
        this.historyRecordCard = '(//android.widget.TextView[contains(@text, "Assessment") or contains(@text, "Report")])[1]';
        this.filterAll = '//*[contains(@text, "All") or contains(@text, "Recent")]';
        this.btnDeleteRecord = '//android.widget.Button[contains(@text, "Delete") or contains(@text, "Remove")]';
    }

    async isHistoryPageVisible() {
        return await this.isDisplayed(this.titleHeader);
    }
}

module.exports = new HistoryPage();
