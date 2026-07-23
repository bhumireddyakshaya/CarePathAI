const BasePage = require('./BasePage');

class DashboardPage extends BasePage {
    
    // Selectors
    get txtWelcome() { return '//*[contains(@text, "Welcome")]'; }
    get bottomNav() { return '//android.widget.NavigationBar'; } // Not strictly needed, we target tabs directly
    get tabHome() { return '//*[@content-desc="Home"]'; }
    get tabAssessment() { return '//*[@content-desc="Assessment"]'; }
    get tabWellness() { return '//*[@content-desc="Wellness"]'; }
    get tabHistory() { return '//*[@content-desc="History"]'; }
    get tabProfile() { return '//*[@content-desc="Profile"]'; }
    get btnLogout() { return '//android.widget.Button[@text="Logout"]'; }
    
    // Quick Insights / Dashboard Actions
    get cardSymptomAnalysis() { return '//*[contains(@text, "Symptom Analysis")]'; }
    get cardFood() { return '//*[contains(@text, "Recommended Food")]'; }
    get cardExercise() { return '//*[contains(@text, "Exercise Plan")]'; }
    get cardHistory() { return '//*[contains(@text, "Health History")]'; }
    
    // Floating Action Button
    get fabAIChatbot() { return '//*[@content-desc="AI Assistant"]'; }

    async isDashboardVisible() {
        return await this.isDisplayed(this.txtWelcome);
    }

    async getWelcomeText() {
        return await this.getText(this.txtWelcome);
    }

    async logout() {
        // Go to Profile Tab first
        await this.click(this.tabProfile);
        await browser.pause(500);
        await this.click(this.btnLogout);
    }

    async navigateToProfile() {
        await this.click(this.tabProfile);
    }
    
    async navigateToAssessment() {
        await this.click(this.tabAssessment);
    }
}

module.exports = new DashboardPage();

