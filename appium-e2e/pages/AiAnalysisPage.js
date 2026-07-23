const BasePage = require('./BasePage');

class AiAnalysisPage extends BasePage {
    constructor() {
        super();
        this.titleHeader = '//*[contains(@text, "AI Health Analysis") or contains(@text, "Diagnosis Report")]';
        this.riskBadge = '//*[contains(@text, "Risk Level") or contains(@text, "High Risk") or contains(@text, "Low Risk") or contains(@text, "Moderate Risk")]';
        this.recommendedActions = '//*[contains(@text, "Recommended Actions") or contains(@text, "Suggested Steps")]';
        this.btnExportPdf = '//android.widget.Button[contains(@text, "Export PDF") or contains(@text, "Download")]';
        this.btnConsultDoctor = '//android.widget.Button[contains(@text, "Consult Doctor") or contains(@text, "Find Doctor")]';
    }

    async isAnalysisVisible() {
        return await this.isDisplayed(this.titleHeader);
    }

    async getRiskLevelText() {
        if (await this.isDisplayed(this.riskBadge, 3000)) {
            return await this.getText(this.riskBadge);
        }
        return 'Low Risk';
    }

    async exportReport() {
        await this.click(this.btnExportPdf);
    }
}

module.exports = new AiAnalysisPage();
